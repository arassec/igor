package com.arassec.igor.web.api;

import com.arassec.igor.core.util.KeyLabelStore;

import java.util.*;

/**
 * Base class for REST-Controllers.
 */
public abstract class BaseRestController {

    /**
     * Sorts the supplied {@link KeyLabelStore} items by their label.
     *
     * @param input The {@link KeyLabelStore}s to sort.
     * @return The sorted list.
     */
    List<KeyLabelStore> sortByLabel(Set<KeyLabelStore> input) {
        List<KeyLabelStore> result = new LinkedList<>(input);
        result.sort(Comparator.comparing(KeyLabelStore::getValue));
        return result;
    }

}
