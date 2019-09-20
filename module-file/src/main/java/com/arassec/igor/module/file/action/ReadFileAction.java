package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Reads the content of a file.
 */
@Slf4j
@IgorComponent("Read file")
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
     * @param data         The data to processData.
     * @param jobExecution The job's execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        if (isValid(data)) {
            String file = getString(data, dataKey);
            log.debug("Reading file: '{}'", file);
            data.put(KEY_FILE_CONTENTS, sourceService.read(file, VOID_WIP_MONITOR));
            log.debug("File '{}' read", file);
            return List.of(data);
        }
        return null;
    }

}
