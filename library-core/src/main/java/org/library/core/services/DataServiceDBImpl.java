package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.dao.DataStorage;
import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("dataServiceDBImpl")
public class DataServiceDBImpl extends AbstractDataService{
    private final DataStorage dataStorage;
    private static final Logger LOGGER = LogManager.getLogger(DataServiceDBImpl.class);

    @Autowired
    public DataServiceDBImpl(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    @Override
    public void insert(FileInfo fileInfo) {
        if (fileInfo == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation insertOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.INSERT, fileInfo);
        addOperationToQueue(insertOperation);
    }

    @Override
    public void update(FileInfo fileInfoUpdated, FileInfo fileInfoRollback) {
        if (fileInfoUpdated == null || fileInfoRollback == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation updateOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.UPDATE, fileInfoUpdated, fileInfoRollback);
        addOperationToQueue(updateOperation);
    }

    @Override
    public void delete(FileInfo fileInfo) {
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
    public List<FileInfo> getList() {
        return null;
    }

    @Override
    public List<FileUpdateOperation> commit() {
        LOGGER.info("commit started");
        try {
            dataStorage.createConnection();
            dataStorage.clearData();
            for (FileUpdateOperation fileUpdateOperation : queue) {
                synchronized (fileUpdateOperation.getFileInfo()) {
                    switch (fileUpdateOperation.getUpdateType()) {
                        case INSERT:
                            dataStorage.insert(fileUpdateOperation.getFileInfo());
                            break;
                        case UPDATE:
                            dataStorage.update(fileUpdateOperation.getFileInfo());
                            break;
                        case DELETE:
                            dataStorage.delete(fileUpdateOperation.getFileInfo());
                            break;
                        default:
                            throw new Exception("Unknown operation type: " + fileUpdateOperation.getUpdateType());
                    }
                }
                fileUpdateOperation.setSuccess();
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        } finally {
            dataStorage.closeConnection();
        }
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        LOGGER.info("commit done, failed " + result.size());
        return result;
    }

    @Override
    public List<FileUpdateOperation> rollback() {
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        return result;
    }

    @Override
    public void setLibraryPath(Path libraryPath) {
        LOGGER.info("setLibraryPath to " + libraryPath);
        this.libraryPath = libraryPath;
        try {
            dataStorage.setDbPath(libraryPath);
        } catch (SQLException ex) {
            // TODO properly handle exception
            LOGGER.error(ex);
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
