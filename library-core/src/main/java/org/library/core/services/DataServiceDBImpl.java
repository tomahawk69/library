package org.library.core.services;

import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("dataServiceDBImpl")
public class DataServiceDBImpl extends AbstractDataService{

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
        // TODO commit stuff here
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        return result;
    }

    @Override
    public List<FileUpdateOperation> rollback() {
        // TODO: stop mechanism should be implemented there
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        return result;
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
