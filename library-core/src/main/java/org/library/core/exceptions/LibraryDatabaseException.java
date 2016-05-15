package org.library.core.exceptions;

import java.sql.SQLException;

public class LibraryDatabaseException extends LibraryException {

    public LibraryDatabaseException(String message) {
        super(message);
    }

    public LibraryDatabaseException(Exception e) {
        super(e);
    }
}
