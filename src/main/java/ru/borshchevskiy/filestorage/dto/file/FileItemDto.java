package ru.borshchevskiy.filestorage.dto.file;

import io.minio.messages.Item;
import lombok.Data;

/**
 * Class represents object (file or directory) stored in file storage.
 */
@Data
public class FileItemDto {
    /**
     * Name of the file or directory, e.g. "file.txt" or "file" for file and "dir/" for folder.
     * Directory's names always and with "/" symbol.
     */
    private String name;
    /**
     * Name of the file or directory including path,
     * e.g. "dir/file.txt" or "dir/file" for file and "folder/dir/" for folder.
     * Directory's names always and with "/" symbol.
     * Root user directory is not included in path.
     */
    private String fullName;

    /**
     * Boolean flag, true if object is directory, false if it is a file.
     */
    private boolean isDirectory;
    /**
     * File size in bytes.
     */
    private Long size;
    /**
     * Human-readable size representation.
     * @see ru.borshchevskiy.filestorage.mapper.FileItemMapper#getViewSize(Item) FileItemMapper.getViewSize(Item)
     */
    private String viewSize;

}
