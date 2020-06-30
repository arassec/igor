package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.misc.action.MiscActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link LimitAction}.
 */
@DisplayName("'Limit action' tests.")
class LimitActionTest extends MiscActionBaseTest {

    /**
     * Tests limiting data items.
     */
    @Test
    @DisplayName("Tests limiting data items.")
    void testProcess() {
        LimitAction limitAction = new LimitAction();
        limitAction.setNumber(2);

        assertFalse(limitAction.process(createData(), new JobExecution()).isEmpty());
        assertFalse(limitAction.process(createData(), new JobExecution()).isEmpty());
        assertTrue(limitAction.process(createData(), new JobExecution()).isEmpty());
        assertTrue(limitAction.process(createData(), new JobExecution()).isEmpty());
    }

}
