package org.library.core.dao;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.library.entities.FileInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataStorageSQLLiteTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testIntegration() throws Exception {
        Path folder = tempDir.newFolder("db_folder").toPath();
        DataStorageSQLLite service = new DataStorageSQLLite();
        service.setDbPath(folder);
        boolean result = Files.exists(Paths.get(folder.toString(), DataStorageSQLLite.DB_NAME));
        assertTrue("DB should exists", result);
    }

    @Test
    public void testCreateConnection() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        String mockUrl = "test url";

        doReturn(mockUrl).when(service).getDBUrl();

        expectedException.expect(SQLException.class);
        expectedException.expectMessage(mockUrl);

        service.getConnection();
    }

    @Test
    public void testCreateStructure() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        doReturn(mockStatement).when(mockConnection).createStatement();

        service.prepareDB();
        service.createStructure();

        verify(mockStatement).executeUpdate(DataStorageSQLLite.createTableSQL);
        verify(mockStatement).executeUpdate(DataStorageSQLLite.createIndexSQL);
    }

    @Test
    public void testUpdateStructurePositive() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doReturn(0).when(service).executeOneSelectStatement(DataStorageSQLLite.checkTableSQL);
        doNothing().when(service).createStructure();

        service.prepareDB();

        verify(service).createStructure();
    }

    @Test
    public void testUpdateStructureNegative() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doReturn(1).when(service).executeOneSelectStatement(DataStorageSQLLite.checkTableSQL);
        doNothing().when(service).createStructure();

        service.prepareDB();

        verify(service, never()).createStructure();
    }

    @Test
    public void testUpdate() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        FileInfo fileInfo = new FileInfo(UUID.randomUUID(), "path", "fileName", 1L, LocalDateTime.now(), null);

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        doReturn(mockStatement).when(mockConnection).prepareStatement(DataStorageSQLLite.updateSQL);
        doReturn(1).when(mockStatement).executeUpdate();

        service.prepareDB();
        service.update(fileInfo);

        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testExecuteOneSelectStatementPositive() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        String sql = "test sql";
        Object expectedResult = new Object();

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockResultSetMetaData = mock(ResultSetMetaData.class);

        doReturn(mockStatement).when(mockConnection).createStatement();
        doReturn(true).when(mockStatement).execute(sql);
        doReturn(mockResultSet).when(mockStatement).getResultSet();
        doReturn(mockResultSetMetaData).when(mockResultSet).getMetaData();
        doReturn(1).when(mockResultSetMetaData).getColumnCount();
        when(mockResultSet.next()).thenReturn(true, false);
        doReturn(expectedResult).when(mockResultSet).getObject(1);

        service.prepareDB();
        Object result = service.executeOneSelectStatement(sql);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testExecuteOneSelectStatementNegative() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        String sql = "test sql";

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);

        doReturn(mockStatement).when(mockConnection).createStatement();
        doReturn(false).when(mockStatement).execute(sql);

        service.prepareDB();
        Object result = service.executeOneSelectStatement(sql);

        assertNull(result);
    }

    @Test
    public void testExecuteOneSelectStatementWrongQueryLesser() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        String sql = "test sql";

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockResultSetMetaData = mock(ResultSetMetaData.class);

        doReturn(mockStatement).when(mockConnection).createStatement();
        doReturn(true).when(mockStatement).execute(sql);
        doReturn(mockResultSet).when(mockStatement).getResultSet();
        doReturn(mockResultSetMetaData).when(mockResultSet).getMetaData();
        doReturn(0).when(mockResultSetMetaData).getColumnCount();
        expectedException.expect(SQLException.class);

        service.prepareDB();
        service.executeOneSelectStatement(sql);
    }

    @Test
    public void testExecuteOneSelectStatementWrongQueryGreater() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        String sql = "test sql";

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockResultSetMetaData = mock(ResultSetMetaData.class);

        doReturn(mockStatement).when(mockConnection).createStatement();
        doReturn(true).when(mockStatement).execute(sql);
        doReturn(mockResultSet).when(mockStatement).getResultSet();
        doReturn(mockResultSetMetaData).when(mockResultSet).getMetaData();
        doReturn(2).when(mockResultSetMetaData).getColumnCount();
        expectedException.expect(SQLException.class);

        service.prepareDB();
        service.executeOneSelectStatement(sql);
    }

    @Test
    public void testExecuteOneSelectStatementWrongQueryMoreThanOneRecord() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        String sql = "test sql";

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockResultSetMetaData = mock(ResultSetMetaData.class);

        doReturn(mockStatement).when(mockConnection).createStatement();
        doReturn(true).when(mockStatement).execute(sql);
        doReturn(mockResultSet).when(mockStatement).getResultSet();
        doReturn(mockResultSetMetaData).when(mockResultSet).getMetaData();
        doReturn(1).when(mockResultSetMetaData).getColumnCount();
        doReturn(null).when(mockResultSet).getObject(1);
        when(mockResultSet.next()).thenReturn(true, true);
        expectedException.expect(SQLException.class);

        service.prepareDB();
        service.executeOneSelectStatement(sql);
    }

    @Test
    public void testInsert() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        FileInfo fileInfo = new FileInfo(UUID.randomUUID(), "path", "fileName", 1L, LocalDateTime.now(), null);

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        doReturn(mockStatement).when(mockConnection).prepareStatement(DataStorageSQLLite.insertSQL);
        doReturn(1).when(mockStatement).executeUpdate();

        service.prepareDB();
        service.insert(fileInfo);

        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testDelete() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        FileInfo fileInfo = new FileInfo(UUID.randomUUID(), "path", "fileName", 1L, LocalDateTime.now(), null);

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        doReturn(mockStatement).when(mockConnection).prepareStatement(DataStorageSQLLite.deleteSQL);
        doReturn(1).when(mockStatement).executeUpdate();

        service.prepareDB();
        service.delete(fileInfo);

        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testGetList() throws Exception {
        DataStorageSQLLite service = spy(new DataStorageSQLLite());
        FileInfo fileInfo1 = new FileInfo(UUID.randomUUID(), "path1", "fileName1", 1L, LocalDateTime.now(), "hash1");
        FileInfo fileInfo2 = new FileInfo(UUID.randomUUID(), "path2", "fileName2", 2L, LocalDateTime.now(), "hash2");

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        doReturn(mockStatement).when(mockConnection).createStatement();
        ResultSet mockResultSet = mock(ResultSet.class);
        doReturn(mockResultSet).when(mockStatement).executeQuery(DataStorageSQLLite.getListSQL);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString(DataStorageSQLLite.Fields.UUID_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getUUID().toString(), fileInfo2.getUUID().toString());
        when(mockResultSet.getString(DataStorageSQLLite.Fields.FILE_PATH_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getPath(), fileInfo2.getPath());
        when(mockResultSet.getString(DataStorageSQLLite.Fields.FILE_NAME_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getFileName(), fileInfo2.getFileName());
        when(mockResultSet.getLong(DataStorageSQLLite.Fields.FILE_SIZE_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getFileSize(), fileInfo2.getFileSize());
        when(mockResultSet.getString(DataStorageSQLLite.Fields.FILE_DATE_FIELD.getDbFieldName()))
                .thenReturn(DataStorageSQLLite.localDateTimeToDBString(fileInfo1.getModifiedDate()), DataStorageSQLLite.localDateTimeToDBString(fileInfo2.getModifiedDate()));
        when(mockResultSet.getString(DataStorageSQLLite.Fields.FILE_MD5_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getMd5Hash(), fileInfo2.getMd5Hash());

        service.prepareDB();
        List<FileInfo> result = service.getList();
        assertEquals(2, result.size());
        assertEquals(fileInfo1, result.get(0));
        assertEquals(fileInfo2, result.get(1));
    }

}