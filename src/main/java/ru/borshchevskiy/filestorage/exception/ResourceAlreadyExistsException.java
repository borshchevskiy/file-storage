package ru.borshchevskiy.filestorage.exception;

/**
 * Exception signals that requested resource already exists.
 * Typically, thrown when trying to create new resource or update existing one, with violation of
 * some unique constraints.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
