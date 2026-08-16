package com.logis.common.config;

import com.logis.auth.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AccountService accountService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            accountService.createInitialAdmin("admin", "admin1234");
        } catch (Exception e) {
            log.warn("초기 관리자 계정 생성 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
        }
    }
}
