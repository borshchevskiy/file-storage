package ru.borshchevskiy.filestorage.repository.impl;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.borshchevskiy.filestorage.config.properties.MinioProperties;
import ru.borshchevskiy.filestorage.exception.repository.*;
import ru.borshchevskiy.filestorage.repository.MinioRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class provides methods to work with objects stored in Minio object storage.
 */
@Repository
@RequiredArgsConstructor
public class MinioRepositoryImpl implements MinioRepository {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * Method retrieves list of Minio's {@link Item}s stored by specified path.
     *
     * @param path path where objects are located.
     * @param isRecursive boolean flag, determining if selection should be recursive.
     *                    <p>
     *                    If set to false, selection will only include objects stored by
     *                    specified path. E.g. path = "dir/" will only return files
     *                    stored in "dir/" directory.
     *                    <p>
     *                    If set to true, selection will include all objects stored by specified path,
     *                    including objects in subfolders.
     *                    E.g. if storage has directory structure "dir/subdir" and
     *                    specified path = "dir/", than selection will return all files in "dir/" and "dir/subdir".
     * @return {@link List} of {@link Item}s.
     * @throws GetObjectsListException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public List<Item> getItemsByPath(String path, boolean isRecursive) {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioProperties.getBucket())
                .prefix(path)
                .recursive(isRecursive)
                .build());
        List<Item> itemList = new ArrayList<>();

        try {
            for (Result<Item> result : results) {
                itemList.add(result.get());
            }
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new GetObjectsListException("Error while getting files data.", e);
        }
        return itemList;
    }
    /**
     * Method retrieves file from storage and returns it as InputStream.
     *
     * @param path path where file is located.

     * @return {@link InputStream} of the file located by path.
     * @throws GetObjectException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public InputStream getFile(String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new GetObjectException("Error while getting file.", e);
        }
    }

    /**
     * Method saves file to storage.
     *
     * @param inputStream {@link InputStream} of the file to be saved.
     * @param fileName name of the file to be saved.
     * @throws PutObjectException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public void putFile(InputStream inputStream, String fileName) {
        try {
            minioClient.putObject(PutObjectArgs
                    .builder()
                    .stream(inputStream, -1, 10485760)
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException
                 | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException
                 | XmlParserException | InternalException e) {
            throw new PutObjectException("Upload failed.", e);
        }
    }
    /**
     * Method deletes file from storage.
     * @param path path to the file to be deleted.
     * @throws DeleteObjectException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public void deleteFile(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new DeleteObjectException("Delete failed.", e);
        }
    }
    /**
     * Method deletes directory from storage.
     * All directory's files and subdirectories are deleted recursively.
     * @param path path to the directory to be deleted.
     * @throws DeleteObjectException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public void deleteDirectory(String path) {
        List<Item> allItemsByPath = getItemsByPath(path, true);
        List<DeleteObject> objects = allItemsByPath.stream()
                .map(Item::objectName)
                .map(DeleteObject::new).toList();

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(minioProperties.getBucket())
                .objects(objects)
                .build());

        for (Result<DeleteError> result : results) {
            try {
                result.get();
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new DeleteObjectException("Delete failed.", e);
            }
        }
    }
    /**
     * Method copies file from oldPath to newPath.
     * @param oldPath path where file is located.
     * @param newPath path where file should be copied.
     * @throws CopyObjectException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public void copyFile(String oldPath, String newPath) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(newPath)
                    .source(CopySource.builder()
                            .bucket(minioProperties.getBucket())
                            .object(oldPath)
                            .build())
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new CopyObjectException("Object modification failed", e);
        }
    }

    /**
     * Method creates new directory.
     * Due to Minio storage restrictions an empty directory cannot be created. So when a new directory is created
     * it contains a file without name and zero bytes size.
     * @param path path where new directory should be created.
     * @throws PutObjectException in case if any exception is thrown by {@link #minioClient}.
     */
    @Override
    public void createDirectory(String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new PutObjectException("Directory creation failed", e);
        }
    }


}
