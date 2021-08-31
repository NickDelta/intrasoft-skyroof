package com.intrasoft.skyroof.core.security.test;

import com.intrasoft.skyroof.core.persistence.model.Role;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockCustomUser(id = 2L, username = "reporter", role = Role.ROLE_REPORTER)
public @interface WithMockReporterUser {
}
