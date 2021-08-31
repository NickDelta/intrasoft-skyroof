package com.intrasoft.skyroof.core.persistence.dao;

import com.intrasoft.skyroof.core.persistence.model.*;
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
@Sql("test_projects.sql")
@Transactional //Rollback transactions after each test
@SpringBootTest
public class TaskDaoTest {

    @Autowired
    private TaskDao taskDao;

    @Test
    public void testSaveAndUpdate(){

        //Trying to avoid injecting ProjectRepository
        Project project = new Project();
        project.setId(1L);

        User adminUser = new User();
        adminUser.setId(1L);

        Task task = new Task();
        task.setTitle("Title");
        task.setDescription("Description");
        task.setProject(project);
        task.setCreator(adminUser);

        Task saved = taskDao.save(task);

        Assert.assertNotNull("Title field not persisted", saved.getTitle());
        Assert.assertNotNull("Description field not persisted", saved.getDescription());
        Assert.assertNotNull("Project field not persisted", saved.getProject());
        Assert.assertEquals("State is different than NOT_STARTED", State.NOT_STARTED, saved.getState()); // Testing @PrePresist initialization to NOT_STARTED
        Assert.assertNotNull("Creation Date is null", task.getCreationDate());
        Assert.assertNotNull("Creator is null", task.getCreator());

        saved.setDescription("Description 2");
        saved = taskDao.save(task);

        Assert.assertEquals("Description not updated", "Description 2", saved.getDescription());
    }

    @Test
    @WithMockCustomUser(id = 2L, username = "reporter", role = Role.ROLE_REPORTER)
    public void testFindAll(){

        //This project has admin,reporter as collaborators
        Project project1 = new Project();
        project1.setId(1L);

        //This project has admin,consult as collaborators
        Project project2 = new Project();
        project2.setId(2L);

        // Below 2 tasks will belong to a project where reporter is a collaborator

        Task task1 = new Task();
        task1.setTitle("Title 1");
        task1.setDescription("Description 1 ");
        task1.setProject(project1);
        taskDao.save(task1);

        Task task2 = new Task();
        task2.setTitle("Title 2");
        task2.setDescription("Description 2");
        task2.setProject(project1);
        taskDao.save(task2);

        // Below task will belong to a project where reporter IS NOT a collaborator
        // It must NOT be returned

        Task task3 = new Task();
        task3.setTitle("Title 3");
        task3.setDescription("Description 3");
        task3.setProject(project2);
        taskDao.save(task3);


        List<Task> found = taskDao.findAll();
        Assert.assertEquals("Incorrect number of projects returned",2, found.size());
        //TODO ADD ID CHECKS
    }

    @Test
    @WithMockCustomUser(id = 2L, username = "reporter", role = Role.ROLE_REPORTER)
    public void testFindById(){

        //This project has admin,reporter as collaborators
        Project project1 = new Project();
        project1.setId(1L);

        // Below task will belong to a project where reporter is a collaborator

        Task task1 = new Task();
        task1.setTitle("Title 1");
        task1.setDescription("Description 1 ");
        task1.setProject(project1);
        taskDao.save(task1);

        taskDao.findById(task1.getId()).orElseThrow(() ->
                new IllegalStateException("Task not persisted"));

    }

    @Test
    @WithMockCustomUser(id = 1L, username = "admin", role = Role.ROLE_ADMIN)
    public void testFindAllByProjectId() {

        //These 2 projects has admin,reporter as collaborators
        Project project1 = new Project();
        project1.setId(1L);

        Project project3 = new Project();
        project3.setId(3L);

        Task task1 = new Task();
        task1.setTitle("Title 1");
        task1.setDescription("Description 1");
        task1.setProject(project1);
        task1.setState(State.NOT_STARTED);
        taskDao.save(task1);

        Task task2 = new Task();
        task2.setTitle("Title 2");
        task2.setDescription("Description 2");
        task2.setProject(project1);
        task1.setState(State.IN_PROGRESS);
        taskDao.save(task2);

        Task task3 = new Task();
        task3.setTitle("Title 3");
        task3.setDescription("Description 3");
        task3.setProject(project3);
        task1.setState(State.COMPLETED);
        taskDao.save(task3);

        Assert.assertEquals("Incorrect number of tasks returned",2, taskDao.findAllByProjectId(project1.getId()).size());
        Assert.assertEquals("Incorrect number of tasks returned",1, taskDao.findAllByProjectId(project3.getId()).size());
    }

    @Test
    public void testFindAllUndeletedStatesByProjectId() {

        Project project1 = new Project();
        project1.setId(1L);

        Task task1 = new Task();
        task1.setTitle("Title 1");
        task1.setDescription("Description 1");
        task1.setProject(project1);
        task1.setState(State.DELETED);
        task1 = taskDao.save(task1);

        Task task2 = new Task();
        task2.setTitle("Title 2");
        task2.setDescription("Description 2");
        task2.setProject(project1);
        task2.setState(State.NOT_STARTED);
        task2 = taskDao.save(task2);

        Task task3 = new Task();
        task3.setTitle("Title 3");
        task3.setDescription("Description 3");
        task3.setProject(project1);
        task3.setState(State.DELETED);
        task3 = taskDao.save(task3);

        List<State> found = taskDao.findAllUndeletedTaskStatesByProjectId(project1.getId());

        Assert.assertEquals("Incorrect number of tasks returned",1, found.size());
        Assert.assertSame("Invalid task state found", task2.getState(), found.get(0));
    }

    @Test
    public void testExistsUndeletedProject() {

        //Trying to avoid injecting ProjectRepository
        Project project1 = new Project();
        project1.setId(1L);

        Task task1 = new Task();
        task1.setTitle("Title 1");
        task1.setDescription("Description 1");
        task1.setProject(project1);
        task1.setState(State.DELETED);
        task1 = taskDao.save(task1);

        Task task2 = new Task();
        task2.setTitle("Title 2");
        task2.setDescription("Description 2");
        task2.setProject(project1);
        task2.setState(State.NOT_STARTED);
        task2 = taskDao.save(task2);

        Task task3 = new Task();
        task3.setTitle("Title 3");
        task3.setDescription("Description 3");
        task3.setProject(project1);
        task3.setState(State.DELETED);
        task3 = taskDao.save(task3);

        boolean result1 = taskDao.existsUndeletedTask(1L);
        Assert.assertTrue("There is at least 1 project not in DELETED state, yet it returned false", result1);

        task2.setState(State.DELETED);
        task2 = taskDao.save(task2);

        boolean result2 = taskDao.existsUndeletedTask(1L);
        Assert.assertFalse("All projects are in DELETED state, yet it returned true", result2);
    }

    @SpringBootApplication
    @EntityScan("com.intrasoft.skyroof.core.persistence.model")
    static class TestConfiguration {
    }
}