package ru.borshchevskiy.filestorage.exception.repository;

/**
 * Exception produced by {@link ru.borshchevskiy.filestorage.repository.MinioRepository} when
 * {@link io.minio.MinioClient} throws any exception when trying to copy an object.
 * This exception serves as wrapper for these exceptions.
 *
 * @see ru.borshchevskiy.filestorage.repository.MinioRepository
 * @see io.minio.MinioClient
 */
public class CopyObjectException extends MinioRepositoryException {

    public CopyObjectException(String message) {
        super(message);
    }

    public CopyObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public CopyObjectException(Throwable cause) {
        super(cause);
    }
}
