package com.intrasoft.skyroof.web.controller;

import com.intrasoft.skyroof.web.RestConfig;
import com.intrasoft.skyroof.web.dto.TaskDTO;
import com.intrasoft.skyroof.core.persistence.model.Project;
import com.intrasoft.skyroof.core.persistence.model.Task;
import com.intrasoft.skyroof.service.ProjectService;
import com.intrasoft.skyroof.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;

import static java.util.Objects.isNull;

@RestController
@CrossOrigin
@RequestMapping(path = RestConfig.API_URI + "/tasks")
@Transactional
public class TaskResource {

    @Autowired
    private TaskService taskService;

    private final Logger log = LoggerFactory.getLogger(TaskResource.class);

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CONSULT')")
    public ResponseEntity<List<Task>> findAll() {
        log.debug("Tasks findAll() endpoint called");
        return ResponseEntity.ok(taskService.findAll());
    }

    @GetMapping(path = "/{taskId}")
    @PreAuthorize("hasRole('ROLE_CONSULT')")
    public ResponseEntity<Task> findById(@PathVariable Long taskId) {
        log.debug("Tasks findById() endpoint called for id: {}",taskId);
        return ResponseEntity.ok(taskService.findById(taskId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_REPORTER')")
    public ResponseEntity<Task> create(@RequestBody TaskDTO taskDTO) {
        log.debug("Tasks create() endpoint called for DTO: {}", taskDTO.toString());

        if(isNull(taskDTO.getTitle())){
            log.debug("Title null validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is a required field");
        }

        Task task = new Task();
        BeanUtils.copyProperties(taskDTO,task,"projectId");

        if (taskDTO.getProjectId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task must be assigned to a project");
        }

        //Assigning project id to a fake project
        Project project = new Project();
        project.setId(taskDTO.getProjectId());
        task.setProject(project);

        return ResponseEntity.ok(taskService.save(task));
    }

    @PutMapping(path = "/{taskId}")
    @PreAuthorize("hasRole('ROLE_REPORTER')")
    public ResponseEntity<Task> update(@PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {

        log.debug("Tasks update() endpoint called for id: {} DTO: {}", taskId, taskDTO.toString());
        Task task = new Task();
        BeanUtils.copyProperties(taskDTO,task,"projectId");

        if(taskDTO.getProjectId() != null){
            //Assigning project id to a fake project
            Project project = new Project();
            project.setId(taskDTO.getProjectId());
            task.setProject(project);
        }

        return ResponseEntity.ok(taskService.update(taskId, task));
    }

    @DeleteMapping(path = "/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Task> deleteById(@PathVariable Long taskId) {
        log.debug("Tasks delete() endpoint called for id: {}", taskId);
        return ResponseEntity.ok(taskService.delete(taskId));
    }

}
