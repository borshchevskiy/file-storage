package ru.borshchevskiy.filestorage.repository;

import io.minio.messages.Item;

import java.io.InputStream;
import java.util.List;

/**
 * Interface defines methods to work with objects stored in Minio object storage.
 */
public interface MinioRepository {
    List<Item> getItemsByPath(String path, boolean isRecursive);

    InputStream getFile(String path);

    void putFile(InputStream inputStream, String path);

    void deleteFile(String path);

    void deleteDirectory(String path);

    void copyFile(String oldPath, String newPath);

    void createDirectory(String path);
}
