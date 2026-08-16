package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.location.LocationMetaDto;
import com.logis.wms.dto.location.LocationRequestDto;
import com.logis.wms.dto.location.LocationResponseDto;
import com.logis.wms.service.LocationService;
import javax.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // 로케이션 폼에 필요한 메타데이터(타입 목록·상위 후보·고객사 목록)를 반환한다
    @GetMapping("/meta")
    public ResponseEntity<LocationMetaDto> meta(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        LocationMetaDto result = locationService.getLocationMeta(accountId);
        return ResponseEntity.ok(result);
    }

    // 로케이션 전체 목록을 반환한다
    @GetMapping
    public ResponseEntity<List<LocationResponseDto>> getLocations(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        List<LocationResponseDto> result = locationService.getLocations(accountId);
        return ResponseEntity.ok(result);
    }

    // 새 로케이션을 생성한다
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody LocationRequestDto dto,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        locationService.createLocation(dto, accountId);
        return ResponseEntity.ok().build();
    }

    // 로케이션 정보를 수정한다
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody LocationRequestDto dto,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        locationService.updateLocation(id, dto, accountId);
        return ResponseEntity.ok().build();
    }

    // 로케이션 활성/비활성을 토글한다 (재고가 있으면 비활성화 불가)
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggle(
            @PathVariable Long id,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        locationService.toggleActive(id, accountId);
        return ResponseEntity.ok().build();
    }

    // 로케이션을 삭제한다 (재고가 있으면 삭제 불가)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        locationService.deleteLocation(id, accountId);
        return ResponseEntity.noContent().build();
    }

    // 관리자는 null(전체), 일반 사용자는 본인 accountId를 반환한다
    private Long resolveAccountId(SessionUser user) {
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }
}
