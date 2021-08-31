package com.intrasoft.skyroof.core.persistence.dao;

import com.intrasoft.skyroof.core.persistence.model.Project;
import com.intrasoft.skyroof.core.persistence.model.Role;
import com.intrasoft.skyroof.core.persistence.model.User;
import com.intrasoft.skyroof.core.security.test.WithMockCustomUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@Sql("test_users.sql")
@Transactional //Rollback transactions after each test
@SpringBootTest
public class ProjectDaoTest {

    @Autowired
    private ProjectDao projectDao;

    @Test
    public void testSaveAndUpdate(){

        User user = new User();
        user.setId(1L);

        Project project = new Project();
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(user);
        project.addCollaborator(user); // Simulating ProjectService - Adding the owner as collaborator
        Project found = projectDao.save(project);

        Assert.assertEquals("Title field not persisted", project.getTitle(), found.getTitle());
        Assert.assertEquals("Description field not persisted", project.getDescription(), found.getDescription());
        Assert.assertNotNull("Creation Date is null", project.getCreationDate());
        Assert.assertFalse("Task delete state is invalid", found.isDeleted());

        project.setDescription("New Description");
        found = projectDao.save(project);
        Assert.assertEquals("Description field not updated", project.getDescription(), found.getDescription());
    }

    @Test
    @WithMockCustomUser(id = 2L, username = "reporter", role = Role.ROLE_REPORTER)
    public void testFindById(){

        User adminUser = new User();
        adminUser.setId(1L);

        User reporterUser = new User();
        reporterUser.setId(2L);

        Project project = new Project();
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(adminUser);
        project.addCollaborator(adminUser); // Simulating ProjectService - Adding the owner as collaborator
        project.addCollaborator(reporterUser);
        projectDao.save(project);

        projectDao.findById(project.getId()).orElseThrow(() ->
                new IllegalStateException("Project shouldn't be null"));

    }

    @Test
    @WithMockCustomUser(id = 2L, username = "reporter", role = Role.ROLE_REPORTER)
    public void testFindAll(){

        User adminUser = new User();
        adminUser.setId(1L);

        User reporterUser = new User();
        reporterUser.setId(2L);


        Project project1 = new Project();
        project1.setTitle("Title 1");
        project1.setDescription("Description 1");
        project1.setOwner(adminUser);
        project1.addCollaborator(adminUser); // Simulating ProjectService - Adding the owner as collaborator
        project1.addCollaborator(reporterUser); //Adding an extra collaborator
        projectDao.save(project1);

        Project project2 = new Project();
        project2.setTitle("Title 2");
        project2.setDescription("Description 2");
        project2.setOwner(adminUser);
        project2.addCollaborator(adminUser); // Simulating ProjectService - Adding the owner as collaborator
        projectDao.save(project2);

        List<Project> found = projectDao.findAll();
        Assert.assertEquals("Incorrect number of projects returned",1, found.size());
    }

    @SpringBootApplication
    @EntityScan("com.intrasoft.skyroof.core.persistence.model")
    static class TestConfiguration {
    }

}