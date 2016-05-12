package org.library.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.library.core.services.DataServiceDBImpl;
import org.library.entities.FileInfo;
import org.library.entities.FileInfoHelper;
import org.library.entities.FileUpdateOperation;

import java.util.List;

import static org.junit.Assert.*;

public class DataServiceDBImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testInsert() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.insert(fileInfo1);
        service.insert(fileInfo2);
        assertEquals(2, service.getQueueSize());
    }


    @Test
    public void testInsertNull() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.insert(null);
    }

    @Test
    public void testUpdate() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
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
        DataServiceDBImpl service = new DataServiceDBImpl();
        FileInfo fileInfo = new FileInfo("");
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.update(fileInfo, null);
    }

    @Test
    public void testUpdateNull2() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
        FileInfo fileInfo = new FileInfo("");
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.update(null, fileInfo);
    }

    @Test
    public void testDelete() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.delete(fileInfo1);
        service.delete(fileInfo2);
        assertEquals(2, service.getQueueSize());
    }

    @Test
    public void testDeleteNull() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
        assertEquals(0, service.getQueueSize());
        expectedException.expect(IllegalArgumentException.class);
        service.delete(null);
    }

    @Test
    public void testCommit() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
        assertEquals(0, service.getQueueSize());
        FileInfo fileInfo1 = new FileInfo("");
        FileInfo fileInfo2 = new FileInfo("");
        service.insert(fileInfo1);
        FileInfo fileInfo2Rollback = FileInfoHelper.createFileInfoCopy(fileInfo2);
        service.update(fileInfo2, fileInfo2Rollback);
        assertEquals(2, service.getQueueSize());
        List<FileUpdateOperation> result = service.commit();
        assertEquals(0, service.getQueueSize());
        assertEquals(2, result.size());
        assertEquals(fileInfo1, result.get(0).getFileInfo());
        assertEquals(fileInfo2, result.get(1).getFileInfo());
        assertEquals(fileInfo2Rollback, result.get(1).getRollbackCopy());
    }

    @Test
    public void testRollback() throws Exception {
        DataServiceDBImpl service = new DataServiceDBImpl();
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