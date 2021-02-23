package com.arassec.igor.plugin.file.connector.ssh;

import com.arassec.igor.core.util.StacktraceFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link JobCancelledException}.
 */
@DisplayName("Tests the Job-Cancelled exception")
class JobCancelledExceptionTest {

    /**
     * Tests the constructor argument of the exception.
     */
    @Test
    @DisplayName("Tests the constructor argument of the exception.")
    void testConstructorArgument() {
        JobCancelledException exception = new JobCancelledException("exception-message");
        assertTrue(StacktraceFormatter.format(exception).startsWith("com.arassec.igor.module.file.connector.ssh" +
                ".JobCancelledException: exception-message"));
    }

}
