package org.library.services;

import org.junit.Ignore;
import org.junit.Test;
import org.library.web.entities.DataStatus;

import java.util.Map;

import static org.junit.Assert.*;

public class LibraryServiceTest {

    @Test
    public void testSetDataStatus() throws Exception {
        LibraryService libraryService = new LibraryService();
        DataStatus dataStatus = DataStatus.IDLE;
        libraryService.setDataStatus(dataStatus, null);
        DataStatus resultDataStatus = (DataStatus) libraryService.getDataStatus().get("status");
        assertEquals(dataStatus, resultDataStatus);
    }

    @Test
    @Ignore
    // TODO fix after implement path parameter
    public void testSetDataStatusAfterRead() throws Exception {
        LibraryService libraryService = new LibraryService();
        libraryService.refreshData();
        Map<String, Object> resultDataStatus = libraryService.getDataStatus();
        assertEquals(DataStatus.REFRESH, resultDataStatus.get("status"));
        Thread.sleep(11000);
        assertEquals(DataStatus.IDLE, libraryService.getDataStatus().get("status"));
    }
}