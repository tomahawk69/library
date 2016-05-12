package org.library.core.services;

import org.junit.Ignore;
import org.junit.Test;
import org.library.common.entities.DataStatus;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LibraryServiceTest {

    @Test
    public void testSetDataStatus() throws Exception {
        DataService dataService = mock(DataServiceDBImpl.class);
        LibraryService libraryService = new LibraryService(dataService, "", 1);
        DataStatus dataStatus = DataStatus.IDLE;
        libraryService.setDataStatus(dataStatus, null);
        DataStatus resultDataStatus = (DataStatus) libraryService.getDataStatus().get("status");
        assertEquals(dataStatus, resultDataStatus);
    }

    @Test
    @Ignore
    // TODO fix after implement path parameter
    public void testSetDataStatusAfterRead() throws Exception {
        DataService dataService = mock(DataServiceDBImpl.class);
        LibraryService libraryService = new LibraryService(dataService, "", 1);
        libraryService.refreshData();
        Map<String, Object> resultDataStatus = libraryService.getDataStatus();
        assertEquals(DataStatus.REFRESH, resultDataStatus.get("status"));
        Thread.sleep(11000);
        assertEquals(DataStatus.IDLE, libraryService.getDataStatus().get("status"));
    }

}