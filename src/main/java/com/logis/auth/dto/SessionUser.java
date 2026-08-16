package com.logis.auth.dto;

import com.logis.auth.enums.AccountRole;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final AccountRole role;
    private final String companyName;

    public boolean isAdmin() {
        return AccountRole.ADMIN == role;
    }
}
