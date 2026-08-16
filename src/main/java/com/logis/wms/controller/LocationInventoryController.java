package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.location.LocationInventoryDetailDto;
import com.logis.wms.dto.location.LocationInventoryDetailDto.SkuRowDto;
import com.logis.wms.dto.location.LocationTreeNodeDto;
import com.logis.wms.service.LocationInventoryService;
import javax.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location-inventory")
@RequiredArgsConstructor
public class LocationInventoryController {

    private final LocationInventoryService locationInventoryService;

    // 로케이션 계층 트리와 각 노드의 재고 집계 데이터를 반환한다
    @GetMapping("/tree")
    public ResponseEntity<List<LocationTreeNodeDto>> tree(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        return ResponseEntity.ok(locationInventoryService.getLocationTree(accountId));
    }

    // 특정 로케이션과 하위 로케이션의 재고 상세를 조회한다
    @GetMapping("/location/{locationId}")
    public ResponseEntity<LocationInventoryDetailDto> detail(@PathVariable Long locationId,
                                                             HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        return ResponseEntity.ok(locationInventoryService.getLocationDetail(locationId, accountId));
    }

    // 전체 SKU 재고 목록을 반환한다
    @GetMapping("/all-skus")
    public ResponseEntity<List<SkuRowDto>> allSkus(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        return ResponseEntity.ok(locationInventoryService.getAllSkuList(accountId));
    }

    // SKU 또는 상품명 키워드로 재고를 검색한다
    @GetMapping("/search")
    public ResponseEntity<List<SkuRowDto>> search(@RequestParam String keyword, HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        return ResponseEntity.ok(locationInventoryService.searchBySku(keyword, accountId));
    }

    private Long resolveAccountId(SessionUser user) {
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }
}
