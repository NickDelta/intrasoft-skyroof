package com.intrasoft.skyroof.service;

import com.intrasoft.skyroof.core.persistence.model.Project;
import com.intrasoft.skyroof.misc.NullCaringBeanUtils;
import com.intrasoft.skyroof.core.persistence.dao.TaskDao;
import com.intrasoft.skyroof.core.persistence.model.State;
import com.intrasoft.skyroof.core.persistence.model.Task;
import com.intrasoft.skyroof.core.persistence.model.User;
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
public class TaskService {

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    private final Logger log = LoggerFactory.getLogger(TaskService.class);

    public List<Task> findAll(){
        List<Task> tasks = taskDao.findAll();
        log.debug("All tasks fetched");
        return tasks;
    }

    public Task findById(Long id){
        Task task = taskDao.findById(id).orElseThrow(() ->{
            log.debug("Task with id {} not found", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with id " + id + " not found");
        });
        log.debug("Task with id {} found", id);
        return task;
    }

    public List<Task> findAllByProjectId(Long projectId){
        //TODO Put an exists here
        List<Task> tasks = taskDao.findAllByProjectId(projectId);
        log.debug("Fetched all tasks of project with id {}", projectId);
        return tasks;
    }

    public Task save(Task task){

        if(task.getState() == State.DELETED){
            log.debug("Cannot delete a project on save validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A new task cannot be initiated in DELETE state");
        }

        try {
            Project project = projectService.findById(task.getProject().getId(), false);
            log.debug("Associating task with project {}", project.getId());
            task.setProject(project);
        } catch (ResponseStatusException ex){ // We actually want a BAD_REQUEST instead of NOT_FOUND
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getReason());
        }

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userService.findByUsername(username);
        task.setCreator(user);

        task = taskDao.save(task);
        log.debug("Task entity saved successfully");
        return task;
    }

    public Task update(Long id, Task updatedTask){

        if(updatedTask.getState() == State.DELETED){
            log.debug("Cannot delete a project on update validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Please use the DELETE endpoint to delete the task");
        }

        Task task = findById(id);

        if(updatedTask.getState() == State.NOT_STARTED &&
                (task.getState() == State.IN_PROGRESS || task.getState() == State.COMPLETED)){
            log.debug("Cannot rollback to NOT_STARTED validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A task cannot go back to NOT_STARTED state since it has been in IN_PROGRESS or COMPLETED state.");
        }

        if(task.getProject() != null && updatedTask.getProject() != null
                && !updatedTask.getProject().equals(task.getProject())){
            log.debug("Cannot assign task to another project validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A task cannot be assigned to another project since it's first assignment");
        }

        if(task.getProject() != null){
            try{
                Project project = projectService.findById(task.getProject().getId(), false);
                log.debug("Associating task with project {}", project.getId());
                task.setProject(project);
            } catch (ResponseStatusException ex){ // We actually want a BAD_REQUEST instead of NOT_FOUND
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getReason());
            }
        }

        NullCaringBeanUtils.copyNonNullProperties(updatedTask,task);

        task = taskDao.save(task);
        log.debug("Task entity with id {} updated",task.getId());
        return task;
    }

    public Task delete(Long id){

        Task task = findById(id);

        if(task.getState() != State.NOT_STARTED){
            log.debug("Cannot delete a non NOT_STARTED task validation triggered");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot mark task as DELETED because it is not in NOT_STARTED state");
        }

        task.setState(State.DELETED);
        task = taskDao.save(task);
        log.debug("Task entity with id {} soft deleted",task.getId());
        return task;
    }

    public List<State> findAllUndeletedTaskStatesByProjectId(Long projectId){
        return taskDao.findAllUndeletedTaskStatesByProjectId(projectId);
    }

    public boolean existsUndeletedTask(Long projectId){
        return taskDao.existsUndeletedTask(projectId);
    }

}
