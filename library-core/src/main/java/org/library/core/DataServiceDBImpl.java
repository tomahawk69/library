package org.library.core;

import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class DataServiceDBImpl implements DataService {

    @Override
    public void insert(FileInfo fileInfo) {

    }

    @Override
    public void update(FileInfo fileInfoUpdated, FileInfo fileInfoRollback) {

    }

    @Override
    public void delete(FileInfo fileInfo) {

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
    public List<FileUpdateOperation>  commit() {
        return null;
    }

    @Override
    public List<FileUpdateOperation> rollback() {
        return null;
    }

    @Override
    public void setLibraryPath(Path libraryPath) {

    }
}
