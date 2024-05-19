package org.ase.image;

public class UnsupportedFileTypeException extends Exception {
    public UnsupportedFileTypeException() {
        super();
    }

    public UnsupportedFileTypeException(String message) {
        super(message);
    }

    public UnsupportedFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFileTypeException(Throwable cause) {
        super(cause);
    }
}
