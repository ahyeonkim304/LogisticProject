package com.logis.auth.enums;

public enum AccountStatus {
    PENDING,    // 가입 후 기본 상태 (승인 대기)
    APPROVED,   // 관리자 승인 완료 (로그인 가능)
    REJECTED    // 승인 거부 (로그인 불가)
}
