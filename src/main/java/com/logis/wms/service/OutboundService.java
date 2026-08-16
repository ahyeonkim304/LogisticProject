package com.logis.wms.service;

import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.entity.Inventory;
import com.logis.wms.entity.InventoryLog;
import com.logis.wms.entity.Order;
import com.logis.wms.entity.OrderItem;
import com.logis.wms.enums.MovementType;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.repository.InventoryLogRepository;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboundService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;

    // 입고 완료 주문을 OUTBOUND_PENDING으로 전환하여 출고를 시작한다
    public OrderResponseDto startOutbound(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        if (order.getStatus() != OrderStatus.INBOUND_COMPLETED
                && order.getStatus() != OrderStatus.READY_TO_SHIP)
            throw new IllegalStateException(
                    "출고 대기 전환은 입고 완료(INBOUND_COMPLETED) 상태에서만 가능합니다. (현재: "
                    + order.getStatus().getLabel() + ")");
        order.setStatus(OrderStatus.OUTBOUND_PENDING);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // FIFO 순서로 재고를 차감하고 OUTBOUND_COMPLETED 상태로 전환한다
    public OrderResponseDto completeOutbound(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.OUTBOUND_PENDING, "출고 완료는 출고 대기(OUTBOUND_PENDING) 상태에서만 가능합니다.");
        for (OrderItem item : order.getItems()) {
            deductFifo(item.getProduct().getId(), item.getProduct().getSku(),
                    item.getQuantity(), "ORDER", orderId);
        }
        order.setStatus(OrderStatus.OUTBOUND_COMPLETED);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 출고 대기 주문을 OUTBOUND_HOLD 상태로 전환하고 보류 사유를 저장한다
    public OrderResponseDto holdOutbound(Long orderId, String reason) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.OUTBOUND_PENDING, "출고 보류는 출고 대기(OUTBOUND_PENDING) 상태에서만 가능합니다.");
        order.setHoldReason(reason);
        order.setStatus(OrderStatus.OUTBOUND_HOLD);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 출고 보류를 해제하여 OUTBOUND_PENDING으로 복원한다
    public OrderResponseDto resumeOutbound(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.OUTBOUND_HOLD, "출고 보류 해제는 출고 보류(OUTBOUND_HOLD) 상태에서만 가능합니다.");
        order.setStatus(OrderStatus.OUTBOUND_PENDING);
        order.setHoldReason(null);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 출고 완료 주문을 SHIPPED 상태로 전환하여 최종 출하 처리한다
    public OrderResponseDto shipOrder(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.OUTBOUND_COMPLETED, "출하는 OUTBOUND_COMPLETED 상태에서만 가능합니다.");
        order.setStatus(OrderStatus.SHIPPED);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 출고 대기 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOutboundPendingOrders(Pageable pageable) {
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.OUTBOUND_PENDING, pageable)
                .map(OrderResponseDto::from);
    }

    // 출고 보류 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOutboundHoldOrders(Pageable pageable) {
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.OUTBOUND_HOLD, pageable)
                .map(OrderResponseDto::from);
    }

    // 출고 완료 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOutboundCompletedOrders(Pageable pageable) {
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.OUTBOUND_COMPLETED, pageable)
                .map(OrderResponseDto::from);
    }

    // INBOUND_COMPLETED와 READY_TO_SHIP 주문을 합쳐 수동 페이징으로 반환한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getReadyForOutboundOrders(Pageable pageable) {
        List<Order> combined = new ArrayList<>();
        combined.addAll(orderRepository.findByStatusAndDeletedFalse(OrderStatus.INBOUND_COMPLETED));
        combined.addAll(orderRepository.findByStatusAndDeletedFalse(OrderStatus.READY_TO_SHIP));
        List<OrderResponseDto> dtos = combined.stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<OrderResponseDto> slice = (start < dtos.size()) ? dtos.subList(start, end) : List.of();
        return new PageImpl<>(slice, pageable, dtos.size());
    }

    // 입고가 오래된 로케이션부터 순서대로 재고를 차감하고 출고 로그를 기록한다 (PalletService에서도 호출)
    void deductFifo(Long productId, String sku, int required, String refType, Long refId) {
        List<Inventory> stocks = inventoryRepository.findByProductIdForUpdate(productId);
        int remaining = required;
        for (Inventory inv : stocks) {
            if (remaining <= 0) break;
            int deduct = Math.min(remaining, inv.getQuantity());
            inv.setQuantity(inv.getQuantity() - deduct);
            inventoryRepository.save(inv);
            remaining -= deduct;

            InventoryLog log = new InventoryLog();
            log.setProduct(inv.getProduct());
            log.setType(MovementType.OUTBOUND);
            log.setQuantity(deduct);
            log.setLocation(inv.getLocation());
            log.setReferenceType(refType);
            log.setReferenceId(refId);
            log.setCreatedAt(LocalDateTime.now());
            inventoryLogRepository.save(log);
        }
        if (remaining > 0)
            throw new IllegalStateException(
                    String.format("출고 가능한 재고가 부족합니다: %s (부족: %d)", sku, remaining));
    }

    // 주문 상태가 기대값과 다르면 메시지와 함께 예외를 던진다
    private void assertStatus(Order order, OrderStatus expected, String message) {
        if (order.getStatus() != expected)
            throw new IllegalStateException(message + " (현재 상태: " + order.getStatus().getLabel() + ")");
    }
}
