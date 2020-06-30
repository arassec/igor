package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.misc.action.MiscActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link SkipAction}.
 */
@DisplayName("'Skip action' tests.")
class SkipActionTest extends MiscActionBaseTest {

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

}
