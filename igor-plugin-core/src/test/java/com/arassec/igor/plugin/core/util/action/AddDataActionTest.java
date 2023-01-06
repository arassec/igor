package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link AddDataAction}.
 */
@DisplayName("'Add data' action tests.")
class AddDataActionTest {

    /**
     * Tests adding data to a data item.
     */
    @Test
    @DisplayName("Tests adding data to a data item.")
    void testProcess() {
        AddDataAction action = new AddDataAction();
        action.setJson("{ \"a\": { \"b\": 12 } }");

        Map<String, Object> dataItem = Map.of("a", Map.of("c", "d"));

        List<Map<String, Object>> result = action.process(dataItem, new JobExecution());

        assertEquals(1, result.size());
        assertEquals("{a={c=d, b=12}}", result.get(0).toString());
    }

}
