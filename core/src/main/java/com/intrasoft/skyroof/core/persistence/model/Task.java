package com.intrasoft.skyroof.core.persistence.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intrasoft.skyroof.core.persistence.util.LocalDateTimeDeserializer;
import com.intrasoft.skyroof.core.persistence.util.LocalDateTimeSerializer;
import org.hibernate.envers.Audited;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "task")
@Audited(targetAuditMode = NOT_AUDITED)
@JsonPropertyOrder({ "id", "title", "description", "state",
        "creation_date", "start_date", "completed_date",
        "project", "creator", "collaborators" }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task extends Creation{

    @Override
    public void onPrePersist(){
        if(state == null){
            state = State.NOT_STARTED;
        }

        if(creationDate == null){
            creationDate = LocalDateTime.now();
        }

        if(state == State.IN_PROGRESS){
            startDate = LocalDateTime.now();
            if(completedDate != null){
                completedDate = null;
            }
        }

        if(state == State.COMPLETED){
            completedDate = LocalDateTime.now();
        }

    }

    @Override
    public void onPreUpdate(){
        if(state == null){
            state = State.NOT_STARTED;
        }

        if(state == State.IN_PROGRESS){
            startDate = LocalDateTime.now();
            if(completedDate != null){
                completedDate = null;
            }
        }

        if(state == State.COMPLETED){
            completedDate = LocalDateTime.now();
        }
    }

    @Id
    @Column(name = "task_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_id_sequence")
    @SequenceGenerator(name = "task_id_sequence", sequenceName = "task_id_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "state", length = 50)
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getStartDate() {
        return startDate;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
