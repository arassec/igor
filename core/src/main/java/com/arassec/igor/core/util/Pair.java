package com.arassec.igor.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Models a key-value-pair.
 *
 * @param <K> The Key.
 * @param <V> The value.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pair<K, V> {

    /**
     * The key.
     */
    private K key;

    /**
     * The value.
     */
    private V value;

}
