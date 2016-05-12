package org.library.core.services;

import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

interface DataService {

    // CRUD methods
    void insert(FileInfo fileInfoNew);
    void update(FileInfo fileInfoUpdated, FileInfo fileInfoRollback);
    void delete(FileInfo fileInfo);
    FileInfo get(UUID uuid);

    // get all data method
    List<FileInfo> getList();

    // transaction operators
    List<FileUpdateOperation> commit();
    List<FileUpdateOperation> rollback();

    void setLibraryPath(Path libraryPath);
}
