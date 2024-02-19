package ru.borshchevskiy.filestorage.util;

/**
 * Utility class to manipulate file size values.
 */
public class FileSizeUtil {
    /**
     * File size conversion factor. Used to convert bytes to human-readable representation.
     */
    private static final Long FILE_SIZE_FACTOR = 1024L;

    /**
     * Method converts file's size from bytes to human-readable string.
     * E.g. size 10240 bytes -> "10 Kb".
     * {@link #FILE_SIZE_FACTOR} is used for conversion.
     *
     * @param bytes size in bytes
     * @return String, representing human-readable value of file's size
     * @see HumanReadableFileSizeUnits
     */
    public static String getViewFileSize(Long bytes) {
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
