package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.module.file.service.FileService;

import java.util.List;
import java.util.Map;

/**
 * Reads the content of a file.
 */
@IgorAction(label = "Read file")
public class ReadFileAction extends BaseFileAction {

    /**
     * The key in the data object where the content of the file should be placed.
     */
    private static final String KEY_FILE_CONTENTS = "fileContents";

    /**
     * IgorParam for the service to use for file listing.
     */
    @IgorParam
    private FileService sourceService;

    /**
     * Reads the content of a file and returns it as string.
     *
     * @param data The data to processData.
     * @return The file content as string.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun) {
        if (isValid(data)) {
            data.put(KEY_FILE_CONTENTS, sourceService.read((String) data.get(dataKey)));
            return List.of(data);
        }
        return null;
    }

}
