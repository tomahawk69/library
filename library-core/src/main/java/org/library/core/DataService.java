package org.library.core;

import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface DataService {
    void insert(FileInfo fileInfoNew);
    void update(FileInfo fileInfoUpdated, FileInfo fileInfoRollback);
    void delete(FileInfo fileInfo);
    FileInfo get(UUID uuid);
    List<FileInfo> getList();
    List<FileUpdateOperation> commit();
    List<FileUpdateOperation> rollback();

    void setLibraryPath(Path libraryPath);
}
