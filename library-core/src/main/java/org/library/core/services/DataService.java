package org.library.core.services;

import org.library.core.exceptions.LibraryDatabaseException;
import org.library.entities.FileInfo;
import org.library.entities.FileUpdateOperation;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DataService {

    // CRUD methods
    void insertFileInfo(FileInfo fileInfoNew);
    void updateFileInfo(FileInfo fileInfoUpdated, FileInfo fileInfoRollback);
    void deleteFileInfo(FileInfo fileInfo);
    FileInfo get(UUID uuid);

    // get all data method
    List<FileInfo> getFileInfoList();

    // transaction operators
    List<FileUpdateOperation> commitFileInfo();
    List<FileUpdateOperation> rollbackFileInfo();

    void setDatabasePath(Path path);
    void prepareDatabase() throws LibraryDatabaseException;

    int getFileInfoCount();

    LocalDateTime getLastUpdateDate();
}
