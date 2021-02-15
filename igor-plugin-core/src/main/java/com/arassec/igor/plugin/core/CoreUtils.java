package com.arassec.igor.plugin.core;

import com.arassec.igor.core.util.IgorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Utility to help with problems in the core plugin and descendant plugins.
 */
public class CoreUtils {

    /**
     * File-suffix appended to files during transfer.
     */
    public static final String FILE_IN_TRANSFER_SUFFIX = ".igor";

    /**
     * Creates a new instance.
     */
    private CoreUtils() {
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

    /**
     * Returns the result of the template executed against the provided data. If the template is invalid with regard to the input
     * data, it is returned without modifications.
     *
     * @param data     The data to execute the query on.
     * @param template The mustache template.
     *
     * @return The template executed against the data or the original template if either of the input parameters is {@code null}
     * or the template is invalid.
     */
    public static String getString(Map<String, Object> data, String template) {
        if (data == null || template == null) {
            return null;
        }
        try {
            return Mustache.compiler().compile(template).execute(data);
        } catch (MustacheException e) {
            return template;
        }
    }

    /**
     * Creates a deep copy of the supplied map.
     *
     * @param data The map to clone.
     *
     * @return A new Map instance with copied content.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> clone(Map<String, Object> data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(mapper.writeValueAsString(data), Map.class);
        } catch (JsonProcessingException e) {
            throw new IgorException("Could not clone data item!", e);
        }
    }

}
