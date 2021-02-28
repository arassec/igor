package com.arassec.igor.plugin.message.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.plugin.core.util.trigger.ManualTrigger;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for message action tests.
 */
abstract class MessageActionBaseTest {

    /**
     * Key for the data parameter provided to tests.
     */
    static final String PARAM_KEY = "parameter";

    /**
     * Value of the data parameter.
     */
    static final String PARAM_VALUE = "igor-message-test";

    /**
     * Creates an input data map for tests.
     *
     * @return Test data.
     */
    Map<String, Object> createData() {
        Map<String, Object> item = new HashMap<>();
        item.put(PARAM_KEY, PARAM_VALUE);

        Trigger trigger = new ManualTrigger();
        trigger.initialize(JobExecution.builder().jobId("1").build());

        Map<String, Object> result = trigger.createDataItem();
        result.put(DataKey.DATA.getKey(), item);

        return result;
    }

}
