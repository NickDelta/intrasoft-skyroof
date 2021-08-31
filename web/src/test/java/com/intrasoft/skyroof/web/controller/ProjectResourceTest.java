package com.intrasoft.skyroof.web.controller;

import com.intrasoft.skyroof.core.persistence.model.Project;
import com.intrasoft.skyroof.core.persistence.model.State;
import com.intrasoft.skyroof.core.persistence.model.Task;
import com.intrasoft.skyroof.core.security.test.WithMockAdminUser;
import com.intrasoft.skyroof.core.security.test.WithMockConsultUser;
import com.intrasoft.skyroof.core.security.test.WithMockReporterUser;
import com.intrasoft.skyroof.service.ProjectService;
import com.intrasoft.skyroof.service.TaskService;
import com.intrasoft.skyroof.web.RestConfig;
import com.intrasoft.skyroof.web.dto.ProjectDTO;
import com.intrasoft.skyroof.misc.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc//(addFilters = false)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@SpringBootTest(classes = ProjectResource.class)
public class ProjectResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProjectService projectServiceMock;

    @MockBean
    private TaskService taskServiceMock;

    @Test
    @WithMockConsultUser
    public void testSuccessfulFindAll() throws Exception {

        Project project1 = new Project();
        project1.setId(1L);
        project1.setTitle("Title 1");
        project1.setDescription("Description 1");
        project1.setState(State.IN_PROGRESS);

        Project project2 = new Project();
        project2.setId(2L);
        project2.setTitle("Title 2");
        project2.setDescription("Description 2");
        project2.setState(State.COMPLETED);

        when(projectServiceMock.findAll()).thenReturn(Arrays.asList(project1,project2));

        mvc.perform(MockMvcRequestBuilders.get('/'+ RestConfig.API_URI + "/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Title 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Description 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].state").value("IN_PROGRESS"))

                .andExpect(MockMvcResultMatchers.jsonPath("$[1].title").value("Title 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("Description 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].state").value("COMPLETED"));

    }

    @Test
    @WithMockConsultUser
    public void testSuccessfulFindById() throws Exception {

        Project project = new Project();
        project.setId(1L);
        project.setTitle("Title 1");
        project.setDescription("Description 1");
        project.setState(State.IN_PROGRESS);

        when(projectServiceMock.findById(1L, true)).thenReturn(project);

        mvc.perform(get('/' + RestConfig.API_URI + "/projects/{id}",project.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Title 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Description 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("IN_PROGRESS"));
    }

    @Test
    @WithMockConsultUser
    public void testSuccessfulFindProjectTasks() throws Exception {

        Long projectId = 1L;

        mvc.perform(get('/'+ RestConfig.API_URI + "/projects/{id}/tasks", projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(taskServiceMock).findAllByProjectId(projectId);

    }

    @Test
    @WithMockAdminUser
    public void testSuccessfulSave() throws Exception {

        ProjectDTO project = new ProjectDTO();
        project.setTitle("Title");
        project.setDescription("Description");

        mvc.perform(post('/'+ RestConfig.API_URI + "/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(StringUtils.toJsonString(project)))
                .andExpect(status().isOk());

        verify(projectServiceMock).save(any());
    }

    @Test
    @WithMockReporterUser
    public void testSuccessfulUpdate() throws Exception {

        long projectId = 1L;

        ProjectDTO project = new ProjectDTO();
        project.setTitle("Title Changed");

        mvc.perform(put('/'+ RestConfig.API_URI + "/projects/{id}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(StringUtils.toJsonString(project)))
                .andExpect(status().isOk());

        verify(projectServiceMock).update(eq(projectId),any(Project.class));
    }

    @Test
    @WithMockAdminUser
    public void testSuccessfulDeleteById() throws Exception {

        long projectId = 1L;

        mvc.perform(delete('/'+ RestConfig.API_URI + "/projects/{id}",projectId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(projectServiceMock).delete(projectId);

    }

    @Test
    @WithMockAdminUser
    public void testSuccessfulAddCollaborator() throws Exception {

        long projectId = 1L;
        String username = "test";

        mvc.perform(post('/'+ RestConfig.API_URI + "/projects/{id}/collaborators/add?username={username}",projectId, username)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(projectServiceMock).addCollaborator(projectId, username);
    }

    @Test
    @WithMockAdminUser
    public void testSuccessfulDeleteCollaborator() throws Exception {

        long projectId = 1L;
        String username = "test";

        mvc.perform(delete('/'+ RestConfig.API_URI + "/projects/{id}/collaborators/delete?username={username}",projectId, username)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(projectServiceMock).deleteCollaborator(projectId, username);
    }

    @SpringBootApplication(scanBasePackages = "com.intrasoft.skyroof")
    static class TestConfiguration {
    }

}