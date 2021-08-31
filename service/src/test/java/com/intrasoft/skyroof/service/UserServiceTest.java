package com.intrasoft.skyroof.service;

import com.intrasoft.skyroof.core.persistence.dao.UserDao;
import com.intrasoft.skyroof.core.security.JwtTokenUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = UserService.class)
public class UserServiceTest {

    @MockBean
    private UserDao userDaoMock;

    @MockBean
    private AuthenticationManager authenticationManagerMock;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private UserService userService;

    @Test(expected = ResponseStatusException.class)
    public void testWrongCredentialsLogin(){

        when(authenticationManagerMock.authenticate(any()))
                .thenThrow(new BadCredentialsException("Wrong details"));

        userService.login("admin", "admin");
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}