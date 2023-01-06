package com.arassec.igor.plugin.core;

import com.arassec.igor.core.util.IgorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility to help with problems in the core plugin and descendant plugins.
 */
public class CorePluginUtils {

    /**
     * File-suffix appended to files during transfer.
     */
    public static final String FILE_IN_TRANSFER_SUFFIX = ".igor";

    /**
     * Jackson's ObjectMapper to convert JSON.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new instance.
     */
    private CorePluginUtils() {
    }

    /**
     * Appends the suffix to the provided filename if required and the suffix is provided.
     *
     * @param file                 The file.
     * @param suffix               An optional file suffix to append to the filename.
     * @param appendFiletypeSuffix Set to {@code true}, if the suffix should be added (if it exists), {@code false} otherwise.
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
     * @return The template executed against the data or the original template if either of the input parameters is {@code null}
     * or the template is invalid.
     */
    public static String evaluateTemplate(Map<String, Object> data, String template) {
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
     * @return A new Map instance with copied content.
     */
    public static Map<String, Object> clone(Map<String, Object> data) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(data), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IgorException("Could not clone data item!", e);
        }
    }

    /**
     * Converts the supplied JSON into a Map.
     *
     * @param json The JSON to convert.
     * @return The JSON as Map.
     */
    public static Map<String, Object> jsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IgorException("Could not convert JSON string.", e);
        }
    }

    /**
     * Adds a value to the data item at the given path. If the path does not yet exist, new elements are
     * added to the data item.
     *
     * @param data  The data item to put the value in.
     * @param path  Path to the position of the value, dot separated.
     * @param value The value to put in the data item.
     */
    @SuppressWarnings("unchecked")
    public static void putValue(Map<String, Object> data, String path, Object value) {
        if (data == null || !StringUtils.hasText(path) || value == null) {
            return;
        }

        Map<String, Object> subData = data;

        String[] pathParts = path.split("\\.");
        if (pathParts.length > 0) {
            for (int i = 0; i < pathParts.length - 1; i++) {
                subData.computeIfAbsent(pathParts[i], s -> new HashMap<String, Object>());
                subData = ((Map<String, Object>) subData.get(pathParts[i]));
            }
            subData.put(pathParts[pathParts.length - 1], value);
        }
    }

    /**
     * Gets a value from a data item at the given path.
     *
     * @param data  The data item to get the value from.
     * @param path  The path to the key of the desired value, dot separated.
     * @param clazz The class with the return type.
     * @param <T>   The type of the returned value.
     * @return Optional containing the value or null, if no value was found or matched the desired return type.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Optional<T> getValue(Map<String, Object> data, String path, Class<T> clazz) {
        if (data == null || !StringUtils.hasText(path) || clazz == null) {
            return Optional.empty();
        }

        Map<String, Object> subData = data;

        String[] pathParts = path.split("\\.");
        if (pathParts.length > 0) {
            for (int i = 0; i < pathParts.length - 1; i++) {
                if (subData.containsKey(pathParts[i]) && subData.get(pathParts[i]) instanceof Map valueMap) {
                    subData = valueMap;
                } else {
                    return Optional.empty();
                }
            }
            String key = pathParts[pathParts.length - 1];
            if (subData.containsKey(key) && clazz.isAssignableFrom(subData.get(key).getClass())) {
                return Optional.of((T) subData.get(key));
            }
        }

        return Optional.empty();
    }

    /**
     * Removes the value from the supplied data item at the given path.
     *
     * @param data The data item.
     * @param path The path to the value, dot separated.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void removeValue(Map<String, Object> data, String path) {
        if (data == null || !StringUtils.hasText(path)) {
            return;
        }

        Map<String, Object> subData = data;

        String[] pathParts = path.split("\\.");
        if (pathParts.length > 0) {
            for (int i = 0; i < pathParts.length - 1; i++) {
                if (subData.containsKey(pathParts[i]) && subData.get(pathParts[i]) instanceof Map valueMap) {
                    subData = valueMap;
                } else {
                    return;
                }
            }
            subData.remove(pathParts[pathParts.length - 1]);
        }
    }

}
