package com.arassec.igor.plugin.common;

import org.springframework.util.StringUtils;

/**
 * Utility to help with problems in the common plugin.
 */
public class CommonUtils {

    /**
     * File-suffix appended to files during transfer.
     */
    public static final String FILE_IN_TRANSFER_SUFFIX = ".igor";

    /**
     * Creates a new instance.
     */
    private CommonUtils() {
    }

    /**
     * Appends the suffix to the provided filename if required and the suffix is provided.
     *
     * @param file                 The file.
     * @param suffix               An optional file suffix to append to the filename.
     * @param appendFiletypeSuffix Set to {@code true}, if the suffix should be added (if it exists), {@code false} otherwise.
     *
     * @return The file with the appended suffix.
     */
    public static String appendSuffixIfRequired(String file, String suffix, boolean appendFiletypeSuffix) {
        String targetFile = file;
        if (StringUtils.hasText(suffix) && appendFiletypeSuffix) {
            if (!suffix.startsWith(".")) {
                suffix = "." + suffix;
            }
            targetFile += suffix;
        }
        return targetFile;
    }

    /**
     * Combines the provided directory with the provided file. Adds a separator if needed.
     *
     * @param directory The path to the source directory.
     * @param file      The filename.
     *
     * @return The path with the added filename.
     */
    public static String combineFilePath(String directory, String file) {
        if (directory == null) {
            directory = "";
        }
        if (!directory.endsWith("/")) {
            directory += "/";
        }
        return directory + file;
    }

}
