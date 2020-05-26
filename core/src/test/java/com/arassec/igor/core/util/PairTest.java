package com.arassec.igor.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link Pair}.
 */
class PairTest {

    /**
     * Tests comparing two pairs.
     */
    @Test
    @DisplayName("Tests pair comparison.")
    void testPairComparison() {
        Pair<String, Integer> pair = new Pair<>();
        pair.setKey("test");
        pair.setValue(666);

        Pair<String, Integer> anotherPair = new Pair<>("test", 666);

        assertEquals(pair, anotherPair);
    }

}
