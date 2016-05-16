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
    private DataService dataService;

    private final AtomicInteger refreshProceedCount = new AtomicInteger(0);
    private final AtomicInteger refreshUpdatedCount = new AtomicInteger(0);
    private final AtomicInteger refreshItemsCount = new AtomicInteger(0);
    private LocalDateTime lastRefreshDate;

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
                ", dataStatus=" + dataStatus +
                ", dataStatusLock=" + dataStatusLock +
                ", dataService=" + dataService +
                ", refreshProceedCount=" + refreshProceedCount +
                ", refreshUpdatedCount=" + refreshUpdatedCount +
                ", refreshItemsCount=" + refreshItemsCount +
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

    public LocalDateTime getRefreshDate() {
        return refreshDate;
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

    public Integer getRefreshProceedCount() {
        return refreshProceedCount.get();
    }

    public AtomicInteger getRefreshUpdatedCount() {
        return refreshUpdatedCount;
    }

    public AtomicInteger getRefreshItemsCount() {
        return refreshItemsCount;
    }

    public StampedLock getDataStatusLock() {
        return dataStatusLock;
    }

    public DataService getDataService() {
        return dataService;
    }

    public void incrementRefreshProceedCount() {
        refreshProceedCount.incrementAndGet();
    }
    public void resetRefreshProceedCount() {
        refreshProceedCount.set(0);
    }
    public void incrementRefreshUpdatedCount() {
        refreshUpdatedCount.incrementAndGet();
    }
    public void resetRefreshUpdatedCount() {
        refreshUpdatedCount.set(0);
    }
    public void setRefreshItemsCount(int value) {
        refreshItemsCount.set(value);
    }

    public void setLastRefreshDate(LocalDateTime lastRefreshDate) {
        this.lastRefreshDate = lastRefreshDate;
    }
}
