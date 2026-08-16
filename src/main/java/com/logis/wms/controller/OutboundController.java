package com.logis.wms.controller;

import com.logis.wms.dto.common.HoldReasonDto;
import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    // 출고 준비(INBOUND_COMPLETED + READY_TO_SHIP) 주문 목록을 반환한다
    @GetMapping("/ready")
    public ResponseEntity<Page<OrderResponseDto>> ready(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = outboundService.getReadyForOutboundOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // 출고 대기(OUTBOUND_PENDING) 주문 목록을 반환한다
    @GetMapping("/pending")
    public ResponseEntity<Page<OrderResponseDto>> pending(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = outboundService.getOutboundPendingOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // 출고 보류(OUTBOUND_HOLD) 주문 목록을 반환한다
    @GetMapping("/hold")
    public ResponseEntity<Page<OrderResponseDto>> hold(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = outboundService.getOutboundHoldOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // 출고 완료(OUTBOUND_COMPLETED) 주문 목록을 반환한다
    @GetMapping("/completed")
    public ResponseEntity<Page<OrderResponseDto>> completed(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = outboundService.getOutboundCompletedOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // 입고 완료 주문을 OUTBOUND_PENDING으로 전환하여 출고를 시작한다
    @PostMapping("/{id}/start")
    public ResponseEntity<OrderResponseDto> start(@PathVariable Long id) {
        OrderResponseDto result = outboundService.startOutbound(id);
        return ResponseEntity.ok(result);
    }

    // FIFO로 재고를 차감하고 OUTBOUND_COMPLETED 상태로 전환한다
    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderResponseDto> complete(@PathVariable Long id) {
        OrderResponseDto result = outboundService.completeOutbound(id);
        return ResponseEntity.ok(result);
    }

    // 출고 주문을 OUTBOUND_HOLD 상태로 전환한다
    @PostMapping("/{id}/hold")
    public ResponseEntity<OrderResponseDto> holdOrder(
            @PathVariable Long id,
            @RequestBody(required = false) HoldReasonDto body) {
        String reason = (body != null) ? body.getReason() : null;
        OrderResponseDto result = outboundService.holdOutbound(id, reason);
        return ResponseEntity.ok(result);
    }

    // 출고 보류를 해제하여 OUTBOUND_PENDING으로 복원한다
    @PostMapping("/{id}/resume")
    public ResponseEntity<OrderResponseDto> resumeOrder(@PathVariable Long id) {
        OrderResponseDto result = outboundService.resumeOutbound(id);
        return ResponseEntity.ok(result);
    }

    // 출고 완료 주문을 SHIPPED 상태로 전환하여 최종 출하 처리한다
    @PostMapping("/{id}/ship")
    public ResponseEntity<OrderResponseDto> ship(@PathVariable Long id) {
        OrderResponseDto result = outboundService.shipOrder(id);
        return ResponseEntity.ok(result);
    }
}
