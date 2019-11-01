package com.arassec.igor.core.util;

import com.arassec.igor.core.model.service.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link StacktraceFormatter} utility.
 */
@DisplayName("Stacktrace-Formatter Tests")
class StacktraceFormatterTest {

    /**
     * Tests formatting stack-traces.
     */
    @Test
    @DisplayName("Tests formatting a throwable.")
    void testFormat() {
        assertNull(StacktraceFormatter.format(null));
        assertTrue(StacktraceFormatter.format(new ServiceException("test")).startsWith("com.arassec.igor.core.model.service.ServiceException: test"));
    }

}
