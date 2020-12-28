package com.arassec.igor.plugin.core;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link CoreUtils}.
 */
@DisplayName("'Common Utils' tests.")
class CoreUtilsTest {

    /**
     * Tests appending an optional suffix to a file name.
     */
    @Test
    @DisplayName("Tests appending an optional suffix to a file name.")
    void testAppendSuffixIfRequired() {
        assertEquals("file.txt", CoreUtils.appendSuffixIfRequired("file.txt", null, false));
        assertEquals("file", CoreUtils.appendSuffixIfRequired("file", ".igor", false));
        assertEquals("file.igor", CoreUtils.appendSuffixIfRequired("file", "igor", true));
        assertEquals("file.igor", CoreUtils.appendSuffixIfRequired("file", ".igor", true));
    }

    /**
     * Tests combining a path and a filename to an absolute path.
     */
    @Test
    @DisplayName("Tests combining a path and a filename to an absolute path.")
    void testCombineFilePath() {
        assertEquals("/file.txt", CoreUtils.combineFilePath(null, "file.txt"));
        assertEquals("directory/file.txt", CoreUtils.combineFilePath("directory", "file.txt"));
        assertEquals("directory/file.txt", CoreUtils.combineFilePath("directory/", "file.txt"));
    }

}
