package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.common.HoldReasonDto;
import com.logis.wms.dto.order.BulkOrderResult;
import com.logis.wms.dto.order.OrderCreateDto;
import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.service.OrderService;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 상태·키워드 필터로 주문 목록을 페이징 조회한다
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        Page<OrderResponseDto> result = orderService.getOrders(status, accountId, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    // 주문 단건을 조회한다
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @PathVariable Long id,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        OrderResponseDto result = orderService.getOrder(id, accountId);
        return ResponseEntity.ok(result);
    }

    // 주문을 생성하고 INBOUND_PENDING 상태로 저장한다
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody OrderCreateDto dto,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        OrderResponseDto result = orderService.createOrder(dto, accountId);
        return ResponseEntity.ok(result);
    }

    // 엑셀 파일을 파싱해 주문을 일괄 등록한다
    @PostMapping("/bulk")
    public ResponseEntity<BulkOrderResult> bulkCreate(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        BulkOrderResult result = orderService.bulkCreateOrders(file, accountId);
        return ResponseEntity.ok(result);
    }

    // 주문을 삭제한다 (INBOUND_PENDING 이전 상태만 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        orderService.deleteOrder(id, accountId);
        return ResponseEntity.noContent().build();
    }

    // 주문을 ORDER_HOLD 상태로 전환하고 이전 상태를 보존한다
    @PostMapping("/{id}/hold")
    public ResponseEntity<Void> holdOrder(
            @PathVariable Long id,
            @RequestBody(required = false) HoldReasonDto body) {
        String reason = (body != null) ? body.getReason() : null;
        orderService.holdOrder(id, reason);
        return ResponseEntity.ok().build();
    }

    // 보류 상태의 주문을 이전 상태로 복원한다
    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeOrder(@PathVariable Long id) {
        orderService.resumeOrder(id);
        return ResponseEntity.ok().build();
    }

    private Long resolveAccountId(SessionUser user) {
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }
}
