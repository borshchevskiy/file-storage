package ru.borshchevskiy.filestorage.util;

import ru.borshchevskiy.filestorage.web.breadcrumbs.Breadcrumbs;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to manipulate file paths.
 */
public class FilePathUtil {

    /**
     * Method adds root user directory to the beginning of the path.
     * According to storage structure each user has its personal directory
     * which is root for all other directories and files.
     *
     * @param userSessionData - session-scoped object which contains user's data,
     *                        including personal root directory name.
     * @param path            path to which personal directory should be added.
     * @return path with added personal directory.
     * @see UserSessionData
     */
    public static String addUserDirectoryToPath(UserSessionData userSessionData, String path) {
        return String.join("/", userSessionData.getUserDirectory(), path);
    }

    /**
     * Method removes root user directory from the beginning of the path.
     * According to storage structure each user has its personal directory
     * which is root for all other directories and files.
     *
     * @param userSessionData - session-scoped object which contains user's data,
     *                        including personal root directory name.
     * @param path            path from which personal directory should be removed.
     * @return path with added personal directory.
     * @see UserSessionData
     */
    public static String removeUserDirectoryFromPath(UserSessionData userSessionData, String path) {
        return path.replaceFirst(userSessionData.getUserDirectory() + "/", "");
    }

    /**
     * Retrieves file extension.
     * Method behavior:
     * <pre>
     *     FilePathUtil.getFileExtension(null) = ""
     *     FilePathUtil.getFileExtension("") = ""
     *     FilePathUtil.getFileExtension("file") = ""
     *     FilePathUtil.getFileExtension("file.") = ""
     *     FilePathUtil.getFileExtension("file.txt") = "txt"
     *     FilePathUtil.getFileExtension("dir./file") = ""
     *     FilePathUtil.getFileExtension("dir./file.txt") = "txt"
     * </pre>
     *
     * @param file name of the file.
     * @return fle extension.
     */
    public static String getFileExtension(String file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        int dotLastIndex = file.lastIndexOf('.');

        if (dotLastIndex < 0 || dotLastIndex == file.length() - 1) {
            return "";
        }

        int separatorLastIndex = file.lastIndexOf('/');

        return separatorLastIndex > dotLastIndex
                ? ""
                : file.substring(dotLastIndex + 1);
    }

    /**
     * Retrieves parent path for the last element in the path.
     * Method behavior:
     * <pre>
     *     FilePathUtil.getParent(null) = ""
     *     FilePathUtil.getParent("") = ""
     *     FilePathUtil.getParent("file") = ""
     *     FilePathUtil.getParent("file.txt") = ""
     *     FilePathUtil.getParent("dir/file.txt") = "dir/"
     *     FilePathUtil.getParent("dir/) = ""
     *     FilePathUtil.getParent("dir/subdir/") = "dir/"
     *     FilePathUtil.getParent("dir/subdir/file.txt") = "dir/subdir/"
     * </pre>
     * @param path path for which parent should be found.
     * @return parent path for the last element in the specified path.
     */
    public static String getParent(String path) {

        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSeparator = path.lastIndexOf('/');

        if (lastSeparator < 0) {
            return "";
        }
        if (path.endsWith("/")) {
            int secondLastSeparator = path.lastIndexOf('/', lastSeparator - 1);
            if (secondLastSeparator < 0) {
                return "";
            }
            return path.substring(0, secondLastSeparator + 1);
        }
        return path.substring(0, lastSeparator + 1);
    }

    /**
     * Retrieves the name of the last element on the path (file or directory).
     * Method behavior:
     * <pre>
     *     FilePathUtil.getName(null) = ""
     *     FilePathUtil.getName("") = ""
     *     FilePathUtil.getName("file") = "file"
     *     FilePathUtil.getName("dir/file") = "file"
     *     FilePathUtil.getName("dir/") = "dir/"
     *     FilePathUtil.getName("dir/subdir/") = "subdir/"
     * </pre>
     *
     * @param path file or directory path.
     * @return file or directory name.
     */
    public static String getName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSeparator = path.lastIndexOf('/');

        if (path.endsWith("/")) {
            int secondLastSeparator = path.lastIndexOf('/', lastSeparator - 1);
            if (secondLastSeparator < 0) {
                return path;
            }
            return path.substring(secondLastSeparator + 1);
        }

        if (lastSeparator < 0) {
            return path;
        }

        return path.substring(lastSeparator + 1);
    }

    /**
     * Generates {@link Breadcrumbs} object based on path.
     *
     * @param path based on which {@link Breadcrumbs} should be generated.
     * @return new {@link Breadcrumbs} object.
     */
    public static Breadcrumbs generateBreadcrumbs(String path) {
        Breadcrumbs breadcrumbs = new Breadcrumbs();
        breadcrumbs.setPath(path);

        if (path.isEmpty()) {
            breadcrumbs.setCrumbsNames(Collections.emptyList());
            breadcrumbs.setCrumbsLinks(Collections.emptyList());
            return breadcrumbs;
        }

        String[] names = path.split("/");

        breadcrumbs.setCrumbsNames(Arrays.asList(names));

        StringBuilder link = new StringBuilder();
        List<String> links = new ArrayList<>();

        for (String name : names) {
            link.append(name).append("/");
            links.add(link.toString());
        }

        breadcrumbs.setCrumbsLinks(links);

        return breadcrumbs;
    }

}
