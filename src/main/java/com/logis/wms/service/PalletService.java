package com.logis.wms.service;

import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.dto.pallet.PalletResponseDto;
import com.logis.wms.dto.pallet.PickingItemDto;
import com.logis.wms.entity.Inventory;
import com.logis.wms.entity.Order;
import com.logis.wms.entity.OrderItem;
import com.logis.wms.entity.Pallet;
import com.logis.wms.entity.PalletOrder;
import com.logis.wms.entity.Product;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.enums.PalletStatus;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.OrderRepository;
import com.logis.wms.repository.PalletOrderRepository;
import com.logis.wms.repository.PalletRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PalletService {

    private final PalletRepository palletRepository;
    private final PalletOrderRepository palletOrderRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OutboundService outboundService;

    // 팔레트를 생성하고 날짜 기반 팔레트 코드를 부여한다
    public PalletResponseDto createPallet(String name) {
        Pallet pallet = new Pallet();
        pallet.setName(name != null && !name.isBlank() ? name.trim() : null);
        pallet.setStatus(PalletStatus.CREATED);
        Pallet saved = palletRepository.save(pallet);
        String dateStr = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        saved.setPalletCode(String.format("PLT-%s-%04d", dateStr, saved.getId()));
        return PalletResponseDto.from(palletRepository.save(saved));
    }

    // INBOUND_COMPLETED 주문을 팔레트에 추가하고 READY_TO_SHIP으로 전환한다
    public PalletResponseDto addOrderToPallet(Long palletId, Long orderId) {
        Pallet pallet = findActivePallet(palletId);
        Order order = orderRepository.findById(orderId)
                .filter(o -> Boolean.FALSE.equals(o.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getStatus() != OrderStatus.INBOUND_COMPLETED)
            throw new IllegalStateException(
                    "입고 완료(INBOUND_COMPLETED) 상태인 주문만 팔레트에 추가할 수 있습니다. "
                    + "(현재 상태: " + order.getStatus() + ")");

        if (palletOrderRepository.findByPalletId(palletId).stream()
                .anyMatch(po -> po.getOrder().getId().equals(orderId)))
            throw new IllegalStateException("이미 팔레트에 포함된 주문입니다.");

        PalletOrder palletOrder = new PalletOrder();
        palletOrder.setPallet(pallet);
        palletOrder.setOrder(order);
        palletOrderRepository.save(palletOrder);

        order.setStatus(OrderStatus.READY_TO_SHIP);
        orderRepository.save(order);
        return buildDetail(pallet);
    }

    // 팔레트에서 주문을 제거하고 해당 주문을 INBOUND_COMPLETED로 되돌린다
    public void removeOrderFromPallet(Long palletId, Long orderId) {
        PalletOrder palletOrder = palletOrderRepository.findByPalletId(palletId).stream()
                .filter(po -> po.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("팔레트에서 주문을 찾을 수 없습니다."));
        palletOrder.getOrder().setStatus(OrderStatus.INBOUND_COMPLETED);
        orderRepository.save(palletOrder.getOrder());
        palletOrderRepository.delete(palletOrder);
    }

    // 팔레트 내 모든 주문 상품을 FIFO로 출고하고 주문·팔레트를 SHIPPED로 전환한다
    public void processOutbound(Long palletId) {
        Pallet pallet = findActivePallet(palletId);
        List<PalletOrder> palletOrders = palletOrderRepository.findByPalletId(palletId);
        if (palletOrders.isEmpty()) throw new IllegalStateException("팔레트에 주문이 없습니다.");

        Map<Long, int[]> required = new HashMap<>();
        Map<Long, String> skuMap = new HashMap<>();
        for (PalletOrder po : palletOrders) {
            for (OrderItem item : po.getOrder().getItems()) {
                Long pid = item.getProduct().getId();
                required.merge(pid, new int[]{item.getQuantity()}, (a, b) -> new int[]{a[0] + b[0]});
                skuMap.put(pid, item.getProduct().getSku());
            }
        }

        for (Map.Entry<Long, int[]> entry : required.entrySet()) {
            outboundService.deductFifo(entry.getKey(), skuMap.get(entry.getKey()),
                    entry.getValue()[0], "PALLET", palletId);
        }

        for (PalletOrder po : palletOrders) {
            po.getOrder().setStatus(OrderStatus.SHIPPED);
            orderRepository.save(po.getOrder());
        }

        pallet.setStatus(PalletStatus.SHIPPED);
        palletRepository.save(pallet);
    }

    // 출하되지 않은 팔레트 목록을 반환한다
    @Transactional(readOnly = true)
    public List<PalletResponseDto> getActivePallets() {
        return palletRepository.findAll().stream()
                .filter(p -> p.getStatus() != PalletStatus.SHIPPED)
                .map(PalletResponseDto::from)
                .collect(Collectors.toList());
    }

    // 팔레트 상세 정보(주문 목록·피킹 목록 포함)를 반환한다
    @Transactional(readOnly = true)
    public PalletResponseDto getPalletDetail(Long palletId) {
        return buildDetail(findActivePallet(palletId));
    }

    // 팔레트에 배정되지 않은 INBOUND_COMPLETED 주문 목록을 반환한다
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAvailableOrders() {
        Set<Long> inPallet = palletOrderRepository.findAll().stream()
                .filter(po -> po.getPallet().getStatus() != PalletStatus.SHIPPED)
                .map(po -> po.getOrder().getId())
                .collect(Collectors.toSet());
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.INBOUND_COMPLETED).stream()
                .filter(o -> !inPallet.contains(o.getId()))
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    // 팔레트에 포함된 주문과 피킹 목록을 조합하여 응답 DTO를 생성한다
    private PalletResponseDto buildDetail(Pallet pallet) {
        List<PalletOrder> palletOrders = palletOrderRepository.findByPalletId(pallet.getId());
        List<OrderResponseDto> orders = palletOrders.stream()
                .map(po -> OrderResponseDto.from(po.getOrder())).collect(Collectors.toList());
        return PalletResponseDto.from(pallet, orders, buildPickingList(palletOrders));
    }

    // 팔레트 내 주문의 피킹 목록을 로케이션별로 구성한다 (재고 부족 시 경고 항목 추가)
    private List<PickingItemDto> buildPickingList(List<PalletOrder> palletOrders) {
        Map<Long, Integer> required = new HashMap<>();
        Map<Long, Product> productMap = new HashMap<>();
        for (PalletOrder po : palletOrders) {
            for (OrderItem item : po.getOrder().getItems()) {
                Long pid = item.getProduct().getId();
                required.merge(pid, item.getQuantity(), Integer::sum);
                productMap.put(pid, item.getProduct());
            }
        }

        List<PickingItemDto> list = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : required.entrySet()) {
            Long productId = entry.getKey();
            int remaining = entry.getValue();
            Product product = productMap.get(productId);
            for (Inventory inv : inventoryRepository.findByProduct_IdOrderByIdAsc(productId)) {
                if (remaining <= 0) break;
                if (inv.getQuantity() <= 0) continue;
                int pick = Math.min(remaining, inv.getQuantity());
                list.add(new PickingItemDto(product.getSku(), product.getName(),
                        inv.getLocation().getCode(), inv.getLocation().getName(), pick));
                remaining -= pick;
            }
            if (remaining > 0)
                list.add(new PickingItemDto(product.getSku(), product.getName(), "⚠ 재고 부족", "", remaining));
        }
        return list;
    }

    // 팔레트를 조회하고 이미 출하된 경우 예외를 던진다
    private Pallet findActivePallet(Long palletId) {
        Pallet pallet = palletRepository.findById(palletId)
                .orElseThrow(() -> new IllegalArgumentException("팔레트를 찾을 수 없습니다: " + palletId));
        if (pallet.getStatus() == PalletStatus.SHIPPED)
            throw new IllegalStateException("이미 출하된 팔레트입니다.");
        return pallet;
    }
}
