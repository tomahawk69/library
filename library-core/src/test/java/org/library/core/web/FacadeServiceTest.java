package org.library.core.web;

import org.junit.Test;
import org.library.common.utils.LoggingUtils;
import org.library.core.services.LibraryService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FacadeServiceTest {

    @Test
    public void refreshData() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        service.refreshData();
        verify(mockLibraryService).refreshData();
    }

    @Test
    public void stopRefreshData() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        service.stopRefreshData();
        verify(mockLibraryService).stopRefreshData();
    }

    @Test
    public void getDataStatus() throws Exception {
        LibraryService mockLibraryService = mock(LibraryService.class);
        FacadeService service = new FacadeService(mockLibraryService);
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("some string", "test");
        expectedResult.put("some number", 123);
        when(mockLibraryService.getDataStatus()).thenReturn(expectedResult);
        Map<String, Object> result = service.getDataStatus();
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