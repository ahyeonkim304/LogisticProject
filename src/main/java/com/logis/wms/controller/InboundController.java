package com.logis.wms.controller;

import com.logis.wms.dto.common.HoldReasonDto;
import com.logis.wms.dto.inbound.ScanRequestDto;
import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.service.InboundService;
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
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    // 입고 대기(INBOUND_PENDING) 주문 목록을 반환한다
    @GetMapping("/pending")
    public ResponseEntity<Page<OrderResponseDto>> pending(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = inboundService.getInboundPendingOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // 입고 보류(INBOUND_HOLD) 주문 목록을 반환한다
    @GetMapping("/hold")
    public ResponseEntity<Page<OrderResponseDto>> hold(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = inboundService.getInboundHoldOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // 입고 완료(INBOUND_COMPLETED) 주문 목록을 반환한다
    @GetMapping("/completed")
    public ResponseEntity<Page<OrderResponseDto>> completed(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<OrderResponseDto> result = inboundService.getInboundCompletedOrders(pageable);
        return ResponseEntity.ok(result);
    }

    // ORDER_CREATED 상태의 주문을 INBOUND_PENDING으로 전환한다
    @PostMapping("/{id}/start")
    public ResponseEntity<OrderResponseDto> start(@PathVariable Long id) {
        OrderResponseDto result = inboundService.startInbound(id);
        return ResponseEntity.ok(result);
    }

    // 입고를 완료하여 INBOUND_COMPLETED 상태로 전환한다
    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderResponseDto> complete(@PathVariable Long id) {
        OrderResponseDto result = inboundService.completeInbound(id);
        return ResponseEntity.ok(result);
    }

    // 입고 주문을 INBOUND_HOLD 상태로 전환한다
    @PostMapping("/{id}/hold")
    public ResponseEntity<OrderResponseDto> holdOrder(
            @PathVariable Long id,
            @RequestBody(required = false) HoldReasonDto body) {
        String reason = (body != null) ? body.getReason() : null;
        OrderResponseDto result = inboundService.holdInbound(id, reason);
        return ResponseEntity.ok(result);
    }

    // 입고 보류 상태를 해제하여 INBOUND_PENDING으로 복원한다
    @PostMapping("/{id}/resume")
    public ResponseEntity<OrderResponseDto> resumeOrder(@PathVariable Long id) {
        OrderResponseDto result = inboundService.resumeInbound(id);
        return ResponseEntity.ok(result);
    }

    // 바코드 스캔 및 치수·중량을 등록하고 입고를 처리한다
    @PostMapping("/{id}/scan")
    public ResponseEntity<OrderResponseDto> scan(
            @PathVariable Long id,
            @RequestBody ScanRequestDto dto) {
        OrderResponseDto result = inboundService.scanWithMeasurement(id, dto);
        return ResponseEntity.ok(result);
    }
}
