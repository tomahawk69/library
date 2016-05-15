package org.library.core.exceptions;

public abstract class LibraryException extends Exception {

    public LibraryException(Exception ex) {
        super(ex);
    }

    public LibraryException(String message) {
        super(message);
    }
}
