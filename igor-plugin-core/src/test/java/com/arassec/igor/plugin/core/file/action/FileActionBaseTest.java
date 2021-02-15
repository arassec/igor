package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.plugin.core.util.trigger.ManualTrigger;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for action tests of the file module.
 */
abstract class FileActionBaseTest {

    /**
     * Creates an input data map for tests.
     *
     * @return Test data.
     */
    Map<String, Object> createData() {
        Map<String, Object> item = new HashMap<>();
        item.put(BaseFileAction.FILENAME_KEY, "filename.txt");
        item.put(BaseFileAction.DIRECTORY_KEY, "/directory/test");
        item.put(BaseFileAction.LAST_MODIFIED_KEY, "123");

        Trigger trigger = new ManualTrigger();
        trigger.initialize(JobExecution.builder().jobId("1").build());

        Map<String, Object> result = trigger.createDataItem();
        result.put(DataKey.DATA.getKey(), item);

        return result;
    }

}
