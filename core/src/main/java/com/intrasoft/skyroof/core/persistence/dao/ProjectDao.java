package com.intrasoft.skyroof.core.persistence.dao;

import com.intrasoft.skyroof.core.persistence.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectDao extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE ?#{authentication.principal.user} MEMBER OF p.collaborators")
    List<Project> findAll();

    @Query("SELECT p FROM Project p WHERE p.id = :id AND " +
            "?#{authentication.principal.user} MEMBER OF p.collaborators")
    Optional<Project> findById(Long id);
}
