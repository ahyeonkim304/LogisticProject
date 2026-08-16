package com.logis.auth.dto;

import com.logis.auth.entity.Account;
import com.logis.auth.enums.AccountRole;
import com.logis.auth.enums.AccountStatus;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AccountResponseDto {

    private final Long id;
    private final String username;
    private final AccountRole role;
    private final AccountStatus status;
    private final String companyName;
    private final LocalDateTime createdAt;

    private AccountResponseDto(Account a) {
        this.id = a.getId();
        this.username = a.getUsername();
        this.role = a.getRole();
        this.status = a.getStatus();
        this.companyName = a.getCompanyName();
        this.createdAt = a.getCreatedAt();
    }

    public static AccountResponseDto from(Account a) {
        return new AccountResponseDto(a);
    }

    public String getStatusLabel() {
        if (status == null) return "";
        switch (status) {
            case PENDING:  return "승인 대기";
            case APPROVED: return "승인 완료";
            case REJECTED: return "승인 거부";
            default:       return status.name();
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case PENDING:  return "badge-inbound-pending";
            case APPROVED: return "badge-inbound-done";
            case REJECTED: return "badge-outbound-pending";
            default:       return "badge-secondary";
        }
    }

    public String getRoleLabel() {
        if (role == null) return "";
        return AccountRole.ADMIN == role ? "관리자" : "일반";
    }
}
