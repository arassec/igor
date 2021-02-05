package com.arassec.igor.core.model.job.misc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ProcessingFinishedCallback}.
 */
@DisplayName("Tests the processing-finished callback.")
class ProcessingFinishedCallbackTest {

    /**
     * The base class under test.
     */
    private final ProcessingFinishedCallback processingFinishedCallback = mock(ProcessingFinishedCallback.class,
            withSettings().defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests the default implementation of the callback.
     */
    @Test
    @DisplayName("Tests the default implementation of the callback.")
    void testProcessingFinished() {
        Map<String, Object> dataItem = Map.of();
        assertDoesNotThrow(() -> processingFinishedCallback.processingFinished(dataItem));
    }

}
