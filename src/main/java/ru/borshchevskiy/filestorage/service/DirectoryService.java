package ru.borshchevskiy.filestorage.service;

/**
 * Defines methods to perform operations with directories.
 */
public interface DirectoryService {


    void deleteDirectory(String path, String name);

    void renameDirectory(String path, String oldName, String newName);

    void createDirectory(String path, String directoryName);
}
