package com.arassec.igor.plugin.core.test.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import com.arassec.igor.plugin.core.CorePluginCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link DuplicateAction}.
 */
@DisplayName("'Duplicate action' tests.")
class DuplicateActionTest extends CoreActionBaseTest {

    /**
     * Tests initialization of the action.
     */
    @Test
    @DisplayName("Tests initialization of the action.")
    void testInitialize() {
        DuplicateAction action = new DuplicateAction();
        assertEquals(CorePluginCategory.TEST.getId(), action.getCategoryId());
        assertEquals("duplicate-action", action.getTypeId());
    }

    /**
     * Tests duplicating data items with the action.
     */
    @Test
    @DisplayName("Tests duplicating data items with the action.")
    void testProcess() {
        DuplicateAction action = new DuplicateAction();

        Map<String, Object> data = createData();

        List<Map<String, Object>> result = action.process(data, new JobExecution());
        assertEquals(0, result.size());

        action.setAmount(3);

        result = action.process(data, new JobExecution());
        assertEquals(3, result.size());

        assertEquals(getString(result.get(0), "$"), getString(result.get(1), "$"));
        assertEquals(getString(result.get(0), "$"), getString(result.get(2), "$"));
    }

}
