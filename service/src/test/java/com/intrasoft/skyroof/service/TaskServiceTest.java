package com.intrasoft.skyroof.service;

import com.intrasoft.skyroof.core.persistence.dao.TaskDao;
import com.intrasoft.skyroof.core.persistence.model.*;
import com.intrasoft.skyroof.core.security.test.WithMockCustomUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = TaskService.class)
public class TaskServiceTest {

    @MockBean
    private TaskDao taskDaoMock;

    @MockBean
    private ProjectService projectServiceMock;

    @MockBean
    private UserService userServiceMock;

    @Autowired
    private TaskService taskService;

    @Test(expected = ResponseStatusException.class)
    public void testFindByIdEntityNotFound() {
        when(taskDaoMock.findById(1L)).thenReturn(Optional.empty());
        taskService.findById(1L);
    }

    @Test(expected = ResponseStatusException.class)
    public void testSaveNoDeleteStateValidation() {
        Task task = new Task();
        task.setTitle("Title");
        task.setDescription("Description");
        task.setState(State.DELETED);
        taskService.save(task);
    }

    //TODO ADD PROJECT FIND VALIDATIONS

    @Test
    @WithMockCustomUser(id = 1L, username = "admin", role = Role.ROLE_ADMIN)
    public void testSuccessfulSave() {

        when(taskDaoMock.save(any(Task.class)))
                .thenAnswer((Answer<Task>) invocation -> invocation.getArgument(0));

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword("password");
        mockUser.setRole(Role.ROLE_ADMIN);

        when(userServiceMock.findByUsername("admin"))
                .thenReturn(mockUser);

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(mockUser);

        when(projectServiceMock.findById(1L, false)).thenReturn(project);

        Task task = new Task();
        task.setTitle("Title");
        task.setDescription("Description");
        task.setProject(project);
        task.setState(State.NOT_STARTED);

        Task saved = taskService.save(task);

        Assert.assertEquals("Title not same",task.getTitle(),saved.getTitle());
        Assert.assertEquals("Description not same",task.getDescription(),saved.getDescription());
        Assert.assertEquals("Project not same", project, task.getProject());
        Assert.assertEquals("State not same",task.getState(),saved.getState());
        Assert.assertEquals("Invalid creator",mockUser,saved.getCreator());

    }

    @Test(expected = ResponseStatusException.class)
    public void testUpdateNoDeleteValidation() {
        Task task = new Task();
        task.setTitle("Title");
        task.setDescription("Description");
        task.setState(State.DELETED);
        taskService.update(1L,task);
    }

    @Test(expected = ResponseStatusException.class)
    public void testUpdateValidStateValidation() {
        Task updatedTask = new Task();
        updatedTask.setTitle("Title");
        updatedTask.setDescription("Description");
        updatedTask.setState(State.NOT_STARTED);

        Task task = new Task();
        task.setTitle(updatedTask.getTitle());
        task.setDescription(task.getDescription());
        task.setState(State.IN_PROGRESS);

        when(taskDaoMock.findById(1L)).thenReturn(Optional.of(task));

        taskService.update(1L,updatedTask);
    }

    @Test
    public void testSuccessfulUpdate() {

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setState(State.IN_PROGRESS);

        when(taskDaoMock.findById(1L)).thenReturn(Optional.of(task));

        when(taskDaoMock.save(any(Task.class)))
                .thenAnswer((Answer<Task>) invocation -> invocation.getArgument(0));

        Task updatedTask = new Task();
        updatedTask.setTitle("Title 2");
        updatedTask.setState(State.COMPLETED);

        Task saved = taskService.update(1L, updatedTask);

        Assert.assertNotNull("Project is null", saved);
        Assert.assertEquals("Title field is not updated", updatedTask.getTitle(), saved.getTitle());
        Assert.assertEquals("State field is not updated", updatedTask.getState(), saved.getState());
        Assert.assertEquals("Description field has been falsely altered ", task.getDescription(), saved.getDescription());
    }

    @Test(expected = ResponseStatusException.class)
    public void testDeleteTaskNotInNotStartedState() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setState(State.IN_PROGRESS);

        when(taskDaoMock.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L);
    }

    @Test
    public void testSuccessfulDelete() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setState(State.NOT_STARTED);

        when(taskDaoMock.findById(1L)).thenReturn(Optional.of(task));
        when(taskDaoMock.save(any(Task.class)))
                .thenAnswer((Answer<Task>) invocation -> invocation.getArgument(0));

        Task deletedTask = taskService.delete(1L);
        Assert.assertEquals("Task state is not DELETED", State.DELETED, deletedTask.getState());
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}