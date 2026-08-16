package com.logis.wms.enums;

public enum OrderStatus {

    INBOUND_PENDING("입고 대기"),
    INBOUND_HOLD("입고 보류"),
    INBOUND_COMPLETED("입고 완료"),

    OUTBOUND_PENDING("출고 대기"),
    OUTBOUND_HOLD("출고 보류"),
    OUTBOUND_COMPLETED("출고 완료"),

    ORDER_HOLD("주문 보류"),

    ORDER_CREATED("주문 생성"),
    READY_TO_SHIP("출고 준비"),
    SHIPPED("출하 완료");

    private final String label;

    OrderStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
