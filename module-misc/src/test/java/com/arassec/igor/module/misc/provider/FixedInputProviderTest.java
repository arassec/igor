package com.arassec.igor.module.misc.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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

    /**
     * Tests providing JSON as input.
     */
    @Test
    @DisplayName("Tests providing JSON as input.")
    @SuppressWarnings("unchecked")
    void testProvideJson() {
        FixedInputProvider provider = new FixedInputProvider();
        provider.setInput("{\"a\": \"b\"}\n{\"c\": 123}");
        provider.setSeparateLines(true);

        provider.initialize("job-id", new JobExecution());

        assertTrue(provider.hasNext());
        Map<String, Object> json = (Map<String, Object>) provider.next().get(FixedInputProvider.INPUT_KEY);
        assertEquals("b", json.get("a"));

        assertTrue(provider.hasNext());
        json = (Map<String, Object>) provider.next().get(FixedInputProvider.INPUT_KEY);
        assertEquals(123, json.get("c"));

        assertFalse(provider.hasNext());
    }

    /**
     * Tests providing JSON as input with included newlines.
     */
    @Test
    @DisplayName("Tests providing JSON as input with included newlines.")
    @SuppressWarnings("unchecked")
    void testProvideJsonWithNewlineIncluded() {
        FixedInputProvider provider = new FixedInputProvider();
        provider.setInput("{\"a\": \"b\",\n\"c\": 123}");
        provider.setSeparateLines(false);

        provider.initialize("job-id", new JobExecution());

        assertTrue(provider.hasNext());
        Map<String, Object> json = (Map<String, Object>) provider.next().get(FixedInputProvider.INPUT_KEY);
        assertEquals("b", json.get("a"));
        assertEquals(123, json.get("c"));

        assertFalse(provider.hasNext());
    }

    /**
     * Tests providing a JSON-Array as input.
     */
    @Test
    @DisplayName("Tests providing a JSON-Array as input.")
    @SuppressWarnings("unchecked")
    void testProvideJsonArray() {
        FixedInputProvider provider = new FixedInputProvider();
        provider.setInput("[\"a\", \"b\", \"c\"]");
        provider.setSeparateLines(false);

        provider.initialize("job-id", new JobExecution());

        assertTrue(provider.hasNext());
        List<String> jsonArray = (List<String>) provider.next().get(FixedInputProvider.INPUT_KEY);
        assertEquals("a", jsonArray.get(0));
        assertEquals("b", jsonArray.get(1));
        assertEquals("c", jsonArray.get(2));

        assertFalse(provider.hasNext());
    }

}