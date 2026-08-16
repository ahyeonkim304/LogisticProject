package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.dashboard.DashboardDto;
import com.logis.wms.service.DashboardService;
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

    // 주문 현황·재고 통계·최근 입출고 로그를 포함한 대시보드 데이터를 반환한다
    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = (user != null && !user.isAdmin()) ? user.getId() : null;
        return ResponseEntity.ok(dashboardService.getDashboard(accountId));
    }
}
