package com.logis.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdateDto {

    private String companyName;

    /** AccountRole enum name: ADMIN / CLIENT */
    private String role;

    /** AccountStatus enum name: PENDING / APPROVED / REJECTED */
    private String status;
}
