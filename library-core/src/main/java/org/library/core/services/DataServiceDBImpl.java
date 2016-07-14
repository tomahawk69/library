package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.dao.DataStorage;
import org.library.core.exceptions.LibraryDatabaseException;
import org.library.core.utils.DateUtils;
import org.library.common.entities.FileInfo;
import org.library.entities.FileUpdateOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component()
public class DataServiceDBImpl extends AbstractDataService {
    private final DataStorage dataStorage;
    private static final Logger LOGGER = LogManager.getLogger(DataServiceDBImpl.class);

    @Autowired
    public DataServiceDBImpl(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    @Override
    public void insertFileInfo(FileInfo fileInfo) {
        if (fileInfo == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation insertOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.INSERT, fileInfo);
        addOperationToQueue(insertOperation);
    }

    @Override
    public void updateFileInfo(FileInfo fileInfoUpdated) {
        if (fileInfoUpdated == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation updateOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.UPDATE, fileInfoUpdated);
        addOperationToQueue(updateOperation);
    }

    @Override
    public void deleteFileInfo(FileInfo fileInfo) {
        if (fileInfo == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation modifyOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.DELETE, fileInfo);
        addOperationToQueue(modifyOperation);
    }

    @Override
    public FileInfo get(UUID uuid) {
        return null;
    }

    @Override
    public List<FileInfo> getFileInfoList() throws LibraryDatabaseException {
        return dataStorage.getFileInfoList();
    }

    @Override
    public List<FileUpdateOperation> commitFileInfo() {
        boolean hasChanged = queue.size() > 0;
        LOGGER.info("commitFileInfo started for " + queue.size());
        try {
            dataStorage.prepareBatch(false);
            for (FileUpdateOperation fileUpdateOperation : queue) {
                synchronized (fileUpdateOperation.getFileInfo()) {
                    switch (fileUpdateOperation.getUpdateType()) {
                        case INSERT:
                            dataStorage.batchInsertFileInfo(fileUpdateOperation.getFileInfo());
                            break;
                        case UPDATE:
                            dataStorage.batchUpdateFileInfo(fileUpdateOperation.getFileInfo());
                            break;
                        case DELETE:
                            dataStorage.batchDeleteFileInfo(fileUpdateOperation.getFileInfo());
                            break;
                        default:
                            throw new Exception("Unknown operation type: " + fileUpdateOperation.getUpdateType());
                    }
                }
                fileUpdateOperation.setSuccess();
            }
            dataStorage.commit();
            if (hasChanged) {
                updateLastUpdateDate(LocalDateTime.now());
            }
        } catch (Exception e) {
            LOGGER.error("commitFileInfo error", e);
        } finally {
            dataStorage.closeConnection();
        }
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        LOGGER.info("commitFileInfo done, failed " + result.size());
        return result;
    }

    @Override
    public List<FileUpdateOperation> rollbackFileInfo() {
        return getFailedOperationsAndClearQueue();
    }

    @Override
    public void setDatabasePath(Path libraryPath) {
        LOGGER.info("setDatabasePath to " + libraryPath);
        this.databasePath = libraryPath;
        dataStorage.setDbPath(libraryPath);
    }

    @Override
    public void prepareDatabase() throws LibraryDatabaseException {
        LOGGER.info("prepareDatabase started");
        try {
            dataStorage.backupDatabase();
            dataStorage.prepareDB();
        } catch (Exception e) {
            LOGGER.error("prepareDatabase error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public int getFileInfoCount() {
        return dataStorage.getFileInfoCount();
    }

    @Override
    public LocalDateTime getLastUpdateDate() {
        return (LocalDateTime) dataStorage.getMeta().get(DataStorage.Fields.LAST_UPDATED_FIELD);
    }

    @Override
    public LocalDateTime getLastRefreshDate() {
        return (LocalDateTime) dataStorage.getMeta().get(DataStorage.Fields.LAST_REFRESH_FIELD);
    }

    @Override
    public void updateLastUpdateDate(LocalDateTime dateTime) throws LibraryDatabaseException {
        try {
            dataStorage.setMeta(DataStorage.Fields.LAST_UPDATED_FIELD, DateUtils.localDateTimeToString(dateTime));
        } catch (SQLException e) {
            LOGGER.error("updateLastUpdateDate error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void updateLastRefreshDate(LocalDateTime dateTime) throws LibraryDatabaseException {
        try {
            dataStorage.setMeta(DataStorage.Fields.LAST_REFRESH_FIELD, DateUtils.localDateTimeToString(dateTime));
        } catch (SQLException e) {
            LOGGER.error("updateLastUpdateDate error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    private List<FileUpdateOperation> getFailedOperationsAndClearQueue() {
        List<FileUpdateOperation> result;
        synchronized (queue) {
            result = queue.parallelStream().filter(fileUpdateOperation -> !fileUpdateOperation.getIsSuccess()).collect(Collectors.toList());
            queue.clear();
        }
        return result;
    }
}
