package org.library.entities;

import org.library.common.entities.DataStatus;
import org.library.core.services.DataService;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public class Library {
    private String path;
    private LocalDateTime refreshDate;
    private int itemsCount;
    private LocalDateTime lastUpdateDate;
    private DataStatus dataStatus;
    private final StampedLock dataStatusLock = new StampedLock();
    private AtomicInteger updatedCount = new AtomicInteger(0);
    private DataService dataService;

    public Library(DataService dataService, String path) {
        this.path = path;
        this.dataService = dataService;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Library{" +
                "path='" + path + '\'' +
                ", refreshDate=" + refreshDate +
                ", itemsCount=" + itemsCount +
                ", lastUpdateDate=" + lastUpdateDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Library library = (Library) o;

        return path.equals(library.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public DataStatus getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(DataStatus dataStatus) {
        this.dataStatus = dataStatus;
    }

    public Integer getUpdatedCount() {
        return updatedCount.get();
    }

    public void incrementUpdatedCount() {
        updatedCount.incrementAndGet();
    }

    public StampedLock getDataStatusLock() {
        return dataStatusLock;
    }

    public DataService getDataService() {
        return dataService;
    }
}
