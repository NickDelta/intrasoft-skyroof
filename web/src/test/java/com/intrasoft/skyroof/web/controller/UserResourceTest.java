package com.intrasoft.skyroof.web.controller;

import com.intrasoft.skyroof.core.persistence.model.Role;
import com.intrasoft.skyroof.misc.StringUtils;
import com.intrasoft.skyroof.service.UserService;
import com.intrasoft.skyroof.web.RestConfig;
import com.intrasoft.skyroof.web.dto.LoginRequestDTO;
import com.intrasoft.skyroof.web.dto.SignUpRequestDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = UserResource.class)
public class UserResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userServiceMock;

    @Test
    public void login() throws Exception {

        LoginRequestDTO body = new LoginRequestDTO();
        body.setUsername("test");
        body.setPassword("test");

        mvc.perform(post('/' + RestConfig.API_URI + "/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(StringUtils.toJsonString(body)))
                .andExpect(status().isOk());

        verify(userServiceMock).login(body.getUsername(), body.getPassword());

    }

    @Test
    public void signUp() throws Exception {

        SignUpRequestDTO body = new SignUpRequestDTO();
        body.setUsername("test");
        body.setPassword("test");
        body.setRole(Role.ROLE_ADMIN);

        mvc.perform(post('/' + RestConfig.API_URI + "/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(StringUtils.toJsonString(body)))
                .andExpect(status().isOk());

        verify(userServiceMock).signup(body.getUsername(), body.getPassword(), body.getRole());
    }

    @SpringBootApplication(scanBasePackages = "com.intrasoft.skyroof")
    static class TestConfiguration {
    }
}