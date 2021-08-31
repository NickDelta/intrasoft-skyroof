package com.intrasoft.skyroof.core.security.test;

import com.intrasoft.skyroof.core.persistence.model.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    long id() default 1L;
    String username() default "admin";
    Role role() default Role.ROLE_ADMIN;
}
