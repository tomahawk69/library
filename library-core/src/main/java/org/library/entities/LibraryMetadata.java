package org.library.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class LibraryMetadata {
    private final String path;
    private final String uuid  = UUID.randomUUID().toString();
    private LocalDateTime refreshDate;
    private int itemsCount;
    private LocalDateTime lastUpdateDate;
    private LocalDateTime lastRefreshDate;
    private String name;

    public LibraryMetadata(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getRefreshDate() {
        return refreshDate;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setRefreshDate(LocalDateTime refreshDate) {
        this.refreshDate = refreshDate;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LibraryMetadata that = (LibraryMetadata) o;

        return path.equals(that.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public LocalDateTime getLastRefreshDate() {
        return lastRefreshDate;
    }

    public void setLastRefreshDate(LocalDateTime lastRefreshDate) {
        this.lastRefreshDate = lastRefreshDate;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "LibraryMetadata{" +
                "path='" + path + '\'' +
                ", uuid='" + uuid + '\'' +
                ", refreshDate=" + refreshDate +
                ", itemsCount=" + itemsCount +
                ", lastUpdateDate=" + lastUpdateDate +
                ", lastRefreshDate=" + lastRefreshDate +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
