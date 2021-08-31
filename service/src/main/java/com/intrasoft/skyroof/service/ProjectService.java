package com.intrasoft.skyroof.service;


import com.intrasoft.skyroof.core.persistence.dao.ProjectDao;
import com.intrasoft.skyroof.core.persistence.model.Project;
import com.intrasoft.skyroof.core.persistence.model.State;
import com.intrasoft.skyroof.core.persistence.model.User;
import com.intrasoft.skyroof.misc.NullCaringBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private final Logger log = LoggerFactory.getLogger(ProjectService.class);

    public List<Project> findAll() {

        List<Project> projects = projectDao.findAll();
        projects.forEach(project -> project.setState(calculateProjectState(project)));
        log.debug("Fetched all project entities");
        return projects;
    }

    public Project findById(long id, boolean calculateState) {

        Project project = projectDao.findById(id).orElseThrow(() -> {
            log.debug("Project with id {} not found. Throwing exception with NOT_FOUND HttpStatus", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found.");
        });

        log.debug("Fetched project entity with id {}", id);

        if (calculateState) {
            log.debug("Calculating state for project with id {}...", id);
            project.setState(calculateProjectState(project));
        }

        return project;
    }

    public Project save(Project project) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userService.findByUsername(username);
        project.setOwner(user);
        project.addCollaborator(user);

        project = projectDao.save(project);
        log.debug("Project entity saved successfully");

        return project;
    }

    public Project update(long id, Project updatedProject) {

        Project project = findById(id, false);

        if (project.isDeleted()) {
            log.debug("Cannot delete on save validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot update project because it is marked as DELETED");
        }

        NullCaringBeanUtils.copyNonNullProperties(updatedProject, project);

        project = projectDao.save(project);
        log.debug("Project entity updated successfully");
        return project;
    }

    public Project delete(long id) {

        Project project = findById(id, false);

        if (taskService.existsUndeletedTask(project.getId())) {
            log.debug("Cannot delete project while it has undeleted tasks validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot mark project as deleted as there are still tasks which are not deleted");
        }

        project.setDeleted(true);
        project = projectDao.save(project);
        log.debug("Project with id {} soft deleted", project.getId());
        return project;
    }

    public Project addCollaborator(long projectId, String username) {

        Project project = findById(projectId, false);

        User user = userService.findByUsername(username);

        project.addCollaborator(user);
        return projectDao.save(project);
    }

    public Project deleteCollaborator(long projectId, String username) {

        Project project = findById(projectId, false);

        User user = userService.findByUsername(username);

        if(project.getOwner().getUsername().equals(username)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Owner cannot be removed from the collaborators list.");
        }

        if (!project.getCollaborators().contains(user)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Collaborator " + username + " does not exist on the project");
        }

        project.removeCollaborator(user);
        return projectDao.save(project);
    }

    public State calculateProjectState(Project project) {

        if (project.isDeleted()) {
            log.debug("Project is marked as deleted. Returning DELETED");
            return State.DELETED;
        }

        List<State> states = taskService.findAllUndeletedTaskStatesByProjectId(project.getId());
        log.debug("Fetched all undeleted tasks of project {}. Their states are: {}", project.getId(), states);

        //Let's be more explicit in this although allMatch()
        //in NOT_STARTED would have produced the same effect
        if (states.isEmpty()) {
            log.debug("Project {} does not have any tasks. Returning NOT_STARTED", project.getId());
            return State.NOT_STARTED;
        }

        if (states.stream().allMatch(state -> state == State.NOT_STARTED)) {
            log.debug("Project {} has every task in NOT_STARTED state. Returning NOT_STARTED", project.getId());
            return State.NOT_STARTED;
        } else if (states.stream().anyMatch(state -> state == State.IN_PROGRESS)) {
            log.debug("Project {} has at least 1 task in IN_PROGRESS state. Returning IN_PROGRESS", project.getId());
            return State.IN_PROGRESS;
        } else if (states.stream().allMatch(state -> state == State.COMPLETED)) {
            log.debug("Project {} has every task in COMPLETED state. Returning COMPLETED", project.getId());
            return State.COMPLETED;
        } else { //Case where some tasks are NOT_STARTED and some others are COMPLETED
            log.debug("Project {} has some tasks in NOT_STARTED and some others in COMPLETED states. Returning IN_PROGRESS", project.getId());
            return State.IN_PROGRESS;
        }
    }
}
