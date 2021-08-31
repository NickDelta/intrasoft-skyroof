package com.intrasoft.skyroof.service;

import com.intrasoft.skyroof.core.persistence.dao.ProjectDao;
import com.intrasoft.skyroof.core.persistence.model.*;
import com.intrasoft.skyroof.core.security.test.WithMockCustomUser;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = ProjectService.class)
public class ProjectServiceTest {

    @MockBean
    private ProjectDao projectDaoMock;

    @MockBean
    private TaskService taskServiceMock;

    @MockBean
    private UserService userServiceMock;

    @Autowired
    private ProjectService projectService;

    @Test(expected = ResponseStatusException.class)
    public void testFindByIdEntityNotFound(){
        when(projectDaoMock.findById(1L)).thenReturn(Optional.empty());
        projectService.findById(1L,true);
    }

    @Test
    public void testFindById() {

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setProject(project);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setState(State.NOT_STARTED);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProject(project);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setState(State.COMPLETED);

        when(projectDaoMock.findById(1L)).thenReturn(Optional.of(project));
        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Arrays.asList(task1.getState(), task2.getState()));

        Project found = projectService.findById(1L,true);
        Assert.assertNotNull("Project is null", found);
        Assert.assertEquals("Invalid project state", State.IN_PROGRESS, found.getState());

    }

    @Test
    public void findAll() {

        Project project1 = new Project();
        project1.setId(1L);
        project1.setTitle("Title 1");
        project1.setDescription("Description 1");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setTitle("Title 2");
        project2.setDescription("Description 2");

        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        projects.add(project2);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setProject(project1);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setState(State.NOT_STARTED);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProject(project2);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setState(State.COMPLETED);

        when(projectDaoMock.findAll()).thenReturn(projects);
        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Collections.singletonList(task1.getState()));
        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(2L))
                .thenReturn(Collections.singletonList(task2.getState()));

        List<Project> found = projectService.findAll();

        Assert.assertEquals("Invalid number of items returned", 2, found.size());
        Assert.assertEquals("Invalid project state", State.NOT_STARTED, found.get(0).getState());
        Assert.assertEquals("Invalid project state", State.COMPLETED, found.get(1).getState());

    }

    @Test
    @WithMockCustomUser(id = 1L, username = "admin", role = Role.ROLE_ADMIN)
    public void testSuccessfulSave() {

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword("password");
        mockUser.setRole(Role.ROLE_ADMIN);

        when(userServiceMock.findByUsername("admin"))
                .thenReturn(mockUser);

        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        Project project = new Project();
        project.setTitle("Title");
        project.setDescription("Description");

        Project saved = projectService.save(project);

        Assert.assertNotNull("Project is null", saved);
        Assert.assertEquals("Title field is null", project.getTitle(), saved.getTitle());
        Assert.assertEquals("Description field is null", project.getDescription(), saved.getDescription());
        Assert.assertEquals("Owner not saved", "admin", saved.getOwner().getUsername());
        Assert.assertTrue("Owner also a collaborator not saved", saved.getCollaborators().contains(mockUser));
    }

    @Test(expected = ResponseStatusException.class)
    public void testUpdateProjectInDeletedState() {
        Project project = new Project();
        project.setTitle("Title 1");
        project.setDescription("Description 1");
        project.setDeleted(true);

        when(projectDaoMock.findById(1L)).thenReturn(Optional.of(project));

        Project updatedProject = new Project();
        updatedProject.setTitle("Title 2");

        projectService.update(1L, updatedProject);
    }

    @Test
    public void testSuccessfulUpdate() {

        Project project = new Project();
        project.setTitle("Title 1");
        project.setDescription("Description 1");

        when(projectDaoMock.findById(1L)).thenReturn(Optional.of(project));

        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        Project updatedProject = new Project();
        updatedProject.setTitle("Title 2");

        Project saved = projectService.update(1L, updatedProject);

        Assert.assertNotNull("Project is null", saved);
        Assert.assertEquals("Title field is not updated", updatedProject.getTitle(), saved.getTitle());
        Assert.assertEquals("Description field has been falsely altered ", project.getDescription(), saved.getDescription());
    }

    @Test(expected = ResponseStatusException.class)
    public void testDeleteNotAllTasksDeleted() {
        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");

        when(projectDaoMock.findById(1L)).thenReturn(Optional.of(project));

        Task task1 = new Task();
        task1.setId(1L);
        task1.setProject(project);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setState(State.DELETED);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setProject(project);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setState(State.NOT_STARTED);

        List<Task> tasks = new ArrayList<>();
        tasks.add(task1);
        tasks.add(task2);

        when(taskServiceMock.existsUndeletedTask(1L)).thenReturn(true);
        projectService.delete(1L);
    }

    @Test
    public void testSuccessfulDelete() {

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");

        when(projectDaoMock.findById(1L)).thenReturn(Optional.of(project));

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L)).thenReturn(Collections.emptyList());

        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        Project deletedProject = projectService.delete(1L);
        Assert.assertTrue("isDeleted is not true", deletedProject.isDeleted());

    }

    @Test
    public void testSuccessfulAddCollaborator(){

        User mockAdminUser = new User();
        mockAdminUser.setId(1L);
        mockAdminUser.setUsername("admin");
        mockAdminUser.setPassword("password");
        mockAdminUser.setRole(Role.ROLE_ADMIN);

        User mockTestUser = new User();
        mockTestUser.setId(2L);
        mockTestUser.setUsername("testUser");
        mockTestUser.setPassword("password");
        mockTestUser.setRole(Role.ROLE_REPORTER);

        when(userServiceMock.findByUsername("testUser"))
                .thenReturn(mockTestUser);

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(mockAdminUser);
        project.addCollaborator(mockAdminUser);

        when(projectDaoMock.findById(1L))
                .thenReturn(Optional.of(project));
        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        Project saved = projectService.addCollaborator(1L, "testUser");

        Assert.assertTrue("Collaborator not saved", saved.getCollaborators().contains(mockTestUser));

    }

    @Test(expected = ResponseStatusException.class)
    public void testDeleteCollaboratorCannotRemoveOwner(){

        User mockAdminUser = new User();
        mockAdminUser.setId(1L);
        mockAdminUser.setUsername("admin");
        mockAdminUser.setPassword("password");
        mockAdminUser.setRole(Role.ROLE_ADMIN);

        when(userServiceMock.findByUsername("admin"))
                .thenReturn(mockAdminUser);

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(mockAdminUser);
        project.addCollaborator(mockAdminUser);

        when(projectDaoMock.findById(1L))
                .thenReturn(Optional.of(project));
        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        projectService.deleteCollaborator(1L, "admin");
    }

    @Test(expected = ResponseStatusException.class)
    public void testDeleteCollaboratorUserNotACollaborator(){

        User mockAdminUser = new User();
        mockAdminUser.setId(1L);
        mockAdminUser.setUsername("admin");
        mockAdminUser.setPassword("password");
        mockAdminUser.setRole(Role.ROLE_ADMIN);

        User mockTestUser = new User();
        mockTestUser.setId(2L);
        mockTestUser.setUsername("testUser");
        mockTestUser.setPassword("password");
        mockTestUser.setRole(Role.ROLE_REPORTER);

        when(userServiceMock.findByUsername("testUser"))
                .thenReturn(mockTestUser);

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(mockAdminUser);
        project.addCollaborator(mockAdminUser);
        // We do not add testUser as a collaborator

        when(projectDaoMock.findById(1L))
                .thenReturn(Optional.of(project));
        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        projectService.deleteCollaborator(1L, "testUser");
    }

    @Test
    public void testSuccessfulDeleteCollaborator(){
        User mockAdminUser = new User();
        mockAdminUser.setId(1L);
        mockAdminUser.setUsername("admin");
        mockAdminUser.setPassword("password");
        mockAdminUser.setRole(Role.ROLE_ADMIN);

        User mockTestUser = new User();
        mockTestUser.setId(2L);
        mockTestUser.setUsername("testUser");
        mockTestUser.setPassword("password");
        mockTestUser.setRole(Role.ROLE_REPORTER);

        when(userServiceMock.findByUsername("testUser"))
                .thenReturn(mockTestUser);

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title");
        project.setDescription("Description");
        project.setOwner(mockAdminUser);
        project.addCollaborator(mockAdminUser);
        project.addCollaborator(mockTestUser);

        when(projectDaoMock.findById(1L))
                .thenReturn(Optional.of(project));
        when(projectDaoMock.save(any(Project.class)))
                .thenAnswer((Answer<Project>) invocation -> invocation.getArgument(0));

        Project saved = projectService.deleteCollaborator(1L, "testUser");

        Assert.assertTrue("Wrong user removed", saved.getCollaborators().contains(mockAdminUser));
        Assert.assertFalse("testUser not removed", saved.getCollaborators().contains(mockTestUser));
    }

    @Test
    public void testCalculateProjectState() {
        Project project = new Project();
        project.setId(1L);

        project.setDeleted(true);
        Assert.assertEquals("Invalid state: Should be DELETED",
                State.DELETED, projectService.calculateProjectState(project));

        //Reset deletion state for the rest of the test
        project.setDeleted(false);

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Lists.emptyList());

        Assert.assertEquals("Invalid state: Should be NOT_STARTED",
                State.NOT_STARTED, projectService.calculateProjectState(project));

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Arrays.asList(State.NOT_STARTED, State.NOT_STARTED));

        Assert.assertEquals("Invalid state: Should be NOT_STARTED",
                State.NOT_STARTED, projectService.calculateProjectState(project));

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Arrays.asList(State.NOT_STARTED, State.IN_PROGRESS));

        Assert.assertEquals("Invalid state: Should be IN_PROGRESS",
                State.IN_PROGRESS, projectService.calculateProjectState(project));

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Arrays.asList(State.IN_PROGRESS, State.COMPLETED));

        Assert.assertEquals("Invalid state: Should be IN_PROGRESS",
                State.IN_PROGRESS, projectService.calculateProjectState(project));

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Arrays.asList(State.COMPLETED, State.COMPLETED));

        Assert.assertEquals("Invalid state: Should be COMPLETED",
                State.COMPLETED, projectService.calculateProjectState(project));

        when(taskServiceMock.findAllUndeletedTaskStatesByProjectId(1L))
                .thenReturn(Arrays.asList(State.NOT_STARTED, State.COMPLETED));

        Assert.assertEquals("Invalid state: Should be IN_PROGRESS",
                State.IN_PROGRESS, projectService.calculateProjectState(project));

    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}