package com.logis.wms.service;

import com.logis.auth.entity.Account;
import com.logis.auth.enums.AccountRole;
import com.logis.auth.enums.AccountStatus;
import com.logis.auth.repository.AccountRepository;
import com.logis.wms.dto.order.BulkOrderResult;
import com.logis.wms.dto.order.OrderCreateDto;
import com.logis.wms.dto.order.OrderItemDto;
import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.entity.Order;
import com.logis.wms.entity.OrderItem;
import com.logis.wms.entity.Product;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.repository.OrderRepository;
import com.logis.wms.repository.ProductRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    // 상태·계정·키워드 필터로 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrders(OrderStatus status, Long accountId,
                                            String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.trim().toUpperCase() + "%" : null;
        return orderRepository.searchOrders(kw, accountId, status, pageable)
                .map(OrderResponseDto::from);
    }

    // 주문 단건을 조회하고 접근 권한을 검증한다
    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long id, Long accountId) {
        Order order = findActiveOrder(id);
        verifyOwnership(order, accountId);
        return OrderResponseDto.from(order);
    }

    // 주문을 생성하고 INBOUND_PENDING 상태로 저장한다
    public OrderResponseDto createOrder(OrderCreateDto dto, Long sessionAccountId) {
        Long resolvedId = resolveAccountId(sessionAccountId, dto.getAccountId());
        Account account = resolveAndValidateAccount(resolvedId);
        return buildAndSaveOrder(dto, account);
    }

    // 엑셀 파일을 파싱하여 주문을 일괄 등록하고 성공/실패 건수를 반환한다
    public BulkOrderResult bulkCreateOrders(MultipartFile file, Long accountId) {
        Account account = resolveAndValidateAccount(accountId);
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String customerName = cellString(row, 0);
                    String sku = cellString(row, 1);
                    int qty = (int) row.getCell(2).getNumericCellValue();

                    Optional<Product> productOpt = (accountId != null)
                            ? productRepository.findBySkuAndAccount_IdAndDeletedFalse(sku, accountId)
                            : productRepository.findBySkuAndDeletedFalse(sku);
                    Product product = productOpt
                            .orElseThrow(() -> new IllegalArgumentException("상품 SKU 없음: " + sku));

                    OrderCreateDto dto = new OrderCreateDto();
                    dto.setCustomerName(customerName);
                    OrderItemDto itemDto = new OrderItemDto();
                    itemDto.setProductId(product.getId());
                    itemDto.setQuantity(qty);
                    dto.setItems(List.of(itemDto));
                    buildAndSaveOrder(dto, account);
                    successCount++;
                } catch (Exception e) {
                    errors.add("행 " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일을 읽을 수 없습니다: " + e.getMessage());
        }

        return new BulkOrderResult(successCount, errors.size(), errors);
    }

    // INBOUND_PENDING 이전 상태의 주문만 소프트 삭제한다
    public void deleteOrder(Long id, Long accountId) {
        Order order = findActiveOrder(id);
        verifyOwnership(order, accountId);
        if (order.getStatus() != OrderStatus.ORDER_CREATED
                && order.getStatus() != OrderStatus.INBOUND_PENDING) {
            throw new IllegalStateException("입고 대기(INBOUND_PENDING) 상태의 주문만 삭제할 수 있습니다.");
        }
        order.setDeleted(true);
        orderRepository.save(order);
    }

    // 주문을 ORDER_HOLD 상태로 전환하고 이전 상태를 prevStatus에 보존한다
    public void holdOrder(Long id, String reason) {
        Order order = findActiveOrder(id);
        if (order.getStatus() == OrderStatus.OUTBOUND_COMPLETED
                || order.getStatus() == OrderStatus.SHIPPED)
            throw new IllegalStateException("출고완료된 주문은 보류할 수 없습니다.");
        if (order.getStatus() == OrderStatus.ORDER_HOLD)
            throw new IllegalStateException("이미 주문 보류 상태입니다.");
        order.setPrevStatus(order.getStatus());
        order.setHoldReason(reason);
        order.setStatus(OrderStatus.ORDER_HOLD);
        orderRepository.save(order);
    }

    // 보류 상태의 주문을 prevStatus로 복원한다
    public void resumeOrder(Long id) {
        Order order = findActiveOrder(id);
        if (order.getStatus() != OrderStatus.ORDER_HOLD)
            throw new IllegalStateException("주문 보류 상태가 아닙니다.");
        OrderStatus prev = order.getPrevStatus();
        if (prev == null) prev = OrderStatus.INBOUND_PENDING;
        order.setStatus(prev);
        order.setPrevStatus(null);
        order.setHoldReason(null);
        orderRepository.save(order);
    }

    // 특정 상태의 주문 전체를 리스트로 반환한다
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusAndDeletedFalse(status).stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    // 삭제되지 않은 주문을 ID로 조회하고 없으면 예외를 던진다
    Order findActiveOrder(Long id) {
        return orderRepository.findById(id)
                .filter(o -> Boolean.FALSE.equals(o.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
    }

    // 세션 accountId를 우선 사용하고, 없으면 DTO의 accountId를 사용한다 (둘 다 없으면 null 허용)
    private Long resolveAccountId(Long sessionAccountId, Long dtoAccountId) {
        if (sessionAccountId != null) return sessionAccountId;
        if (dtoAccountId != null) return dtoAccountId;
        return null;
    }

    // accountId로 계정을 조회하고 CLIENT는 APPROVED 상태인지 검증한다
    private Account resolveAndValidateAccount(Long accountId) {
        if (accountId == null) return null;
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        if (account.getRole() == AccountRole.CLIENT
                && account.getStatus() != AccountStatus.APPROVED) {
            throw new IllegalStateException(
                    "승인된 고객만 주문을 생성할 수 있습니다. (현재 상태: " + account.getStatus() + ")");
        }
        return account;
    }

    // 주문 엔티티와 주문 항목을 생성하고 저장 후 주문번호를 부여한다
    private OrderResponseDto buildAndSaveOrder(OrderCreateDto dto, Account account) {
        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setDeleted(false);
        order.setAccount(account);

        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "상품을 찾을 수 없습니다: " + itemDto.getProductId()));
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            order.getItems().add(item);
        }

        order.setStatus(OrderStatus.INBOUND_PENDING);
        Order saved = orderRepository.save(order);
        saved.setOrderNo("ORD-" + String.format("%08d", saved.getId()));
        return OrderResponseDto.from(orderRepository.save(saved));
    }

    // 일반 사용자가 다른 계정의 주문에 접근하면 예외를 던진다
    private void verifyOwnership(Order order, Long accountId) {
        if (accountId != null
                && (order.getAccount() == null
                    || !accountId.equals(order.getAccount().getId()))) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }
    }

    // 엑셀 셀 값을 문자열로 읽는다 (숫자 셀은 정수로 변환)
    private String cellString(Row row, int col) {
        var cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}
