package com.logis.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountCreateDto {

    private String username;
    private String password;
    private String companyName;
    private String role   = "CLIENT";   // 기본값: 일반 고객
    private String status = "APPROVED"; // 기본값: 승인 완료 (관리자가 직접 생성)
}
