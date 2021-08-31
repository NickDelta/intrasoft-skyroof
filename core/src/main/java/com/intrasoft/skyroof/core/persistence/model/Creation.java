package com.intrasoft.skyroof.core.persistence.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intrasoft.skyroof.core.persistence.util.LocalDateTimeDeserializer;
import com.intrasoft.skyroof.core.persistence.util.LocalDateTimeSerializer;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class Creation implements Serializable {

    @PrePersist
    public abstract void onPrePersist();

    @PreUpdate
    public abstract void onPreUpdate();

    @Column(name = "creation_date", nullable = false)
    protected LocalDateTime creationDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
