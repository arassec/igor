package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.module.file.provider.ListFilesProvider;

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
        item.put(ListFilesProvider.FILENAME_KEY, "filename.txt");
        item.put(ListFilesProvider.DIRECTORY_KEY, "/directory/test");
        item.put(ListFilesProvider.LAST_MODIFIED_KEY, "123");

        Map<String, Object> result = new HashMap<>();
        result.put(DataKey.META.getKey(), Job.createMetaData("1"));
        result.put(DataKey.DATA.getKey(), item);

        return result;
    }

}
