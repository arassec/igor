package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link SkipAction}.
 */
@DisplayName("'Skip action' tests.")
class SkipActionTest extends CoreActionBaseTest {

    /**
     * Tests skipping data items.
     */
    @Test
    @DisplayName("Tests skipping data items.")
    void testProcess() {
        SkipAction skipAction = new SkipAction();
        skipAction.setNumber(2);

        assertTrue(skipAction.process(createData(), new JobExecution()).isEmpty());
        assertTrue(skipAction.process(createData(), new JobExecution()).isEmpty());
        assertFalse(skipAction.process(createData(), new JobExecution()).isEmpty());
    }

    /**
     * Tests that the action doesn't support events.
     */
    @Test
    @DisplayName("Tests that the action doesn't support events.")
    void testSupportsEvents() {
        SkipAction skipAction = new SkipAction();
        assertFalse(skipAction.supportsEvents());
    }

    /**
     * Tests, that the action always enforces single-thread execution.
     */
    @Test
    @DisplayName("Tests, that the action always enforces single-thread execution.")
    void testEnforceSingleThread() {
        SkipAction skipAction = new SkipAction();
        assertTrue(skipAction.enforceSingleThread());
    }

}
