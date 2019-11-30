package com.arassec.igor.core.util;

import java.util.List;

/**
 * Utility to help with {@link ModelPage}s.
 */
public class ModelPageHelper {

    /**
     * Prevents instantiation.
     */
    private ModelPageHelper() {
    }

    /**
     * Creates the requested model page for the given number of elements.
     *
     * @param all        All available elements.
     * @param pageNumber The desired page number.
     * @param pageSize   The desired page size.
     * @param <T>        The model class.
     *
     * @return The {@link ModelPage}.
     */
    public static <T> ModelPage<T> getModelPage(List<T> all, int pageNumber, int pageSize) {
        if (all != null && !all.isEmpty()) {
            long totalPages = all.size() / pageSize;
            if (all.size() % pageSize > 0) {
                totalPages++;
            }
            if (totalPages > 0 && totalPages > pageNumber) {
                int startIndex = pageNumber * pageSize;
                int endIndex = startIndex + pageSize;
                if (endIndex >= all.size()) {
                    endIndex = all.size();
                }
                return new ModelPage<>(pageNumber, pageSize, totalPages, all.subList(startIndex, endIndex));
            }
        }
        return new ModelPage<>(pageNumber, 0, 0, List.of());
    }

}
