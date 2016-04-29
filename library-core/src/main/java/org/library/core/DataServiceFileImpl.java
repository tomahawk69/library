package org.library.core;

import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataServiceFileImpl implements DataService {
    private static final String DB_NAME = "db.zip";
    private List<FileUpdateOperation> queue = new ArrayList<>();
    private Path libraryPath;

    @Override
    public void insert(FileInfo fileInfo) {
        if (fileInfo == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation insertOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.INSERT, fileInfo);
        synchronized (queue) {
            queue.add(insertOperation);
        }
    }

    @Override
    public void update(FileInfo fileInfoUpdated, FileInfo fileInfoRollback) {
        if (fileInfoUpdated == null || fileInfoRollback == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation updateOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.UPDATE, fileInfoUpdated, fileInfoRollback);
        synchronized (queue) {
            queue.add(updateOperation);
        }
    }

    @Override
    public void delete(FileInfo fileInfo) {
        if (fileInfo == null) {
            throw new IllegalArgumentException();
        }
        FileUpdateOperation modifyOperation = new FileUpdateOperation(FileUpdateOperation.UpdateType.DELETE, fileInfo);
        synchronized (queue) {
            queue.add(modifyOperation);
        }
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
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        return result;
    }

    @Override
    public List<FileUpdateOperation> rollback() {
        // stop mechanism should be implemented there
        List<FileUpdateOperation> result = getFailedOperationsAndClearQueue();
        return result;
    }

    @Override
    public void setLibraryPath(Path libraryPath) {
        this.libraryPath = libraryPath;
    }

    public List<FileUpdateOperation> getFailedOperationsAndClearQueue() {
        List<FileUpdateOperation> result;
        synchronized (queue) {
            result = queue.stream().filter(fileUpdateOperation -> !fileUpdateOperation.getIsSuccess()).collect(Collectors.toList());
            queue.clear();
        }
        return result;
    }

    public int getQueueSize() {
        return queue.size();
    }
}
