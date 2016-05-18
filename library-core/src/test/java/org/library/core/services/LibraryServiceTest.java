package org.library.core.services;

import org.junit.Test;
import org.library.common.entities.DataStatus;
import org.library.entities.LibraryMetadata;

import static org.mockito.Mockito.*;

public class LibraryServiceTest {

    @Test
    public void testInitLibrary() throws Exception {
        DataService dataService = mock(DataService.class);
        String path = "test path";
        LibraryMetadata libraryMetadata = new LibraryMetadata(path);
        Library library = spy(new Library(dataService, libraryMetadata));

        library.initLibrary();

        verify(dataService, times(1)).prepareDatabase();
        verify(library).populateMetadata();
        verify(library).setDataStatus(DataStatus.IDLE);

    }


}