package com.logis.wms.dto.order;

import com.logis.wms.entity.Order;
import com.logis.wms.enums.OrderStatus;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class OrderResponseDto {

    private final Long id;
    private final String orderNo;
    private final OrderStatus status;
    private final String customerName;
    private final String holdReason;
    private final OrderStatus prevStatus;
    private final List<OrderItemDto> items;

    private OrderResponseDto(Order o) {
        this.id           = o.getId();
        this.orderNo      = o.getOrderNo();
        this.status       = o.getStatus();
        this.customerName = o.getCustomerName();
        this.holdReason   = o.getHoldReason();
        this.prevStatus   = o.getPrevStatus();
        this.items = o.getItems().stream()
                .map(OrderItemDto::from)
                .collect(Collectors.toList());
    }

    public static OrderResponseDto from(Order o) {
        return new OrderResponseDto(o);
    }

    public String getStatusLabel() {
        if (status == null) return "";
        switch (status) {
            case INBOUND_PENDING:    return "입고 대기";
            case INBOUND_HOLD:       return "입고 보류";
            case INBOUND_COMPLETED:  return "입고 완료";
            case OUTBOUND_PENDING:   return "출고 대기";
            case OUTBOUND_HOLD:      return "출고 보류";
            case OUTBOUND_COMPLETED: return "출고 완료";
            case ORDER_HOLD:         return "주문 보류";
            case ORDER_CREATED:      return "주문 생성";
            case READY_TO_SHIP:      return "출고 준비";
            case SHIPPED:            return "출하 완료";
            default:                 return status.name();
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case INBOUND_PENDING:    return "badge-inbound-pending";
            case INBOUND_HOLD:       return "badge-inbound-hold";
            case INBOUND_COMPLETED:  return "badge-inbound-done";
            case OUTBOUND_PENDING:   return "badge-outbound-pending";
            case OUTBOUND_HOLD:      return "badge-outbound-hold";
            case OUTBOUND_COMPLETED: return "badge-outbound-done";
            case ORDER_HOLD:         return "badge-order-hold";
            case ORDER_CREATED:      return "badge-created";
            case READY_TO_SHIP:      return "badge-ready-to-ship";
            case SHIPPED:            return "badge-shipped";
            default:                 return "badge-secondary";
        }
    }
}
