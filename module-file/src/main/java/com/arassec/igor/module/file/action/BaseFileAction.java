package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.module.file.provider.ListFilesProvider;
import lombok.Data;

import java.util.Map;

/**
 * Base class for file based actions.
 */
public abstract class BaseFileAction extends BaseAction {

    /**
     * Default-Query for the directory.
     */
    protected static final String DIRECTORY_QUERY = "$.data." + ListFilesProvider.DIRECTORY_KEY;

    /**
     * Default-Query for the filename.
     */
    protected static final String FILENAME_QUERY = "$.data." + ListFilesProvider.FILENAME_KEY;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseFileAction(String typeId) {
        super("28ac6145-b4d8-4d09-8e99-b35ac24aae22", typeId);
    }

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
        ResolvedData result = new ResolvedData();

        result.setSourceFilename(getString(data, sourceFilename));
        result.setSourceDirectory(resolveDirectory(data, sourceDirectory));
        result.setTargetFilename(getString(data, targetFilename));
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
        String resolvedFilename = getString(data, filename);
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
        String resolvedDirectory = getString(data, query);

        if (resolvedDirectory == null) {
            resolvedDirectory = "/";
        }

        if (!resolvedDirectory.endsWith("/")) {
            resolvedDirectory += "/";
        }

        return resolvedDirectory;
    }

}
