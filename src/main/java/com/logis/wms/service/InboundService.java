package com.logis.wms.service;

import com.logis.wms.dto.inbound.ScanRequestDto;
import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.dto.pallet.PalletResponseDto;
import com.logis.wms.entity.Order;
import com.logis.wms.entity.OrderItem;
import com.logis.wms.entity.OrderMeasurement;
import com.logis.wms.entity.Pallet;
import com.logis.wms.entity.PalletOrder;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.enums.PalletStatus;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.OrderMeasurementRepository;
import com.logis.wms.repository.OrderRepository;
import com.logis.wms.repository.PalletOrderRepository;
import com.logis.wms.repository.PalletRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InboundService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PalletRepository palletRepository;
    private final PalletOrderRepository palletOrderRepository;
    private final OrderMeasurementRepository orderMeasurementRepository;

    // INBOUND_PENDING 주문의 재고를 검증하고 INBOUND_COMPLETED로 전환한다
    public OrderResponseDto completeInbound(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.INBOUND_PENDING, "입고 완료는 입고 대기(INBOUND_PENDING) 상태에서만 가능합니다.");
        validateStock(order);
        order.setStatus(OrderStatus.INBOUND_COMPLETED);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 입고 주문을 INBOUND_HOLD 상태로 전환하고 보류 사유를 저장한다
    public OrderResponseDto holdInbound(Long orderId, String reason) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.INBOUND_PENDING, "입고 보류는 입고 대기(INBOUND_PENDING) 상태에서만 가능합니다.");
        order.setHoldReason(reason);
        order.setStatus(OrderStatus.INBOUND_HOLD);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // INBOUND_HOLD 상태의 주문을 INBOUND_PENDING으로 복원한다
    public OrderResponseDto resumeInbound(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        assertStatus(order, OrderStatus.INBOUND_HOLD, "입고 보류 해제는 입고 보류(INBOUND_HOLD) 상태에서만 가능합니다.");
        order.setStatus(OrderStatus.INBOUND_PENDING);
        order.setHoldReason(null);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 스캔 시 치수·중량을 기록하고 입고 완료 처리한다; 팔레트 지정 시 즉시 출고 대기로 전환
    public OrderResponseDto scanWithMeasurement(Long orderId, ScanRequestDto dto) {
        Double width = dto.getWidth();
        Double depth = dto.getDepth();
        Double height = dto.getHeight();
        Double actualWeight = dto.getWeight();
        Integer volumeDivisor = dto.getVolumeDivisor();
        Long palletId = dto.getPalletId();

        Order order = orderRepository.findById(orderId)
                .filter(o -> Boolean.FALSE.equals(o.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        if (order.getStatus() != OrderStatus.INBOUND_PENDING
                && order.getStatus() != OrderStatus.ORDER_CREATED)
            throw new IllegalStateException(
                    "이미 처리된 주문입니다. (현재 상태: " + order.getStatus().getLabel() + ")");

        validateStock(order);

        if (hasAnyMeasurement(width, depth, height, actualWeight)) {
            OrderMeasurement m = new OrderMeasurement();
            m.setOrder(order);
            m.setWidth(width);
            m.setDepth(depth);
            m.setHeight(height);
            m.setActualWeight(actualWeight);
            m.setVolumeDivisor(volumeDivisor);
            m.calcVolumeWeight();
            orderMeasurementRepository.save(m);
        }

        if (palletId != null) {
            Pallet pallet = palletRepository.findById(palletId)
                    .orElseThrow(() -> new IllegalArgumentException("팔레트를 찾을 수 없습니다: " + palletId));
            if (pallet.getStatus() == PalletStatus.SHIPPED)
                throw new IllegalStateException("이미 출하된 팔레트입니다.");
            PalletOrder po = new PalletOrder();
            po.setPallet(pallet);
            po.setOrder(order);
            palletOrderRepository.save(po);
        }

        order.setStatus(OrderStatus.INBOUND_COMPLETED);
        orderRepository.save(order);

        if (palletId != null) {
            order.setStatus(OrderStatus.OUTBOUND_PENDING);
            orderRepository.save(order);
        }

        return OrderResponseDto.from(order);
    }

    // ORDER_CREATED 주문을 INBOUND_PENDING으로 전환하여 입고를 시작한다
    public OrderResponseDto startInbound(Long orderId) {
        Order order = orderService.findActiveOrder(orderId);
        if (order.getStatus() != OrderStatus.ORDER_CREATED)
            throw new IllegalStateException("입고 시작은 ORDER_CREATED 상태에서만 가능합니다.");
        order.setStatus(OrderStatus.INBOUND_PENDING);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    // 입고 대기 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getInboundPendingOrders(Pageable pageable) {
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.INBOUND_PENDING, pageable)
                .map(OrderResponseDto::from);
    }

    // 입고 보류 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getInboundHoldOrders(Pageable pageable) {
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.INBOUND_HOLD, pageable)
                .map(OrderResponseDto::from);
    }

    // 입고 완료 주문 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getInboundCompletedOrders(Pageable pageable) {
        return orderRepository.findByStatusAndDeletedFalse(OrderStatus.INBOUND_COMPLETED, pageable)
                .map(OrderResponseDto::from);
    }

    // 출하되지 않은 팔레트 목록을 반환한다
    @Transactional(readOnly = true)
    public List<PalletResponseDto> getActivePallets() {
        return palletRepository.findAll().stream()
                .filter(p -> p.getStatus() != PalletStatus.SHIPPED)
                .map(PalletResponseDto::from)
                .collect(Collectors.toList());
    }

    // 주문 항목의 재고가 충분한지 검증한다
    private void validateStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Long qtyL = inventoryRepository.sumQuantityByProductId(item.getProduct().getId());
            int cur = qtyL != null ? qtyL.intValue() : 0;
            if (cur < item.getQuantity())
                throw new IllegalStateException(String.format(
                        "재고 부족: %s (현재: %d, 요청: %d)",
                        item.getProduct().getSku(), cur, item.getQuantity()));
        }
    }

    // 치수·중량 값 중 하나라도 입력됐는지 확인한다
    private boolean hasAnyMeasurement(Double w, Double d, Double h, Double wt) {
        return (w != null && w > 0) || (d != null && d > 0)
                || (h != null && h > 0) || (wt != null && wt > 0);
    }

    // 주문 상태가 기대값과 다르면 메시지와 함께 예외를 던진다
    private void assertStatus(Order order, OrderStatus expected, String message) {
        if (order.getStatus() != expected)
            throw new IllegalStateException(message + " (현재 상태: " + order.getStatus().getLabel() + ")");
    }
}
