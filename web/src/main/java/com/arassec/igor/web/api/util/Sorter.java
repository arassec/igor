package com.arassec.igor.web.api.util;

import com.arassec.igor.core.application.factory.util.KeyLabelStore;

import java.util.*;

/**
 * Utility class to sort things.
 */
public class Sorter {

    /**
     * Sorts the supplied {@link KeyLabelStore} items by their label.
     *
     * @param input The {@link KeyLabelStore}s to sort.
     * @return The sorted list.
     */
    public static final List<KeyLabelStore> sortByLabel(Set<KeyLabelStore> input) {
        List<KeyLabelStore> result = new LinkedList(input);
        Collections.sort(result, Comparator.comparing(KeyLabelStore::getLabel));
        return result;
    }

}
