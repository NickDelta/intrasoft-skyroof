package com.intrasoft.skyroof.core.persistence.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_CONSULT,
    ROLE_REPORTER,
    ROLE_ADMIN;

    public String getAuthority() {
        return name();
    }

}