package com.arassec.igor.plugin.common;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link CommonUtils}.
 */
@DisplayName("'Common Utils' tests.")
class CommonUtilsTest {

    /**
     * Tests appending an optional suffix to a file name.
     */
    @Test
    @DisplayName("Tests appending an optional suffix to a file name.")
    void testAppendSuffixIfRequired() {
        assertEquals("file.txt", CommonUtils.appendSuffixIfRequired("file.txt", null, false));
        assertEquals("file", CommonUtils.appendSuffixIfRequired("file", ".igor", false));
        assertEquals("file.igor", CommonUtils.appendSuffixIfRequired("file", "igor", true));
        assertEquals("file.igor", CommonUtils.appendSuffixIfRequired("file", ".igor", true));
    }

    /**
     * Tests combining a path and a filename to an absolute path.
     */
    @Test
    @DisplayName("Tests combining a path and a filename to an absolute path.")
    void testCombineFilePath() {
        assertEquals("/file.txt", CommonUtils.combineFilePath(null, "file.txt"));
        assertEquals("directory/file.txt", CommonUtils.combineFilePath("directory", "file.txt"));
        assertEquals("directory/file.txt", CommonUtils.combineFilePath("directory/", "file.txt"));
    }

}
