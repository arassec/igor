package com.arassec.igor.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Models a subset of all items of a certain type. Used for paging through a large list of items.
 *
 * @param <T> The type of this page.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelPage<T> {

    /**
     * The current page number.
     */
    private int number;

    /**
     * The current page size.
     */
    private int size;

    /**
     * The total number of pages.
     */
    private long totalPages;

    /**
     * The list of items of this page.
     */
    private List<T> items;

}
