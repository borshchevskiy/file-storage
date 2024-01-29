package ru.borshchevskiy.filestorage.config.handlers;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.borshchevskiy.filestorage.config.properties.MinioProperties;
import ru.borshchevskiy.filestorage.exception.repository.BucketInitException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Creates a Minio bucket if it doesn't exist.
 * Bucket's name is received from {@link MinioProperties} instance.
 *
 * @see MinioProperties
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketHandler {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    /**
     * On {@link ContextRefreshedEvent} checks if Minio bucket with required name exists.
     * If not - creates new bucket. Name is received from {@link MinioProperties} instance.
     *
     * @see ContextRefreshedEvent
     * @see MinioProperties
     */
    @EventListener(ContextRefreshedEvent.class)
    public void handleContextRefreshedEvent() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
                log.debug("Bucket " + minioProperties.getBucket() + " created.");
            }
            log.debug("Bucket check. Bucket " + minioProperties.getBucket() + " exists.");
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("Exception occurred during bucket check. " + e.getMessage() + e);
            throw new BucketInitException(e);
        }
    }
}
