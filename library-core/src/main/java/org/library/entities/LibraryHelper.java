package org.library.entities;

import org.library.core.services.Library;

public class LibraryHelper {

    public static LibraryWeb libraryToLibraryWebEntity(Library library) {
        return new LibraryWeb()
                .setUuid(library.getUUID())
                .setPath(library.getPath())
                .setLastRefreshDate(library.getRefreshDate())
                .setLastUpdateDate(library.getLastUpdateDate())
                .setItemsCount(library.getItemsCount())
                .setName(library.getName());
    }
}
