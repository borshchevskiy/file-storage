package ru.borshchevskiy.filestorage.mapper;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.util.FilePathUtil;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Class provides methods for converting objects from file storage to {@link FileItemDto} objects.
 */
@Component
@RequiredArgsConstructor
public class FileItemMapper {

    private final UserSessionData userSessionData;
    /**
     * File size conversion factor. Used to convert bytes to human-readable representation.
     */
    private final Long FILE_SIZE_FACTOR = 1024L;

    /**
     * Method creates new {@link FileItemDto} instance based on Minio's {@link Item} instance received.
     *
     * @param item Minio's {@link Item} instance
     * @return new {@link FileItemDto} based on {@link Item} instance received
     */
    public FileItemDto mapToFileInfoDto(Item item) {
        FileItemDto fileItemDto = new FileItemDto();
        fileItemDto.setFullName(FilePathUtil.removeUserDirectoryFromPath(userSessionData, item.objectName()));
        fileItemDto.setName(getName(item));
        fileItemDto.setEncodedFullName(URLEncoder.encode(
                FilePathUtil.removeUserDirectoryFromPath(userSessionData, item.objectName()),
                StandardCharsets.UTF_8));
        fileItemDto.setDirectory(item.isDir());
        fileItemDto.setSize(item.size());
        fileItemDto.setViewSize(getViewSize(item));
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
     *
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

    /**
     * Method converts file's size from bytes to human-readable string.
     * E.g. size 10240 bytes -> "10 Kb".
     * {@link #FILE_SIZE_FACTOR} is used for conversion.
     *
     * @param item Minio's {@link Item}
     * @return String, representing human-readable value of file's size
     * @see HumanReadableFileSizeUnits
     */
    private String getViewSize(Item item) {
        long bytes = item.size();
        int sizeCounter = 0;

        if (bytes < FILE_SIZE_FACTOR) {
            return bytes + " " + HumanReadableFileSizeUnits.values()[sizeCounter].name();
        }

        sizeCounter++;
        long sizeBreakPoint = FILE_SIZE_FACTOR * FILE_SIZE_FACTOR;

        while (bytes >= sizeBreakPoint
                && sizeCounter < HumanReadableFileSizeUnits.values().length) {

            bytes /= FILE_SIZE_FACTOR;
            sizeCounter++;
        }

        return String.format("%.1f %s",
                bytes / Double.valueOf(FILE_SIZE_FACTOR),
                HumanReadableFileSizeUnits.values()[sizeCounter].name());
    }

}
