package com.logis.auth.dto.response;

import com.logis.auth.dto.SessionUser;

public record UserResponse(
        Long id,
        String username,
        String role,
        String companyName,
        boolean admin
) {
    public static UserResponse from(SessionUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getCompanyName() != null ? user.getCompanyName() : "",
                user.isAdmin()
        );
    }
}
