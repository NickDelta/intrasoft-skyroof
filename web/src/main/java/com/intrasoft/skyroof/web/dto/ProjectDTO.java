package com.intrasoft.skyroof.web.dto;

public class ProjectDTO {

    private String title;
    private String description;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
