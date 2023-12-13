package ru.borshchevskiy.filestorage.service.impl;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.service.DirectoryService;
import ru.borshchevskiy.filestorage.util.FilePathUtil;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.util.List;

/**
 *  Class provides methods to manipulate directories in storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final MinioRepository minioRepository;
    private final UserSessionData userSessionData;

    /**
     * Method creates new directory on specified path.
     * E.g. for storage element = "dir/subdir/":
     * path = "dir/subdir/", newDirectoryName = "newdir" -> result = "dir/subdir/newdir/".
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path path where new directory should be created.
     * @param directoryName name of the new directory.
     */
    @Override
    public void createDirectory(String path, String directoryName) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String newDirectoryPath = storagePath + directoryName + "/";
        minioRepository.createDirectory(newDirectoryPath);
        log.debug("Directory " + newDirectoryPath + " created.");
    }
    /**
     * Method renames directory on specified path.
     * E.g. for storage element = "dir/olddir/":
     * path = "dir/", oldName = "olddir/", newName = "newdir" -> result = "dir/newdir/".
     * <p>
     * Renaming is performed by changing directory's name in paths of all items which has specified directory in it.
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path path to the directory which should be renamed.
     * @param oldName old name of the directory.
     * @param newName new name of the new directory.
     */
    @Override
    public void renameDirectory(String path, String oldName, String newName) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String oldFullPath = storagePath + oldName;
        String newFullPath = storagePath + newName + "/";

        List<String> oldPathFileNames = minioRepository.getItemsByPath(oldFullPath, true).stream()
                .map(Item::objectName)
                .toList();

        for (String oldPathFileName : oldPathFileNames) {
            String newPathFileName = oldPathFileName.replaceFirst(oldFullPath, newFullPath);
            minioRepository.copyFile(oldPathFileName, newPathFileName);
            minioRepository.deleteFile(oldPathFileName);
        }
        log.debug("Directory " + oldFullPath + " renamed to " + newFullPath + ".");
    }
    /**
     * Method deletes directory on specified path.
     * E.g. for storage element = "dir/subdir/": path = "dir/", name = "subdir/" -> result = "dir/".
     * <p>
     * Due to Minio specifics, if parent directories for deleted directory were empty they are also deleted.
     * To cover this case, path to the directory is checked after deletion.
     * If this path is empty (no items found for this path) it means that Minio deleted it.
     * So in this case this path is recreated.
     * <p>
     * Path parameter value is used to create storage-specific path value.
     * @param path path to the directory to be deleted is located.
     * @param name name of the directory which should be deleted.
     */
    @Override
    public void deleteDirectory(String path, String name) {
        String storagePath = FilePathUtil.addUserDirectoryToPath(userSessionData, path);
        String fullPath = storagePath + name;
        minioRepository.deleteDirectory(fullPath);

        List<Item> items = minioRepository.getItemsByPath(path, false);
        if (items.isEmpty() && !path.isEmpty()) {
            minioRepository.createDirectory(storagePath);
        }
        log.debug("Directory " + fullPath + " deleted.");
    }
}
