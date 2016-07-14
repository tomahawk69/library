package org.library.core.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.exceptions.LibraryDatabaseException;
import org.library.core.utils.DateUtils;
import org.library.common.utils.FileUtils;
import org.library.common.entities.FileInfo;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
class DataStorageSQLite implements DataStorage {
    private static final Logger LOGGER = LogManager.getLogger(DataStorageSQLite.class);

    static final String DB_NAME = ".db";
    static final String BACKUP_EXT = ".dbak";
    static final String DB_BACKUP_NAME = ".db_%d%02d%02d_%02d%02d%s";
    private Path dbPath;

    private Connection connection;
    static String insertFileInfoSQL;
    static String deleteFileInfoSQL;
    static String updateFileInfoSQL;
    static String getFileInfoListSQL;
    static String getFileInfoCountSQL;
    static String getMetaSQL;
    static String getDBVersionSQL;
    static String setMetaSQL;

    static String clearFileInfoSQL;
    private static final String FILES_TABLE_NAME = "files";
    private static final String META_TABLE_NAME = "meta";

    private static final String UPDATE_SCRIPT_PATH = "metadata/sqlite/%03d.sql";

    static {
        insertFileInfoSQL = new StringBuilder("insert into ")
                .append(FILES_TABLE_NAME).append(" (")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(")")
                .append(" values (?, ?, ?, ?, ?, ?)").toString();
        deleteFileInfoSQL = new StringBuilder("delete from ")
                .append(FILES_TABLE_NAME).append(" where ")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(" = ?").toString();
        updateFileInfoSQL = new StringBuilder("update ")
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
        setMetaSQL = "UPDATE " + META_TABLE_NAME + " set %s = ?";
        getMetaSQL = String.format("SELECT %s, %s FROM %s", Fields.LAST_UPDATED_FIELD.getDbFieldName(), Fields.LAST_REFRESH_FIELD.getDbFieldName(), META_TABLE_NAME);
        getFileInfoCountSQL = String.format("SELECT count() FROM %s", FILES_TABLE_NAME);
        clearFileInfoSQL = "delete from " + FILES_TABLE_NAME;
        getDBVersionSQL = "PRAGMA user_version";
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
        LOGGER.debug("commit");
        try {
            if (isConnectionActive() && !connection.getAutoCommit()) {
                LOGGER.debug("commit executed");
                connection.commit();
            }
            closeConnection();
        } catch (SQLException e) {
            LOGGER.error("commit error", e);
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
        LOGGER.info("Update database structure");
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
    void updateStructure() throws SQLException, IOException {
        Integer databaseVersion = (Integer) executeOneSelectStatement(getDBVersionSQL);
        LOGGER.info("Current database version is " + databaseVersion);
        String scriptPath = String.format(UPDATE_SCRIPT_PATH, databaseVersion);
        URL url = getResourceURL(scriptPath);
        if (url == null) {
            LOGGER.info("No database updates are available");
        } else {
        do {
            LOGGER.info("Starting applying script: " + scriptPath);
            createStructure(getFileContent(url));
            scriptPath = String.format(UPDATE_SCRIPT_PATH, ++databaseVersion);
            url = getResourceURL(scriptPath);
            }
            while (url != null) ;
        }
    }

    public String getFileContent(URL url) throws IOException {
        return FileUtils.loadFileToString(url);
    }

    public URL getResourceURL(String scriptPath) {
        return getClass().getClassLoader().getResource(scriptPath);
    }

    void createStructure(String sql) throws SQLException {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (Exception ex) {
            LOGGER.error("createStructure error", ex);
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
        return String.format("jdbc:sqlite:%s", getDatabasePath());
    }

    public Path getDatabasePath() {
        return Paths.get(dbPath.toString(), DB_NAME);
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
    public Map<Fields, Object> getMeta() {
        Map<Fields, Object> result = new HashMap<>();
        try {
            Map<String, Object> values = executeSelectStatement(getMetaSQL);
            if (values.containsKey(Fields.LAST_REFRESH_FIELD.getDbFieldName())) {
                result.put(Fields.LAST_REFRESH_FIELD, DateUtils.stringToLocalDateTime(String.valueOf(values.get(Fields.LAST_REFRESH_FIELD.getDbFieldName()))));
            }
            if (values.containsKey(Fields.LAST_UPDATED_FIELD.getDbFieldName())) {
                result.put(Fields.LAST_UPDATED_FIELD, DateUtils.stringToLocalDateTime(String.valueOf(values.get(Fields.LAST_UPDATED_FIELD.getDbFieldName()))));
            }
        } catch (Exception e) {
            LOGGER.error("getMeta error", e);
        }
        LOGGER.info("Meta is " + result);
        return result;
    }

    @Override
    public void setMeta(Fields field, Object value) throws SQLException {
        try (Connection connection = getConnection()) {
            String updateSql = String.format(setMetaSQL, field.getDbFieldName());
            PreparedStatement statement = connection.prepareStatement(updateSql);
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean backupDatabase() {
        boolean result = false;
        Path copyDbPath = getDatabaseBackupName();
        if (Files.exists(copyDbPath)) {
            LOGGER.info("Copy of database already exists: " + copyDbPath);
        } else {
            Path dbFilePath = getDatabasePath();
            if (Files.exists(dbFilePath)) {
                LOGGER.info("Creating copy of database: " + copyDbPath);
                try {
                    Files.copy(dbFilePath, copyDbPath);
                    Files.setAttribute(copyDbPath, "dos:hidden", true);
                    result = true;
                    LOGGER.info("Copy created successfully");
                    clearOldBackups(10);
                } catch (IOException e) {
                    LOGGER.error("backupDatabase error ", e);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("setAttribute error ", e);
                }
            }
        }
        return result;
    }

    public void clearOldBackups(int maxCountOfBackups) throws IOException {
        FileUtils.clearOldFiles(BACKUP_EXT, dbPath, maxCountOfBackups);
    }

    private Path getDatabaseBackupName() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Paths.get(dbPath.toString(),
                String.format(DB_BACKUP_NAME,
                        localDateTime.getYear(),
                        localDateTime.getMonthValue(),
                        localDateTime.getDayOfMonth(),
                        localDateTime.getHour(),
                        localDateTime.getMinute(),
                        BACKUP_EXT
                ));
    }

    private Map<String, Object> executeSelectStatement(String querySql) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(querySql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            if (resultSet.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (resultSet.getObject(i) != null) {
                        result.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                }
            }
        }
        return result;
    }

    public LocalDateTime getLastUpdateDate() {
        LocalDateTime result = null;
        try {
            String dateString = (String) executeOneSelectStatement(getMetaSQL);
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


}
