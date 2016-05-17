package org.library.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LibraryWeb implements Serializable {

    private String path;
    private String name;
    private LocalDateTime lastUpdateDate;
    private LocalDateTime lastRefreshDate;
    private Integer count;
    private String uuid;

    LibraryWeb setPath(String path) {
        this.path = path;
        return this;
    }

    LibraryWeb setName(String name) {
        this.name = name;
        return this;
    }

    LibraryWeb setLastUpdateDate(LocalDateTime date) {
        this.lastUpdateDate = date;
        return this;
    }

    LibraryWeb setLastRefreshDate(LocalDateTime date) {
        this.lastRefreshDate = date;
        return this;
    }

    LibraryWeb setItemsCount(Integer count) {
        this.count = count;
        return this;
    }

    LibraryWeb setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public LocalDateTime getLastRefreshDate() {
        return lastRefreshDate;
    }

    public Integer getCount() {
        return count;
    }

    public String getUuid() {
        return uuid;
    }
}
