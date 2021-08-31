package com.intrasoft.skyroof.core.persistence.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "project")
@Audited(targetAuditMode = NOT_AUDITED)
@JsonPropertyOrder({ "id", "title", "description",
        "creation_date", "state", "owner", "collaborators" }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project extends Creation {

    @Override
    public void onPrePersist(){

        if(creationDate == null){
            creationDate = LocalDateTime.now();
        }

    }

    @Override
    public void onPreUpdate() {

    }

    @Id
    @Column(name = "project_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_id_sequence")
    @SequenceGenerator(name = "project_id_sequence", sequenceName = "project_id_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(name = "project_collaborators",
            joinColumns = { @JoinColumn(name = "fk_project") },
            inverseJoinColumns = { @JoinColumn(name = "fk_user") })
    private Set<User> collaborators = new HashSet<>();

    @Transient
    private State state;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long projectId) {
        this.id = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonFilter("userShowOnlyUsernameFilter")
    public User getOwner() {
        return owner;
    }

    public void setOwner(User creator) {
        this.owner = creator;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @JsonFilter("userShowOnlyUsernameFilter")
    public Set<User> getCollaborators() {
        return collaborators;
    }

    public void addCollaborator(User user) {
        this.collaborators.add(user);
        user.getProjects().add(this);
    }

    public void removeCollaborator(User user) {
        this.collaborators.remove(user);
        user.getProjects().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
