package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.inventory.InventoryLogDto;
import com.logis.wms.dto.inventory.LocationInventoryDto;
import com.logis.wms.dto.inventory.StockAddRequestDto;
import com.logis.wms.dto.inventory.StockAdjustRequestDto;
import com.logis.wms.dto.product.ProductResponseDto;
import com.logis.wms.service.InventoryService;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 상품별 현재 재고 수량을 페이징 조회한다
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getInventoryList(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        Page<ProductResponseDto> result = inventoryService.getInventoryList(keyword, accountId, pageable);
        return ResponseEntity.ok(result);
    }

    // 특정 상품의 로케이션별 재고 분포를 조회한다
    @GetMapping("/{productId}/locations")
    public ResponseEntity<List<LocationInventoryDto>> getByLocation(@PathVariable Long productId) {
        List<LocationInventoryDto> result = inventoryService.getInventoryByLocation(productId);
        return ResponseEntity.ok(result);
    }

    // 최근 30건의 입출고 로그를 반환한다 (대시보드용)
    @GetMapping("/recent-logs")
    public ResponseEntity<List<InventoryLogDto>> recentLogs() {
        List<InventoryLogDto> result = inventoryService.getRecentLogs();
        return ResponseEntity.ok(result);
    }

    // 키워드·날짜 범위로 입출고 이력을 페이징 조회한다
    @GetMapping("/history")
    public ResponseEntity<Page<InventoryLogDto>> history(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        Page<InventoryLogDto> result = inventoryService.getHistoryList(
                keyword, accountId, startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }

    // SKU 기반으로 재고를 수동 입고 처리한다
    @PostMapping("/add")
    public ResponseEntity<Void> addStock(@RequestBody StockAddRequestDto dto) {
        inventoryService.addStockBySku(dto);
        return ResponseEntity.ok().build();
    }

    // SKU 기반으로 재고를 조정(증가/차감)한다
    @PostMapping("/adjust")
    public ResponseEntity<Void> adjustStock(@RequestBody StockAdjustRequestDto dto) {
        inventoryService.adjustStockBySku(dto);
        return ResponseEntity.ok().build();
    }

    private Long resolveAccountId(SessionUser user) {
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }
}
