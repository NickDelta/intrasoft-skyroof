package com.intrasoft.skyroof.web.controller;

import com.intrasoft.skyroof.web.dto.ProjectDTO;
import com.intrasoft.skyroof.misc.NullCaringBeanUtils;
import com.intrasoft.skyroof.core.persistence.model.Project;
import com.intrasoft.skyroof.core.persistence.model.Task;
import com.intrasoft.skyroof.web.RestConfig;
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

import java.util.List;

import static java.util.Objects.isNull;

@RestController
@CrossOrigin
@RequestMapping(path = RestConfig.API_URI + "/projects")
public class ProjectResource {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    private final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CONSULT')")
    public ResponseEntity<List<Project>> findAll() {
        log.debug("Projects findAll() endpoint called");
        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping(path = "/{projectId}")
    @PreAuthorize("hasRole('ROLE_CONSULT')")
    public ResponseEntity<Project> findById(@PathVariable Long projectId) {
        log.debug("Projects findById() endpoint called for id: {}",projectId);
        return ResponseEntity.ok(projectService.findById(projectId,true));
    }

    @GetMapping(path = "/{projectId}/tasks")
    @PreAuthorize("hasRole('ROLE_CONSULT')")
    public ResponseEntity<List<Task>> findProjectTasks(@PathVariable Long projectId) {
        log.debug("Projects findProjectTask() endpoint called for id: {}",projectId);
        return ResponseEntity.ok(taskService.findAllByProjectId(projectId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Project> create(@RequestBody ProjectDTO projectDTO) {
        log.debug("Projects create() endpoint called for DTO: {}", projectDTO.toString());

        if(isNull(projectDTO.getTitle())){
            log.debug("Title null validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title field is required");
        }

        Project project = new Project();
        BeanUtils.copyProperties(projectDTO,project);

        return ResponseEntity.ok(projectService.save(project));
    }

    @PutMapping(path = "/{projectId}")
    @PreAuthorize("hasRole('ROLE_REPORTER')")
    public ResponseEntity<Project> update(@PathVariable Long projectId, @RequestBody ProjectDTO projectDTO) {
        log.debug("Projects update() endpoint called for id: {} DTO: {}", projectId, projectDTO.toString());

        Project updatedProject = new Project();
        NullCaringBeanUtils.copyNonNullProperties(projectDTO, updatedProject);

        return ResponseEntity.ok(projectService.update(projectId,updatedProject));
    }

    @DeleteMapping(path = "/{projectId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Project> deleteById(@PathVariable Long projectId) {
        log.debug("Project delete() endpoint called for id: {}", projectId);
        return ResponseEntity.ok(projectService.delete(projectId));
    }

    @PostMapping(path = "/{projectId}/collaborators/add")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addCollaborator(@PathVariable Long projectId, @RequestParam String username){
        Project project = projectService.addCollaborator(projectId, username);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping(path = "/{projectId}/collaborators/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCollaborator(@PathVariable Long projectId, @RequestParam String username){
        Project project = projectService.deleteCollaborator(projectId, username);
        return ResponseEntity.ok(project);
    }

}
