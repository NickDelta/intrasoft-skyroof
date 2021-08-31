package com.intrasoft.skyroof.core.persistence.dao;

import com.intrasoft.skyroof.core.persistence.model.State;
import com.intrasoft.skyroof.core.persistence.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskDao extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE ?#{authentication.principal.user} " +
            "MEMBER OF t.project.collaborators AND t.state <> 'DELETED'")
    List<Task> findAll();

    @Query("SELECT t FROM Task t WHERE t.id = :id " +
            "AND ?#{authentication.principal.user} MEMBER OF t.project.collaborators " +
            "AND t.state <> 'DELETED'")
    Optional<Task> findById(Long id);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
            "AND ?#{authentication.principal.user} MEMBER OF t.project.collaborators " +
            "AND t.state <> 'DELETED'")
    List<Task> findAllByProjectId(Long projectId);

    // Below methods are needed for internal calculations. Users must not use them as they are unsecured

    @Query("SELECT t.state FROM Task t WHERE t.project.id = :projectId AND t.state <> 'DELETED'")
    List<State> findAllUndeletedTaskStatesByProjectId(Long projectId);

    @Query(value = "SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Task t WHERE EXISTS (SELECT 1 FROM Task t1 WHERE " +
            "t1.project.id = t1.project.id AND t1.state <> 'DELETED')")
    boolean existsUndeletedTask(Long projectId);
}
