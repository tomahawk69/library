package org.library.core.dao;


import org.library.core.exceptions.LibraryDatabaseException;
import org.library.entities.FileInfo;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    Map<Fields, Object> getMeta();

    void setMeta(Fields field, Object value) throws SQLException;

    enum Fields {
        UUID_FIELD("f_uuid"), FILE_PATH_FIELD("f_file_path"), FILE_NAME_FIELD("f_file_name"),
        FILE_SIZE_FIELD("f_file_size"), FILE_DATE_FIELD("f_file_date"), FILE_MD5_FIELD("f_file_md5"),
        LAST_UPDATED_FIELD("f_last_updated"), LAST_REFRESH_FIELD("f_last_refreshed");

        private final String dbFieldName;

        Fields(String dbFieldName) {
            this.dbFieldName = dbFieldName;
        }

        public String getDbFieldName() {
            return dbFieldName;
        }
    }

}
