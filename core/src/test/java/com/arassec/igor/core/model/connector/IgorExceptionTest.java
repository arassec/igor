package com.arassec.igor.core.model.connector;

import com.arassec.igor.core.util.IgorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorException}.
 */
@DisplayName("Connector-Exception Tests")
public class IgorExceptionTest {

    /**
     * Tests the igor-exception's constructors.
     */
    @Test
    @DisplayName("Tests the igor-exception's constructors.")
    void testIgorException() {
        IgorException igorException = new IgorException("test-message");
        assertEquals("test-message", igorException.getMessage());

        IllegalStateException illegalStateException = new IllegalStateException("test-exception");

        igorException = new IgorException("second-message", illegalStateException);
        assertEquals("second-message", igorException.getMessage());
        assertEquals(illegalStateException, igorException.getCause());
    }

}
