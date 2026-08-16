package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.dashboard.DashboardDto;
import com.logis.wms.dto.dashboard.SafetyStockAlertDto;
import com.logis.wms.dto.dashboard.TopProductDto;
import com.logis.wms.dto.dashboard.ZoneCapacityDto;
import com.logis.wms.service.DashboardService;
import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(HttpSession session) {
        Long accountId = resolveAccountId(session);
        return ResponseEntity.ok(dashboardService.getDashboard(accountId));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDto>> getTopProducts(HttpSession session) {
        Long accountId = resolveAccountId(session);
        return ResponseEntity.ok(dashboardService.getTopOutboundProducts(accountId));
    }

    @GetMapping("/zone-capacity")
    public ResponseEntity<List<ZoneCapacityDto>> getZoneCapacity(HttpSession session) {
        Long accountId = resolveAccountId(session);
        return ResponseEntity.ok(dashboardService.getZoneCapacity(accountId));
    }

    @GetMapping("/safety-stock")
    public ResponseEntity<List<SafetyStockAlertDto>> getSafetyStock(HttpSession session) {
        Long accountId = resolveAccountId(session);
        return ResponseEntity.ok(dashboardService.getSafetyStockAlerts(accountId));
    }

    private Long resolveAccountId(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }
}
