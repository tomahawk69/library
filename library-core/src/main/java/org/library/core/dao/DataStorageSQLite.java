package org.library.core.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.exceptions.LibraryDatabaseException;
import org.library.core.utils.DateUtils;
import org.library.entities.FileInfo;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
class DataStorageSQLite implements DataStorage {
    private static final Logger LOGGER = LogManager.getLogger(DataStorageSQLite.class);

    static final String DB_NAME = ".db";
    private Path dbPath;
    private Connection connection;
    static String checkTableSQL;
    static String checkTableMetaSQL;
    static String createTableSQL;
    static String createIndexSQL;
    static String createTableMetaSQL;
    static String initTableMetaSQL;

    static String insertFileInfoSQL;
    static String deleteFileInfoSQL;
    static String updateFileInfoSQL;
    static String getFileInfoListSQL;
    static String getFileInfoCountSQL;
    static String getLastUpdateDateSQL;
    static String clearFileInfoSQL;

    private static final String FILES_TABLE_NAME = "files";
    private static final String META_TABLE_NAME = "database";

    static {
        insertFileInfoSQL = new StringBuilder("batchInsertFileInfo into ")
                .append(FILES_TABLE_NAME).append(" (")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(")")
                .append(" values (?, ?, ?, ?, ?, ?)").toString();
        deleteFileInfoSQL = new StringBuilder("batchDeleteFileInfo from ")
                .append(FILES_TABLE_NAME).append(" where ")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(" = ?").toString();
        updateFileInfoSQL = new StringBuilder("batchUpdateFileInfo ")
                .append(FILES_TABLE_NAME).append(" set ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(" = ? ")
                .append(" where ")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(" = ?")
                .toString();
        getFileInfoListSQL = new StringBuilder("select ")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_MD5_FIELD.getDbFieldName()).append(", ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName())
                .append(" from ").append(FILES_TABLE_NAME).toString();
        createTableSQL = new StringBuilder("create table ")
                .append(FILES_TABLE_NAME).append(" (")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(" varchar(36) primary key not null, ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(" varchar(1024) not null, ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(" varchar(255) not null, ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(" integer null, ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(" datetime null, ")
                .append(Fields.FILE_MD5_FIELD.getDbFieldName()).append(" varchar(32) null, ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(" datetime null)")
                .toString();
        createIndexSQL = new StringBuilder("CREATE INDEX ")
                .append(FILES_TABLE_NAME).append("_").append(Fields.FILE_PATH_FIELD.getDbFieldName())
                .append(" ON ").append(FILES_TABLE_NAME).append(" (")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(")").toString();
        createTableMetaSQL = new StringBuilder("create table ")
                .append(META_TABLE_NAME).append(" (")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(" datetime null)")
                .toString();
        checkTableSQL = String.format("SELECT count() FROM sqlite_master WHERE type='table' AND name='%s'",
                FILES_TABLE_NAME);
        checkTableMetaSQL = String.format("SELECT count() FROM sqlite_master WHERE type='table' AND name='%s'",
                META_TABLE_NAME);
        initTableMetaSQL = String.format("INSERT INTO %s (%s) values(null)",
                META_TABLE_NAME, Fields.LAST_UPDATED_FIELD.getDbFieldName());
        getFileInfoCountSQL = String.format("SELECT count() FROM %s", FILES_TABLE_NAME);
        getLastUpdateDateSQL = String.format("SELECT %s FROM %s", Fields.LAST_UPDATED_FIELD.getDbFieldName(), META_TABLE_NAME);
        clearFileInfoSQL = "batchDeleteFileInfo from " + FILES_TABLE_NAME;
    }

    @Override
    public void batchUpdateFileInfo(FileInfo fileInfo) throws LibraryDatabaseException {
        LOGGER.debug("Updating " + fileInfo);
        try (PreparedStatement statement = connection.prepareStatement(updateFileInfoSQL)) {
            statement.setString(6, fileInfo.getUUID().toString());
            statement.setString(1, fileInfo.getPath());
            statement.setString(2, fileInfo.getFileName());
            statement.setLong(3, fileInfo.getFileSize());
            statement.setString(4, DateUtils.localDateTimeToString(fileInfo.getModifiedDate()));
            statement.setString(3, fileInfo.getMd5Hash());
            int i = statement.executeUpdate();
            LOGGER.debug(String.format("Updated %d records", i));
        } catch (SQLException e) {
            LOGGER.error("Update error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void batchDeleteFileInfo(FileInfo fileInfo) throws LibraryDatabaseException {
        LOGGER.debug("Deleting " + fileInfo);
        try (PreparedStatement statement = connection.prepareStatement(deleteFileInfoSQL)) {
            statement.setString(1, fileInfo.getUUID().toString());
            int i = statement.executeUpdate();
            LOGGER.debug(String.format("Deleted %d records", i));
        } catch (SQLException e) {
            LOGGER.error("Delete error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void batchInsertFileInfo(FileInfo fileInfo) throws LibraryDatabaseException {
        LOGGER.debug("Inserting " + fileInfo);
        try (PreparedStatement statement = connection.prepareStatement(insertFileInfoSQL)) {
            statement.setString(1, fileInfo.getUUID().toString());
            statement.setString(2, fileInfo.getPath());
            statement.setString(3, fileInfo.getFileName());
            statement.setLong(4, fileInfo.getFileSize());
            statement.setString(5, DateUtils.localDateTimeToString(fileInfo.getModifiedDate()));
            statement.setString(6, fileInfo.getMd5Hash());
            int i = statement.executeUpdate();
            LOGGER.debug(String.format("Inserted %d records", i));
        } catch (SQLException e) {
            LOGGER.error("Insert error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void prepareBatch(boolean autoCommit) throws LibraryDatabaseException {
        closeConnection();
        try {
            connection = getConnection();
            if (!autoCommit) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            LOGGER.error("prepareBatch error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void commit() throws LibraryDatabaseException {
        try {
            if (isConnectionActive() && !connection.getAutoCommit()) {
                connection.commit();
            }
            closeConnection();
        } catch (SQLException e) {
            LOGGER.error("commitFileInfo error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void rollback() throws LibraryDatabaseException {
        try {
            if (isConnectionActive() && !connection.getAutoCommit()) {
                connection.commit();
            }
            closeConnection();
        } catch (SQLException e) {
            LOGGER.error("rollbackFileInfo error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    private boolean isConnectionActive() {
        boolean result = false;
        try {
            result = connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LOGGER.error("isConnectionActive error", e);
        }
        return result;
    }

    @Override
    public List<FileInfo> getFileInfoList() throws LibraryDatabaseException {
        LOGGER.debug("Selecting all records");
        List<FileInfo> results = new ArrayList<>();
        Connection connection = null;
        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(getFileInfoListSQL);
            while (resultSet.next()) {
                FileInfo fileInfo = new FileInfo(
                        UUID.fromString(resultSet.getString(Fields.UUID_FIELD.getDbFieldName())),
                        resultSet.getString(Fields.FILE_PATH_FIELD.getDbFieldName()),
                        resultSet.getString(Fields.FILE_NAME_FIELD.getDbFieldName()),
                        resultSet.getLong(Fields.FILE_SIZE_FIELD.getDbFieldName()),
                        DateUtils.stringToLocalDateTime(resultSet.getString(Fields.FILE_DATE_FIELD.getDbFieldName())),
                        resultSet.getString(Fields.FILE_MD5_FIELD.getDbFieldName())
                );
                results.add(fileInfo);
            }
        } catch (SQLException e) {
            LOGGER.error("isConnectionActive error", e);
            throw new LibraryDatabaseException(e);
        } finally {
            closeConnection(connection);
        }
        LOGGER.debug(String.format("Selected %d records", results.size()));
        return results;
    }

    @Override
    public void prepareDB() {
        LOGGER.debug("Update database structure");
        try {
            updateStructure();
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDBUrl());
    }

    /**
     * No upgrade is available
     */
    void updateStructure() throws SQLException {
        Object count = executeOneSelectStatement(checkTableSQL);
        if ((int) count == 0) {
            createStructure();
        }
        count = executeOneSelectStatement(checkTableMetaSQL);
        if ((int) count == 0) {
            createStructureMeta();
        }
    }

    private void createStructureMeta() throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableMetaSQL);
            statement.executeUpdate(initTableMetaSQL);
        }
    }

    void createStructure() throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableSQL);
            statement.executeUpdate(createIndexSQL);
        }
    }

    Object executeOneSelectStatement(String sql) throws SQLException {
        Object result = null;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            if (statement.execute(sql)) {
                ResultSet resultSet = statement.getResultSet();
                if (resultSet.getMetaData().getColumnCount() == 1) {
                    if (resultSet.next()) {
                        result = resultSet.getObject(1);
                    }
                    if (resultSet.next()) {
                        throw new SQLException("Result set contains more than 1 record");
                    }
                } else {
                    throw new SQLException("Result set contains of more than 1 column");
                }
            }
        }
        return result;
    }

    String getDBUrl() {
        return String.format("jdbc:sqlite:%s", Paths.get(dbPath.toString(), DB_NAME));
    }

    @Override
    public void setDbPath(Path dbPath) {
        this.dbPath = dbPath;
    }

    @Deprecated
    @Override
    public void clearData() throws LibraryDatabaseException {
        // TODO remove after proper implementation of commitFileInfo procedure
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(clearFileInfoSQL);
        } catch (SQLException e) {
            LOGGER.error("Update error", e);
            throw new LibraryDatabaseException(e);
        }
    }

    @Override
    public void closeConnection() {
        closeConnection(connection);
    }

    @Override
    public int getFileInfoCount() {
        int result = 0;
        try {
            result = (int) executeOneSelectStatement(getFileInfoCountSQL);
        } catch (SQLException e) {
            LOGGER.error("getFileInfoCount error", e);
        }
        return result;
    }

    @Override
    public LocalDateTime getLastUpdateDate() {
        LocalDateTime result = null;
        try {
            String dateString = (String) executeOneSelectStatement(getLastUpdateDateSQL);
            if (dateString != null) {
                result = DateUtils.stringToLocalDateTime(dateString);
            }
        } catch (SQLException e) {
            LOGGER.error("getLastUpdateDate error", e);
        }
        return result;
    }

    private void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            LOGGER.error(ex);
        }
    }

    enum Fields {
        UUID_FIELD("f_uuid"), FILE_PATH_FIELD("f_file_path"), FILE_NAME_FIELD("f_file_name"),
        FILE_SIZE_FIELD("f_file_size"), FILE_DATE_FIELD("f_file_date"), FILE_MD5_FIELD("f_file_md5"),
        LAST_UPDATED_FIELD("f_last_updated");

        private final String dbFieldName;

        Fields(String dbFieldName) {
            this.dbFieldName = dbFieldName;
        }

        public String getDbFieldName() {
            return dbFieldName;
        }
    }
}
