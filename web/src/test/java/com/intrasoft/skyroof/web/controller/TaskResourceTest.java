package com.intrasoft.skyroof.web.controller;

import com.intrasoft.skyroof.core.security.test.WithMockAdminUser;
import com.intrasoft.skyroof.core.security.test.WithMockConsultUser;
import com.intrasoft.skyroof.core.security.test.WithMockReporterUser;
import com.intrasoft.skyroof.service.TaskService;
import com.intrasoft.skyroof.web.RestConfig;
import com.intrasoft.skyroof.web.dto.TaskDTO;
import com.intrasoft.skyroof.misc.StringUtils;
import com.intrasoft.skyroof.core.persistence.model.State;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
@SpringBootTest(classes = TaskResource.class)
public class TaskResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TaskService taskServiceMock;

    @Test
    @WithMockConsultUser
    public void testSuccessfulFindAll() throws Exception {

        mvc.perform(MockMvcRequestBuilders.get('/'+ RestConfig.API_URI + "/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(taskServiceMock).findAll();
    }

    @Test
    @WithMockConsultUser
    public void testSuccessfulFindById() throws Exception {

        mvc.perform(get('/' + RestConfig.API_URI + "/tasks/{id}",1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(taskServiceMock).findById(1L);
    }

    @Test
    @WithMockReporterUser
    public void testSuccessfulSave() throws Exception {

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Title");
        taskDTO.setDescription("Description");
        taskDTO.setProjectId(1L);
        taskDTO.setState(State.NOT_STARTED);

        mvc.perform(post('/'+ RestConfig.API_URI + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(StringUtils.toJsonString(taskDTO)))
                .andExpect(status().isOk());

        verify(taskServiceMock).save(any());
    }

    @Test
    @WithMockReporterUser
    public void testSuccessfulUpdate() throws Exception {

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("New title");
        taskDTO.setState(State.IN_PROGRESS);

        mvc.perform(put('/'+ RestConfig.API_URI + "/tasks/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(StringUtils.toJsonString(taskDTO)))
                .andExpect(status().isOk());

        verify(taskServiceMock).update(eq(1L), any());
    }

    @Test
    @WithMockAdminUser
    public void testSuccessfulDeleteById() throws Exception {

        mvc.perform(delete('/'+ RestConfig.API_URI + "/tasks/{id}",1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(taskServiceMock).delete(1L);

    }

    @SpringBootApplication(scanBasePackages = "com.intrasoft.skyroof")
    static class TestConfiguration {
    }

}