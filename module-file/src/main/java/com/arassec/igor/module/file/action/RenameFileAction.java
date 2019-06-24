package com.arassec.igor.module.file.action;


import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Renames a file.
 */
@Slf4j
@IgorAction(label = "Rename file")
public class RenameFileAction extends BaseFileAction {

    /**
     * IgorParam for the service to rename the file.
     */
    @IgorParam
    private FileService sourceService;

    /**
     * The target name of the file.
     */
    @IgorParam
    private String targetFilename;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            if (isDryRun) {
                data.put(DRY_RUN_COMMENT_KEY, "Renamed file " + data.get(dataKey) + " to " + targetFilename);
            } else {
                String file = getString(data, dataKey);
                log.debug("Renaming file '{}' to '{}'", file, targetFilename);
                sourceService.move(file, targetFilename, VOID_WORK_IN_PROGRESS_MONITOR);
                log.debug("File '{}' renamed", file, targetFilename);
            }
            return List.of(data);
        }
        return null;
    }

}
