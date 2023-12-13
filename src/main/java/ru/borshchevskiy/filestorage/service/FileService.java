package ru.borshchevskiy.filestorage.service;

import ru.borshchevskiy.filestorage.dto.file.FileItemDto;

import java.io.InputStream;
import java.util.List;
/**
 * Defines methods to perform operations with files.
 */
public interface FileService {

    List<FileItemDto> getItemsByPath(String prefix);

    InputStream downloadFile(String path, String name);

    void uploadFile(InputStream inputStream, String path, String name);

    void deleteFile(String path, String name);

    void renameFile(String path, String oldName, String newName);

}
