package com.intrasoft.skyroof.core.security.test;

import com.intrasoft.skyroof.core.persistence.model.Role;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockCustomUser(id = 1L, username = "admin", role = Role.ROLE_ADMIN)
public @interface WithMockAdminUser {
}
