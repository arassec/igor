package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.plugin.core.CorePluginUtils;
import lombok.Data;

import java.util.Map;

/**
 * Base class for file based actions.
 */
public abstract class BaseFileAction extends BaseAction {

    /**
     * The key to the filename.
     */
    public static final String FILENAME_KEY = "filename";

    /**
     * The key to the directory.
     */
    public static final String DIRECTORY_KEY = "directory";

    /**
     * The key to the file's last modification timestamp.
     */
    public static final String LAST_MODIFIED_KEY = "lastModified";

    /**
     * Default-Template for the directory.
     */
    protected static final String DIRECTORY_TEMPLATE = "{{data." + DIRECTORY_KEY + "}}";

    /**
     * Default-Template for the filename.
     */
    protected static final String FILENAME_TEMPLATE = "{{data." + FILENAME_KEY + "}}";

    /**
     * Resolves the supplied variables and returns the result.
     *
     * @param data            The original data to process.
     * @param sourceFilename  The configured source filename parameter.
     * @param sourceDirectory The configured source directory parameter.
     * @param targetFilename  The configured target filename parameter.
     * @param targetDirectory The configured target directory parameter.
     *
     * @return The resolved parameter values as {@link ResolvedData}.
     */
    ResolvedData resolveData(Map<String, Object> data, String sourceFilename, String sourceDirectory, String targetFilename, String targetDirectory) {
        var result = new ResolvedData();

        result.setSourceFilename(CorePluginUtils.getString(data, sourceFilename));
        result.setSourceDirectory(resolveDirectory(data, sourceDirectory));
        result.setTargetFilename(CorePluginUtils.getString(data, targetFilename));
        result.setTargetDirectory(resolveDirectory(data, targetDirectory));

        if (result.getSourceFilename() == null || result.getTargetFilename() == null) {
            if (isSimulation(data)) {
                data.put(DataKey.SIMULATION_LOG.getKey(), "Couldn't resolve variables: source (" + result.getSourceFilename() +
                        ") / target (" + result.getTargetFilename() + ")");
            }
            return null;
        }

        return result;
    }

    /**
     * Resolves the supplied variables and returns the result as path to the file.
     *
     * @param data      The original data to process.
     * @param filename  The configured filename parameter.
     * @param directory The configured directory parameter.
     *
     * @return The resolved file path.
     */
    String resolveFilePath(Map<String, Object> data, String filename, String directory) {
        var resolvedFilename = CorePluginUtils.getString(data, filename);
        String resolvedDirectory = resolveDirectory(data, directory);

        if (resolvedFilename == null) {
            if (isSimulation(data)) {
                data.put(DataKey.SIMULATION_LOG.getKey(), "Couldn't resolve variable: " + filename);
            }
            return null;
        }

        return resolvedDirectory + resolvedFilename;
    }

    /**
     * Container for resolved configuration parameter values.
     */
    @Data
    static class ResolvedData {

        /**
         * The resolved source filename.
         */
        String sourceFilename;

        /**
         * The resolved soruce directory.
         */
        String sourceDirectory;

        /**
         * The resolved target filename.
         */
        String targetFilename;

        /**
         * The resolved target directory.
         */
        String targetDirectory;

    }

    /**
     * Resolves a directory from the supplied data.
     *
     * @param data  The data.
     * @param query The query to get the directory with.
     *
     * @return A directory (or path) as String.
     */
    private String resolveDirectory(Map<String, Object> data, String query) {
        var resolvedDirectory = CorePluginUtils.getString(data, query);

        if (resolvedDirectory == null) {
            resolvedDirectory = "/";
        }

        if (!resolvedDirectory.endsWith("/")) {
            resolvedDirectory += "/";
        }

        return resolvedDirectory;
    }

}
