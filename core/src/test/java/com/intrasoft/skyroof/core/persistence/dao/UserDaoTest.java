package com.intrasoft.skyroof.core.persistence.dao;

import com.intrasoft.skyroof.core.persistence.model.Role;
import com.intrasoft.skyroof.core.persistence.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void testSaveAndFindByUsername(){

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_ADMIN);
        userDao.save(user);

        user = userDao.findByUsername("admin")
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Assert.assertEquals("Invalid username", "admin", user.getUsername());
        Assert.assertTrue("Invalid password hash", passwordEncoder.matches("password", user.getPassword()));
        Assert.assertEquals("Invalid role", Role.ROLE_ADMIN, user.getRole());
    }

    @SpringBootApplication
    @EntityScan("com.intrasoft.skyroof.core.persistence.model")
    static class TestConfiguration {
    }

}