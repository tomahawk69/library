package org.library.core.dao;


import org.library.entities.FileInfo;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public interface DataStorage {
    void update(FileInfo fileInfo) throws SQLException;
    void delete(FileInfo fileInfo) throws SQLException;
    void insert(FileInfo fileInfo) throws SQLException;
    List<FileInfo> getList() throws SQLException;

    void setDbPath(Path dbPath) throws SQLException;

    void clearData() throws SQLException;

    void createConnection() throws SQLException;

    void closeConnection();
}
