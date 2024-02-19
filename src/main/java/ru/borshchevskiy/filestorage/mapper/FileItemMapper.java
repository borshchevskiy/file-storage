package ru.borshchevskiy.filestorage.mapper;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.util.FilePathUtil;
import ru.borshchevskiy.filestorage.util.FileSizeUtil;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

/**
 * Class provides methods for converting objects from file storage to {@link FileItemDto} objects.
 */
@Component
@RequiredArgsConstructor
public class FileItemMapper {

    private final UserSessionData userSessionData;

    /**
     * Method creates new {@link FileItemDto} instance based on Minio's {@link Item} instance received.
     *
     * @param item Minio's {@link Item} instance
     * @return new {@link FileItemDto} based on {@link Item} instance received
     */
    public FileItemDto mapToFileItemDto(Item item) {
        FileItemDto fileItemDto = new FileItemDto();
        fileItemDto.setFullName(FilePathUtil.removeUserDirectoryFromPath(userSessionData, item.objectName()));
        fileItemDto.setName(getName(item));
        fileItemDto.setDirectory(item.isDir());
        fileItemDto.setSize(item.size());
        fileItemDto.setViewSize(FileSizeUtil.getViewFileSize(item.size()));
        return fileItemDto;
    }

    /**
     * Method retrieves the name of the file or directory from the Minio's {@link Item}.
     * <p>
     * In case item is directory (has isDir set to true), {@link FilePathUtil#getName(String)} is used.
     * <p>
     * if item is a file, {@link FilePathUtil}'s methods not used.
     * It is done to cover the case with empty directory.
     * For such directory Minio creates a file with empty name and 0 size,
     * so it can't be processed as regular file name.
     *
     * @param item Minio's {@link Item}.
     * @return String, representing the name of the file or directory
     * @see FilePathUtil
     */
    private String getName(Item item) {
        String originalName = item.objectName();

        if (item.isDir()) {
            return FilePathUtil.getName(originalName);
        }

        int lastSeparator = originalName.lastIndexOf('/');

        if (lastSeparator < 0) {
            return originalName;
        }

        return originalName.substring(lastSeparator + 1);
    }
}
