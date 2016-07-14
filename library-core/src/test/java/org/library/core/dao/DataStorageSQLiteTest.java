package org.library.core.dao;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.library.core.utils.DateUtils;
import org.library.common.entities.FileInfo;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataStorageSQLiteTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testIntegration() throws Exception {
        Path folder = tempDir.newFolder("db_folder").toPath();
        DataStorageSQLite service = new DataStorageSQLite();
        service.setDbPath(folder);
        service.prepareDB();
        boolean result = Files.exists(Paths.get(folder.toString(), DataStorageSQLite.DB_NAME));
        assertTrue("DB should exists", result);
    }

    @Test
    public void testCreateConnection() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());
        String mockUrl = "test url";

        doReturn(mockUrl).when(service).getDBUrl();

        expectedException.expect(SQLException.class);
        expectedException.expectMessage(mockUrl);

        service.getConnection();
    }

    @Test
    public void testCreateStructure() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());

        Connection mockConnection = mock(Connection.class);
        Statement mockStatement = mock(Statement.class);
        String sql = "test sql";

        doReturn(mockConnection).when(service).getConnection();
        doReturn(mockStatement).when(mockConnection).createStatement();

        service.createStructure(sql);

        verify(mockStatement).executeUpdate(sql);
    }

    @Test
    public void testUpdateStructurePositive() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());

        URL url1 = tempDir.newFile("test0").toURI().toURL();
        URL url2 = tempDir.newFile("test1").toURI().toURL();
        doReturn(0).when(service).executeOneSelectStatement(DataStorageSQLite.getDBVersionSQL);
        doReturn(url1).when(service).getResourceURL(contains("000"));
        doReturn(url2).when(service).getResourceURL(contains("001"));
        doReturn(null).when(service).getResourceURL(contains("002"));
        String sql1 = "0";
        String sql2 = "1";
        doReturn(sql1).when(service).getFileContent(url1);
        doReturn(sql2).when(service).getFileContent(url2);
        doReturn(null).when(service).getFileContent(null);
        doNothing().when(service).createStructure(anyString());

        service.updateStructure();

        verify(service, times(2)).createStructure(anyString());
        verify(service).createStructure(sql1);
        verify(service).createStructure(sql2);
    }

    @Test
    public void testUpdateStructureNegative() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());

        doReturn(0).when(service).executeOneSelectStatement(DataStorageSQLite.getDBVersionSQL);
        doReturn(null).when(service).getResourceURL(contains("0"));

        service.updateStructure();

        verify(service, never()).createStructure(anyString());
    }

    @Test
    public void testUpdate() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());
        FileInfo fileInfo = new FileInfo(UUID.randomUUID(), "path", "fileName", 1L, LocalDateTime.now(), null);

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        doReturn(mockStatement).when(mockConnection).prepareStatement(DataStorageSQLite.updateFileInfoSQL);
        doReturn(1).when(mockStatement).executeUpdate();

        service.prepareDB();
        service.prepareBatch(false);
        service.batchUpdateFileInfo(fileInfo);

        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testExecuteOneSelectStatementPositive() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());
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
        DataStorageSQLite service = spy(new DataStorageSQLite());
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
        DataStorageSQLite service = spy(new DataStorageSQLite());
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
        DataStorageSQLite service = spy(new DataStorageSQLite());
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
        DataStorageSQLite service = spy(new DataStorageSQLite());
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
        DataStorageSQLite service = spy(new DataStorageSQLite());
        FileInfo fileInfo = new FileInfo(UUID.randomUUID(), "path", "fileName", 1L, LocalDateTime.now(), null);

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        doReturn(mockStatement).when(mockConnection).prepareStatement(DataStorageSQLite.insertFileInfoSQL);
        doReturn(1).when(mockStatement).executeUpdate();

        service.prepareDB();
        service.prepareBatch(false);
        service.batchInsertFileInfo(fileInfo);

        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testDelete() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());
        FileInfo fileInfo = new FileInfo(UUID.randomUUID(), "path", "fileName", 1L, LocalDateTime.now(), null);

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        PreparedStatement mockStatement = mock(PreparedStatement.class);
        doReturn(mockStatement).when(mockConnection).prepareStatement(DataStorageSQLite.deleteFileInfoSQL);
        doReturn(1).when(mockStatement).executeUpdate();

        service.prepareDB();
        service.prepareBatch(false);
        service.batchDeleteFileInfo(fileInfo);

        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testGetList() throws Exception {
        DataStorageSQLite service = spy(new DataStorageSQLite());
        FileInfo fileInfo1 = new FileInfo(UUID.randomUUID(), "path1", "fileName1", 1L, LocalDateTime.now(), "hash1");
        FileInfo fileInfo2 = new FileInfo(UUID.randomUUID(), "path2", "fileName2", 2L, LocalDateTime.now(), "hash2");

        Connection mockConnection = mock(Connection.class);
        doReturn(mockConnection).when(service).getConnection();
        doNothing().when(service).updateStructure();

        Statement mockStatement = mock(Statement.class);
        doReturn(mockStatement).when(mockConnection).createStatement();
        ResultSet mockResultSet = mock(ResultSet.class);
        doReturn(mockResultSet).when(mockStatement).executeQuery(DataStorageSQLite.getFileInfoListSQL);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString(DataStorageSQLite.Fields.UUID_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getUUID().toString(), fileInfo2.getUUID().toString());
        when(mockResultSet.getString(DataStorageSQLite.Fields.FILE_PATH_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getPath(), fileInfo2.getPath());
        when(mockResultSet.getString(DataStorageSQLite.Fields.FILE_NAME_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getFileName(), fileInfo2.getFileName());
        when(mockResultSet.getLong(DataStorageSQLite.Fields.FILE_SIZE_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getFileSize(), fileInfo2.getFileSize());
        when(mockResultSet.getString(DataStorageSQLite.Fields.FILE_DATE_FIELD.getDbFieldName()))
                .thenReturn(DateUtils.localDateTimeToString(fileInfo1.getModifiedDate()), DateUtils.localDateTimeToString(fileInfo2.getModifiedDate()));
        when(mockResultSet.getString(DataStorageSQLite.Fields.FILE_MD5_FIELD.getDbFieldName()))
                .thenReturn(fileInfo1.getMd5Hash(), fileInfo2.getMd5Hash());

        service.prepareDB();
        List<FileInfo> result = service.getFileInfoList();
        assertEquals(2, result.size());
        assertEquals(fileInfo1, result.get(0));
        assertEquals(fileInfo2, result.get(1));
    }

}