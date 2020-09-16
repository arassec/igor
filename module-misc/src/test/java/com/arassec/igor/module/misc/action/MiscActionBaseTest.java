package com.arassec.igor.module.misc.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.Job;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for action tests.
 */
public abstract class MiscActionBaseTest {

    /**
     * Key for the data parameter provided to tests.
     */
    protected static final String PARAM_KEY = "parameter";

    /**
     * Value of the data parameter.
     */
    protected static final String PARAM_VALUE = "igor-message-test";

    /**
     * The job's ID.
     */
    protected static final String JOB_ID = "job-id";

    /**
     * Creates an input data map for tests.
     *
     * @return Test data.
     */
    protected Map<String, Object> createData() {
        Map<String, Object> item = new HashMap<>();
        item.put(PARAM_KEY, PARAM_VALUE);

        Map<String, Object> result = new HashMap<>();
        result.put(DataKey.META.getKey(), Job.createMetaData(JOB_ID, null));
        result.put(DataKey.DATA.getKey(), item);

        return result;
    }

}
