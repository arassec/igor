package com.arassec.igor.module.misc.action;

import com.arassec.igor.core.model.job.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for action tests.
 */
public abstract class BaseMiscActionTest {

    /**
     * Key for the data parameter provided to tests.
     */
    protected static final String PARAM_KEY = "parameter";

    /**
     * Value of the data parameter.
     */
    protected static final String PARAM_VALUE = "igor-message-test";

    /**
     * Creates an input data map for tests.
     *
     * @return Test data.
     */
    protected Map<String, Object> createData() {
        Map<String, Object> item = new HashMap<>();
        item.put(PARAM_KEY, PARAM_VALUE);

        Map<String, Object> result = new HashMap<>();
        result.put(Task.META_KEY, Task.createMetaData("1", "1"));
        result.put(Task.DATA_KEY, item);

        return result;
    }

}
