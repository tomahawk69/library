package org.library.core.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.entities.FileInfo;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
class DataStorageSQLLite implements DataStorage {
    private static final Logger LOGGER = LogManager.getLogger(DataStorageSQLLite.class);

    static final String DB_NAME = ".db";
    private static final String DB_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private Path dbPath;
    private Connection connection;
    static String checkTableSQL;
    static String createTableSQL;
    static String createIndexSQL;
    static String insertSQL;
    static String deleteSQL;
    static String updateSQL;
    static String getListSQL;
    static String clearDataSQL;

    private static final String FILES_TABLE_NAME = "files";

    static {
        insertSQL = new StringBuilder("insert into ")
                .append(FILES_TABLE_NAME).append(" (")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(", ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(")")
                .append(" values (?, ?, ?, ?, ?, ?)").toString();
        deleteSQL = new StringBuilder("delete from ")
                .append(FILES_TABLE_NAME).append(" where ")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(" = ?").toString();
        updateSQL = new StringBuilder("update ")
                .append(FILES_TABLE_NAME).append(" set ")
                .append(Fields.FILE_PATH_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.FILE_NAME_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.FILE_SIZE_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.FILE_DATE_FIELD.getDbFieldName()).append(" = ?, ")
                .append(Fields.LAST_UPDATED_FIELD.getDbFieldName()).append(" = ? ")
                .append(" where ")
                .append(Fields.UUID_FIELD.getDbFieldName()).append(" = ?")
                .toString();
        getListSQL = new StringBuilder("select ")
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
        checkTableSQL = String.format("SELECT count() FROM sqlite_master WHERE type='table' AND name='%s'",
                FILES_TABLE_NAME);
        clearDataSQL = "delete from " + FILES_TABLE_NAME;
    }

    @Override
    public void update(FileInfo fileInfo) throws SQLException {
        LOGGER.debug("Updating " + fileInfo);
        try (PreparedStatement statement = connection.prepareStatement(updateSQL)) {
            statement.setString(6, fileInfo.getUUID().toString());
            statement.setString(1, fileInfo.getPath());
            statement.setString(2, fileInfo.getFileName());
            statement.setLong(3, fileInfo.getFileSize());
            statement.setString(4, localDateTimeToDBString(fileInfo.getModifiedDate()));
            statement.setString(3, fileInfo.getMd5Hash());
            int i = statement.executeUpdate();
            LOGGER.debug(String.format("Updated %d records", i));
        }
    }

    @Override
    public void delete(FileInfo fileInfo) throws SQLException {
        LOGGER.debug("Deleting " + fileInfo);
        try (PreparedStatement statement = connection.prepareStatement(deleteSQL)) {
            statement.setString(1, fileInfo.getUUID().toString());
            int i = statement.executeUpdate();
            LOGGER.debug(String.format("Deleted %d records", i));
        }
    }

    @Override
    public void insert(FileInfo fileInfo) throws SQLException {
        LOGGER.debug("Inserting " + fileInfo);
        try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            statement.setString(1, fileInfo.getUUID().toString());
            statement.setString(2, fileInfo.getPath());
            statement.setString(3, fileInfo.getFileName());
            statement.setLong(4, fileInfo.getFileSize());
            statement.setString(5, localDateTimeToDBString(fileInfo.getModifiedDate()));
            statement.setString(6, fileInfo.getMd5Hash());
            int i = statement.executeUpdate();
            LOGGER.debug(String.format("Inserted %d records", i));
        }
    }

    static String localDateTimeToDBString(LocalDateTime localDateTime) {
        return localDateTime.format(getDBDateTimeFormatter());
    }

    @Override
    public List<FileInfo> getList() throws SQLException {
        LOGGER.debug("Selecting all records");
        List<FileInfo> results = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(getListSQL)) {
                while (resultSet.next()) {
                    FileInfo fileInfo = new FileInfo(
                            UUID.fromString(resultSet.getString(Fields.UUID_FIELD.getDbFieldName())),
                            resultSet.getString(Fields.FILE_PATH_FIELD.getDbFieldName()),
                            resultSet.getString(Fields.FILE_NAME_FIELD.getDbFieldName()),
                            resultSet.getLong(Fields.FILE_SIZE_FIELD.getDbFieldName()),
                            dbStringToLocalDateTime(resultSet.getString(Fields.FILE_DATE_FIELD.getDbFieldName())),
                            resultSet.getString(Fields.FILE_MD5_FIELD.getDbFieldName())
                    );
                    results.add(fileInfo);
                }
            }
        }
        LOGGER.debug(String.format("Selected %d records", results.size()));
        return results;
    }

    private static LocalDateTime dbStringToLocalDateTime(String string) {
        return LocalDateTime.parse(string, getDBDateTimeFormatter());
    }

    private static DateTimeFormatter getDBDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(DB_DATETIME_PATTERN);
    }

    void prepareDB() throws SQLException {
        LOGGER.debug("Update database structure");
        try {
            createConnection();
            updateStructure();
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage(), e);
        } finally {
            closeConnection();
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
    }

    void createStructure() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            statement.executeUpdate(createIndexSQL);
        }
    }

    Object executeOneSelectStatement(String sql) throws SQLException {
        Object result = null;
        try (Statement statement = connection.createStatement()) {
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
    public void setDbPath(Path dbPath) throws SQLException {
        this.dbPath = dbPath;
        prepareDB();
    }

    @Deprecated
    @Override
    public void clearData() throws SQLException {
        // TODO remove after proper implementation of commit procedure
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(clearDataSQL);
        }
    }

    @Override
    public void createConnection() throws SQLException {
        closeConnection();
        connection = getConnection();
    }

    @Override
    public void closeConnection()  {
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
