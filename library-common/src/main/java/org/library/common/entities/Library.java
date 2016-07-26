package org.library.common.entities;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class Library {

    @Id
    private String id;

    private String path;
    private LocalDateTime updated;

    public Library() {
    }

    public Library(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Library{" +
                "path='" + path + '\'' +
                ", updated=" + updated +
                ", id='" + id + '\'' +
                '}';
    }
}
