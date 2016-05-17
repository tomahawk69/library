package org.library.core.web;

import org.junit.Test;
import org.library.common.utils.LoggingUtils;
import org.library.core.services.LibraryService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FacadeServiceTest {

    @Test
    public void refreshData() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        String uuid = UUID.randomUUID().toString();
        service.refreshData(uuid);
        verify(mockLibraryService).refreshData(uuid);
    }

    @Test
    public void stopRefreshData() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        String uuid = UUID.randomUUID().toString();
        service.stopRefreshData(uuid);
        verify(mockLibraryService).stopRefreshData(uuid);
    }

    @Test
    public void getDataStatus() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("some string", "test");
        expectedResult.put("some number", 123);
        String uuid = UUID.randomUUID().toString();
        when(mockLibraryService.getDataStatus(uuid)).thenReturn(expectedResult);
        Map<String, Object> result = service.getDataStatus(uuid);
        assertEquals(expectedResult, result);
    }

    @Test
    public void setDebugEnabledTrue() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        String expectedResult = String.valueOf(true);
        String result = service.setDebugEnabled(true);
        assertEquals(expectedResult, result);
    }

    @Test
    public void setDebugEnabledFalse() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        String expectedResult = String.valueOf(false);
        String result = service.setDebugEnabled(false);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDebugEnabled() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        String expectedResult = String.valueOf(LoggingUtils.getDebugEnabled());
        String result = service.getDebugEnabled();
        assertEquals(expectedResult, result);
    }
}