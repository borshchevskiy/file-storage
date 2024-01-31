package ru.borshchevskiy.filestorage.service.impl;

import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.mapper.FileItemMapper;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.service.SearchService;
import ru.borshchevskiy.filestorage.util.FilePathUtil;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class provides methods to perform search actions in storage.
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final MinioRepository minioRepository;
    private final FileItemMapper fileItemMapper;
    private final UserSessionData userSessionData;

    /**
     * Method performs search of files and directories which contain query value in their path or name.
     * At first all user's items are found, then files and directories are searched separately,
     * then results are combined into final list.
     * <p>
     * Because search is made recursively from the root, all elements in 'allUserItems'
     * will be of 'file' type in Minio's representation (their {@link Item#isDir()} method will return false).
     * It happens because empty folder in Minio must contain an empty file with blank name and zero size.
     * This empty files still can be found during recursive search.
     * Lists of files and directories are defined from user's items
     * in {@link SearchServiceImpl#searchFiles(String, List)} and
     * {@link SearchServiceImpl#searchDirectories(String, List)}.
     *
     * @param query value to be found.
     * @return {@link List} of {@link FileItemDto} reflecting files and directories found.
     */
    @Override
    public List<FileItemDto> search(String query) {
        List<Item> itemsContainQuery = minioRepository
                .getItemsByPath(userSessionData.getUserDirectory(), true)
                .stream()
                .filter(item -> item.objectName().contains(query))
                .toList();

        List<FileItemDto> files = searchFiles(query, itemsContainQuery);
        Set<FileItemDto> directories = searchDirectories(query, itemsContainQuery);

        List<FileItemDto> results = new ArrayList<>();
        results.addAll(directories);
        results.addAll(files);

        return results;
    }

    /**
     * Method used to search directories which contain query value in their path or name.
     * First, method filters all paths that are directory paths (end with '/').
     * After that, each path is checked to find all subpaths corresponding search query.
     * <p>
     * E.g. if storage has the only path = "dir1/dir/dir1/" and search query = "1",
     * correct search result should be 2 paths: "dir<b>1</b>/" and "dir1/dir/dir<b>1</b>/".
     *
     * @param query value to be found.
     * @param items list of all user's objects.
     * @return set of {@link FileItemDto} representing directories found.
     */
    private Set<FileItemDto> searchDirectories(String query, List<Item> items) {
        Set<FileItemDto> directories = new HashSet<>();
        Set<String> pathsContainingQuery = items.stream()
                .map(item -> FilePathUtil.removeUserDirectoryFromPath(userSessionData, item.objectName()))
                .filter(itemName -> itemName.endsWith("/"))
                .collect(Collectors.toSet());

        for (String path : pathsContainingQuery) {
            StringBuilder directoryPath = new StringBuilder();
            for (String pathPart : path.split("/")) {
                directoryPath.append(pathPart).append("/");
                if (pathPart.contains(query)) {

                    String fullName = directoryPath.toString();
                    FileItemDto directory = new FileItemDto();

                    directory.setFullName(fullName);
                    directory.setName(FilePathUtil.getName(fullName));
                    directory.setDirectory(true);
                    directory.setSize(0L);
                    directory.setViewSize("");

                    directories.add(directory);
                }
            }
        }
        return directories;
    }

    /**
     * Method used to search files which contain query value in their names.
     *
     * @param query value to be found.
     * @param items list of all user's objects.
     * @return list of {@link FileItemDto} representing files found.
     */
    private List<FileItemDto> searchFiles(String query, List<Item> items) {
        return items.stream()
                .filter(item -> !item.objectName().endsWith("/"))
                .filter(item -> {
                    String name = FilePathUtil.getName(item.objectName());
                    return name.contains(query);
                })
                .map(fileItemMapper::mapToFileItemDto)
                .toList();
    }
}
