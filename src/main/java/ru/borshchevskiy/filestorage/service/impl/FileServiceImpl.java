package ru.borshchevskiy.filestorage.service.impl;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.mapper.FileItemMapper;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.service.FileService;
import ru.borshchevskiy.filestorage.util.FilePathUtil;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

/**
 *  Class provides methods to manipulate files in storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioRepository minioRepository;
    private final FileItemMapper fileItemMapper;
    private final UserSessionData userSessionData;

    /**
     * Method returns {@link List} of {@link FileItemDto} reflecting files and directories located by specified path.
     * Selection is not recursive, which means that only directories located by specified path are selected, but not
     * their contents.
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path path to objects. Indicates directory where objects are located.
     * @return {@link List} of {@link FileItemDto}.
     */
    @Override
    public List<FileItemDto> getItemsByPath(String path) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        List<Item> itemList = minioRepository.getItemsByPath(storagePath, false);

        return itemList.stream()
                .map(fileItemMapper::mapToFileInfoDto)
                .sorted(Comparator.comparing(FileItemDto::isDirectory).reversed()
                        .thenComparing(FileItemDto::getName))
                .filter(dto -> !dto.getName().isBlank())
                .toList();
    }

    /**
     * Method returns an {@link InputStream} of the file.
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path to directory where required file is located.
     * @param name name of the file.
     * @return {@link InputStream} of the file located by path.
     */
    @Override
    public InputStream downloadFile(String path, String name) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String fullName = storagePath + name;
        log.debug("File " + fullName + " requested for download.");
        return minioRepository.getFile(fullName);
    }

    /**
     * Method saves the file to storage by specified path.
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param inputStream {@link InputStream} of the file to be saved to storage.
     * @param path path to save the file.
     */
    @Override
    public void uploadFile(InputStream inputStream, String path, String name) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String fullPath = storagePath + name;
        minioRepository.putFile(inputStream, fullPath);
        log.debug("File " + fullPath + " saved to storage.");
    }

    /**
     * Method renames specified file.
     * E.g. for storage element = "dir/file.txt":
     * path = "dir/" oldName = "file.txt", newName = "newFile" -> result = "dir/newFile.txt"
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path path to the directory where file is located.
     * @param oldName old name of the file.
     * @param newName new name of the file.
     */
    @Override
    public void renameFile(String path, String oldName, String newName) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String extension = FilePathUtil.getFileExtension(oldName);
        String oldFullPath = storagePath + oldName;
        String newFullPath = storagePath + newName + (extension.isBlank() ? "" : "." + extension);

        minioRepository.copyFile(oldFullPath, newFullPath);
        minioRepository.deleteFile(oldFullPath);
        log.debug("File " + oldFullPath + " was renamed to " + newFullPath + ".");
    }

    /**
     * Method deletes the file.
     * E.g. for storage element = "dir/file.txt":
     * path = "dir/" name = "file.txt" -> result = "dir/"
     * <p>
     * Due to Minio specifics, if the deleted file was the only file in the directory, then this directory
     * and all empty parent directories will also be deleted. To cover this case, path of the file
     * is checked after file deletion. If this path is empty (no items found for this path) it means
     * that Minio deleted it. So in this case this path is recreated.
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path path to the directory where file is located.
     * @param name name of the file to be deleted.
     */
    @Override
    public void deleteFile(String path, String name) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String fullPath = storagePath + name;
        minioRepository.deleteFile(fullPath);
        log.debug("File " + fullPath + " was deleted.");

        List<Item> items = minioRepository.getItemsByPath(storagePath, false);
        if (items.isEmpty() && !path.isEmpty()) {
            minioRepository.createDirectory(storagePath);
        }
    }
}
