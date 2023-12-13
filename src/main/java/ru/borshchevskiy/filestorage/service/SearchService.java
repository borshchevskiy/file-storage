package ru.borshchevskiy.filestorage.service;

import ru.borshchevskiy.filestorage.dto.file.FileItemDto;

import java.util.List;

/**
 *  Defines methods to perform search operations in storage.
 */
public interface SearchService {

    List<FileItemDto> search(String query);
}
