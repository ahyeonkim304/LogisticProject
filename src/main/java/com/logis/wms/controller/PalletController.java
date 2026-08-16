package com.logis.wms.controller;

import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.dto.pallet.PalletCreateDto;
import com.logis.wms.dto.pallet.PalletResponseDto;
import com.logis.wms.service.PalletService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pallets")
@RequiredArgsConstructor
public class PalletController {

    private final PalletService palletService;

    // 출하되지 않은 팔레트 목록을 반환한다
    @GetMapping
    public ResponseEntity<List<PalletResponseDto>> getActivePallets() {
        List<PalletResponseDto> result = palletService.getActivePallets();
        return ResponseEntity.ok(result);
    }

    // 팔레트 상세 정보와 포함된 주문·피킹 목록을 반환한다
    @GetMapping("/{id}")
    public ResponseEntity<PalletResponseDto> getDetail(@PathVariable Long id) {
        PalletResponseDto result = palletService.getPalletDetail(id);
        return ResponseEntity.ok(result);
    }

    // 팔레트에 추가 가능한 주문 목록을 반환한다 (INBOUND_COMPLETED이며 팔레트 미배정)
    @GetMapping("/available-orders")
    public ResponseEntity<List<OrderResponseDto>> availableOrders() {
        List<OrderResponseDto> result = palletService.getAvailableOrders();
        return ResponseEntity.ok(result);
    }

    // 팔레트를 생성한다 (코드 미지정 시 자동 생성)
    @PostMapping
    public ResponseEntity<PalletResponseDto> createPallet(
            @RequestBody(required = false) PalletCreateDto dto) {
        String code = (dto != null) ? dto.getCode() : null;
        PalletResponseDto result = palletService.createPallet(code);
        return ResponseEntity.ok(result);
    }

    // 입고 완료 주문을 팔레트에 추가하고 READY_TO_SHIP으로 전환한다
    @PostMapping("/{id}/orders/{orderId}")
    public ResponseEntity<PalletResponseDto> addOrder(
            @PathVariable Long id,
            @PathVariable Long orderId) {
        PalletResponseDto result = palletService.addOrderToPallet(id, orderId);
        return ResponseEntity.ok(result);
    }

    // 팔레트에서 주문을 제거하고 INBOUND_COMPLETED로 되돌린다
    @DeleteMapping("/{id}/orders/{orderId}")
    public ResponseEntity<Void> removeOrder(
            @PathVariable Long id,
            @PathVariable Long orderId) {
        palletService.removeOrderFromPallet(id, orderId);
        return ResponseEntity.noContent().build();
    }

    // 팔레트 내 전체 주문을 FIFO로 출고 처리하고 팔레트를 SHIPPED로 변경한다
    @PostMapping("/{id}/outbound")
    public ResponseEntity<Void> processOutbound(@PathVariable Long id) {
        palletService.processOutbound(id);
        return ResponseEntity.ok().build();
    }
}
