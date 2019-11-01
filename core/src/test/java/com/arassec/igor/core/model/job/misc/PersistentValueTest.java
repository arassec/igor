package com.arassec.igor.core.model.job.misc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link PersistentValue} model.
 */
@DisplayName("Persistent-Value Tests")
class PersistentValueTest {

    /**
     * Sometimes you just want to test getters and setters... :)
     */
    @Test
    @DisplayName("Tests content handling.")
    void testContentHandling() {
        Instant now = Instant.now();
        PersistentValue persistentValue = new PersistentValue("content");
        persistentValue.setId(1234L);
        persistentValue.setCreated(now);
        assertEquals("content", persistentValue.getContent());
        assertEquals(1234L, persistentValue.getId());
        assertEquals(now, persistentValue.getCreated());
    }

}
