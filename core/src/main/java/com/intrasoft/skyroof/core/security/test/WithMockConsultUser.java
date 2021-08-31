package com.intrasoft.skyroof.core.security.test;

import com.intrasoft.skyroof.core.persistence.model.Role;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockCustomUser(id = 3L, username = "consult", role = Role.ROLE_CONSULT)
public @interface WithMockConsultUser {
}
