package com.arassec.igor.module.misc.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link FixedInputProvider}.
 */
@DisplayName("'Fixed input provider' tests.")
class FixedInputProviderTest {

    /**
     * Tests the provider.
     */
    @Test
    @DisplayName("Tests providing fixed input.")
    void testProvide() {
        FixedInputProvider provider = new FixedInputProvider();
        provider.setInput("abc\ndef");
        provider.setSeparateLines(true);

        provider.initialize("job-id", new JobExecution());

        assertTrue(provider.hasNext());
        assertEquals("abc", provider.next().get(FixedInputProvider.INPUT_KEY));
        assertTrue(provider.hasNext());
        assertEquals("def", provider.next().get(FixedInputProvider.INPUT_KEY));
        assertFalse(provider.hasNext());
    }

    /**
     * Tests keeping newline characters as part of the input.
     */
    @Test
    @DisplayName("Tests providing input with included newlines.")
    void testProvideWithNewlineIncluded() {
        FixedInputProvider provider = new FixedInputProvider();
        provider.setInput("abc\ndef");
        provider.setSeparateLines(false);

        provider.initialize("job-id", new JobExecution());

        assertTrue(provider.hasNext());
        assertEquals("abc\ndef", provider.next().get(FixedInputProvider.INPUT_KEY));
        assertFalse(provider.hasNext());
    }

}