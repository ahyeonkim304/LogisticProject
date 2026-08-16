package com.logis.auth.controller;

import com.logis.auth.dto.AccountCreateDto;
import com.logis.auth.dto.AccountResponseDto;
import com.logis.auth.dto.AccountUpdateDto;
import com.logis.auth.dto.response.MessageResponse;
import com.logis.auth.service.AccountService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    // 계정 목록을 키워드·상태 필터와 페이징으로 조회한다
    @GetMapping
    public ResponseEntity<Page<AccountResponseDto>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<AccountResponseDto> result = accountService.getAccounts(keyword, status, pageable);
        return ResponseEntity.ok(result);
    }

    // 계정의 승인 상태를 변경한다 (PENDING / APPROVED / REJECTED)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            accountService.changeStatus(id, status);
            return ResponseEntity.ok(MessageResponse.of("상태가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }

    // 계정의 역할을 변경한다 (ADMIN / CLIENT)
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String role = body.get("role");
            accountService.changeRole(id, role);
            return ResponseEntity.ok(MessageResponse.of("역할이 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }

    // 관리자가 새 계정을 직접 생성한다 (기본 상태 APPROVED)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AccountCreateDto dto) {
        try {
            accountService.createAccount(dto);
            return ResponseEntity.ok(MessageResponse.of("계정이 생성되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }

    // 계정의 회사명·역할·상태를 수정한다
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody AccountUpdateDto dto) {
        try {
            accountService.updateAccount(id, dto);
            return ResponseEntity.ok(MessageResponse.of("계정이 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }

    // 계정을 소프트 삭제한다
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok(MessageResponse.of("계정이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }
}
