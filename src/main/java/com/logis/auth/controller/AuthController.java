package com.logis.auth.controller;

import com.logis.auth.dto.LoginDto;
import com.logis.auth.dto.RegisterDto;
import com.logis.auth.dto.SessionUser;
import com.logis.auth.dto.response.MessageResponse;
import com.logis.auth.dto.response.UserResponse;
import com.logis.auth.service.AccountService;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;

    // 현재 세션에 로그인된 사용자 정보를 반환한다
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // 로그인 처리 후 세션에 사용자 정보를 저장한다
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto, HttpSession session) {
        try {
            SessionUser user = accountService.login(dto.getUsername(), dto.getPassword());
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(60 * 60);
            return ResponseEntity.ok(UserResponse.from(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }

    // 세션을 무효화하여 로그아웃 처리한다
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(MessageResponse.of("로그아웃되었습니다."));
    }

    // 신규 회원가입을 처리한다 (가입 후 PENDING 상태로 관리자 승인 대기)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto dto) {
        try {
            accountService.register(dto);
            return ResponseEntity.ok(
                    MessageResponse.of("회원가입이 완료되었습니다. 관리자 승인 후 로그인 가능합니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(MessageResponse.of(e.getMessage()));
        }
    }
}
