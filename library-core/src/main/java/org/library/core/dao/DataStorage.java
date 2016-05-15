package org.library.core.dao;


import org.library.core.exceptions.LibraryDatabaseException;
import org.library.entities.FileInfo;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public interface DataStorage {

    void batchUpdateFileInfo(FileInfo fileInfo) throws LibraryDatabaseException;
    void batchDeleteFileInfo(FileInfo fileInfo) throws LibraryDatabaseException;
    void batchInsertFileInfo(FileInfo fileInfo) throws LibraryDatabaseException;

    void prepareBatch(boolean autoCommit) throws LibraryDatabaseException;
    void commit() throws LibraryDatabaseException;
    void rollback() throws LibraryDatabaseException;

    List<FileInfo> getFileInfoList() throws LibraryDatabaseException;

    void prepareDB() throws LibraryDatabaseException;

    void setDbPath(Path dbPath);

    void clearData() throws LibraryDatabaseException;

    void closeConnection();

    int getFileInfoCount();

    LocalDateTime getLastUpdateDate();
}
