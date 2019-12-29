package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.misc.action.BaseMiscActionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link FilterByRegExpAction}.
 */
@DisplayName("'Filter by regular expression' Tests.")
class FilterByRegExpActionTest extends BaseMiscActionTest {

    /**
     * Tests configuration errors.
     */
    @Test
    @DisplayName("Tests the action with an invalid configuration.")
    void testUnresolvedInput() {
        FilterByRegExpAction action = new FilterByRegExpAction();
        List<Map<String, Object>> processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());

        action.setInput("$." + DataKey.DATA.getKey() + "." + PARAM_KEY);
        processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());

        action.setInput(null);
        action.setExpression("$." + DataKey.DATA.getKey() + "." + PARAM_KEY);

        processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());
    }

    /**
     * Tests filtering the input data.
     */
    @Test
    @DisplayName("Tests filtering by regular expression.")
    void testProcess() {
        FilterByRegExpAction action = new FilterByRegExpAction();
        Map<String, Object> data = createData();

        // matches the expression:
        action.setInput("$." + DataKey.DATA.getKey() + "." + PARAM_KEY);
        action.setExpression("igor.*test");

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));

        // doesn't match expression:
        action.setExpression("test.*igor");

        processedData = action.process(data, new JobExecution());

        assertTrue(processedData.isEmpty());
    }

}