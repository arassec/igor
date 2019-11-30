package com.arassec.igor.web.controller;

import com.arassec.igor.web.model.KeyLabelStore;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Base class for REST-Controllers.
 */
public interface BaseRestController {

    /**
     * Sorts the supplied {@link KeyLabelStore} items by their label.
     *
     * @param input The {@link KeyLabelStore}s to sort.
     * @return The sorted list.
     */
    default List<KeyLabelStore> sortByLabel(Set<KeyLabelStore> input) {
        List<KeyLabelStore> result = new LinkedList<>(input);
        result.sort(Comparator.comparing(KeyLabelStore::getValue));
        return result;
    }

}
