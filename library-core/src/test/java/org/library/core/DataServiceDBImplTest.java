package org.library.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.library.core.dao.DataStorage;
import org.library.core.services.DataServiceDBImpl;
import org.library.entities.FileInfo;
import org.library.entities.FileInfoHelper;
import org.library.entities.FileUpdateOperation;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataServiceDBImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testInsert() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.insert(fileInfo1);
        service.insert(fileInfo2);
        assertEquals(2, service.getQueueSize());
    }


    @Test
    public void testInsertNull() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.insert(null);
    }

    @Test
    public void testUpdate() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo1Rollback = FileInfoHelper.createFileInfoCopy(fileInfo1);
        FileInfo fileInfo2 = new FileInfo("");
        FileInfo fileInfo2Rollback = FileInfoHelper.createFileInfoCopy(fileInfo2);
        service.update(fileInfo1, fileInfo1Rollback);
        service.update(fileInfo2, fileInfo2Rollback);
        assertEquals(2, service.getQueueSize());
    }

    @Test
    public void testUpdateNull1() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        FileInfo fileInfo = new FileInfo("");
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.update(fileInfo, null);
    }

    @Test
    public void testUpdateNull2() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        FileInfo fileInfo = new FileInfo("");
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.update(null, fileInfo);
    }

    @Test
    public void testDelete() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.delete(fileInfo1);
        service.delete(fileInfo2);
        assertEquals(2, service.getQueueSize());
    }

    @Test
    public void testDeleteNull() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.delete(null);
    }

    @Test
    public void testCommitPositive() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.insert(fileInfo1);
        FileInfo fileInfo2Rollback = FileInfoHelper.createFileInfoCopy(fileInfo2);
        service.update(fileInfo2, fileInfo2Rollback);
        assertEquals(2, service.getQueueSize());

        doNothing().when(dataStorage).insert(fileInfo1);
        doNothing().when(dataStorage).update(fileInfo2);

        List<FileUpdateOperation> result = service.commit();
        assertEquals(0, service.getQueueSize());
        assertEquals(0, result.size());

        verify(dataStorage).insert(fileInfo1);
        verify(dataStorage).update(fileInfo2);
        verify(dataStorage).clearData();
    }

    @Test
    public void testCommitInsertFailed() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        service.insert(fileInfo1);

        doThrow(SQLException.class).when(dataStorage).insert(fileInfo1);

        List<FileUpdateOperation> result = service.commit();
        assertEquals(0, service.getQueueSize());
        assertEquals(1, result.size());

        verify(dataStorage).insert(fileInfo1);
        assertEquals(fileInfo1, result.get(0).getFileInfo());
    }

    @Test
    public void testCommitUpdateFailed() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.insert(fileInfo1);
        FileInfo fileInfo2Rollback = FileInfoHelper.createFileInfoCopy(fileInfo2);
        service.update(fileInfo2, fileInfo2Rollback);
        assertEquals(2, service.getQueueSize());

        doNothing().when(dataStorage).insert(fileInfo1);
        doThrow(SQLException.class).when(dataStorage).update(fileInfo2);

        List<FileUpdateOperation> result = service.commit();
        assertEquals(0, service.getQueueSize());
        assertEquals(1, result.size());

        verify(dataStorage).insert(fileInfo1);
        verify(dataStorage).update(fileInfo2);
        assertEquals(fileInfo2, result.get(0).getFileInfo());
        assertEquals(fileInfo2Rollback, result.get(0).getRollbackCopy());
    }

    @Test
    public void testRollback() throws Exception {
        DataStorage dataStorage = mock(DataStorage.class);
        DataServiceDBImpl service = new DataServiceDBImpl(dataStorage);
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        FileInfo fileInfo2Rollback = FileInfoHelper.createFileInfoCopy(fileInfo1);
        service.insert(fileInfo1);
        service.update(fileInfo2, fileInfo2Rollback);
        assertEquals(2, service.getQueueSize());
        List<FileUpdateOperation> result = service.rollback();
        assertEquals(0, service.getQueueSize());
        assertEquals(2, result.size());
        assertEquals(fileInfo1, result.get(0).getFileInfo());
        assertEquals(fileInfo2, result.get(1).getFileInfo());
        assertEquals(fileInfo2Rollback, result.get(1).getRollbackCopy());
    }
}