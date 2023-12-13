package ru.borshchevskiy.filestorage.exception;

/**
 * Exception signals that request hasn't been processed due to network, server or any other I/O failure.
 */
public class MultipartProcessingException extends RuntimeException {

    public MultipartProcessingException(String message) {
        super(message);
    }

    public MultipartProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipartProcessingException(Throwable cause) {
        super(cause);
    }
}
