package org.library.core.exceptions;

import java.io.IOException;

public class LibrarySettingsException extends LibraryException {

    public LibrarySettingsException(IOException ex) {
        super(ex);
    }
}
