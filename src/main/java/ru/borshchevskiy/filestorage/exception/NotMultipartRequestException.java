package ru.borshchevskiy.filestorage.exception;

/**
 * Exception signals that request doesn't contain multipart content.
 */
public class NotMultipartRequestException extends RuntimeException {

    public NotMultipartRequestException(String message) {
        super(message);
    }

    public NotMultipartRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotMultipartRequestException(Throwable cause) {
        super(cause);
    }
}
