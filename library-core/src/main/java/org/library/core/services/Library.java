package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.DataStatus;
import org.library.core.exceptions.LibraryDatabaseException;
import org.library.common.entities.FileInfo;
import org.library.entities.LibraryMetadata;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hub for library metadata
 */
public class Library implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(Library.class);
    private final LibraryMetadata libraryMetadata;
    private transient final DataService dataService;

    private DataStatus dataStatus;
    private transient AtomicInteger refreshItemsCount = new AtomicInteger(0);
    private transient AtomicInteger refreshProceedCount = new AtomicInteger(0);
    private transient AtomicInteger refreshUpdatedCount = new AtomicInteger(0);

    Library(DataService dataService, LibraryMetadata libraryMetadata) {
        this.libraryMetadata = libraryMetadata;
        this.dataService = dataService;
        this.dataStatus = DataStatus.INITIALIZING;
    }

    void initLibrary() {
        try {
            dataService.prepareDatabase();
            populateMetadata();
            setDataStatus(DataStatus.IDLE);
        } catch (LibraryDatabaseException e) {
            LOGGER.error("initLibrary error", e);
        }
    }

    LibraryMetadata getLibraryMetadata() {
        return libraryMetadata;
    }

    DataService getDataService() {
        return dataService;
    }

    void populateMetadata() {
        libraryMetadata.setItemsCount(dataService.getFileInfoCount());
        libraryMetadata.setLastUpdateDate(dataService.getLastUpdateDate());
        libraryMetadata.setLastRefreshDate(dataService.getLastRefreshDate());

    }

    boolean setDataStatus(DataStatus dataStatus) {
        this.dataStatus = dataStatus;
        return true;
    }

    DataStatus getDataStatus() {
        return dataStatus;
    }

    @Override
    public String toString() {
        return "Library{" +
                "libraryMetadata=" + libraryMetadata +
                ", dataStatus=" + dataStatus +
                '}';
    }

    public Integer getItemsCount() {
        return libraryMetadata.getItemsCount();
    }

    public LocalDateTime getLastUpdateDate() {
        return libraryMetadata.getLastUpdateDate();
    }

    public LocalDateTime getRefreshDate() {
        return libraryMetadata.getLastRefreshDate();
    }

    Integer getRefreshItemsCount() {
        return refreshItemsCount.get();
    }

    Integer getRefreshProceedCount() {
        return refreshProceedCount.get();
    }

    Integer getRefreshUpdatedCount() {
        return refreshUpdatedCount.get();
    }

    public String getPath() {
        return libraryMetadata.getPath();
    }

    public String getUUID() {
        return libraryMetadata.getUuid();
    }

    boolean checkAndSetDataStatus(DataStatus dataStatus, List<DataStatus> allowedDataStatuses) {
        boolean result = false;
        if ((allowedDataStatuses == null || allowedDataStatuses.contains(this.dataStatus))) {
            result = setDataStatus(dataStatus);
        }
        return result;
    }

    void setRefreshItemsCount(int refreshItemsCount) {
        this.refreshItemsCount.set(refreshItemsCount);
    }

    void resetRefreshProceedCount() {
        refreshProceedCount.set(0);
    }

    void resetRefreshUpdatedCount() {
        refreshUpdatedCount.set(0);
    }

    /**
     * Throws exception to inform invoker that operation was unsuccessful
     *
     * @return List of file info
     * @throws LibraryDatabaseException
     */
    List<FileInfo> getFileInfoList() throws LibraryDatabaseException {
        List<FileInfo> result;
        try {
            result = getDataService().getFileInfoList();
        } catch (LibraryDatabaseException e) {
            LOGGER.debug("getFileInfoList was unsuccessful", e);
            throw e;
        }
        return result;

    }

    void incrementRefreshProceedCount() {
        refreshProceedCount.incrementAndGet();
    }

    void incrementRefreshUpdatedCount() {
        refreshUpdatedCount.incrementAndGet();
    }

    void insertFileInfo(FileInfo fileInfo) {
        dataService.insertFileInfo(fileInfo);
    }

    void updateFileInfo(FileInfo fileInfo) {
        dataService.updateFileInfo(fileInfo);
    }

    public String getName() {
        return libraryMetadata.getName();
    }
}
