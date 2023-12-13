package ru.borshchevskiy.filestorage.exception.repository;

/**
 * Parent exception for all {@link  ru.borshchevskiy.filestorage.repository.MinioRepository} related exceptions.
 */
public class MinioRepositoryException extends RuntimeException {

    public MinioRepositoryException(String message) {
        super(message);
    }

    public MinioRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinioRepositoryException(Throwable cause) {
        super(cause);
    }
}
