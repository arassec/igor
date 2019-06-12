package com.arassec.igor.module.file.action;


import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;

import java.util.List;
import java.util.Map;

/**
 * Renames a file.
 */
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
                sourceService.move((String) data.get(dataKey), targetFilename, jobExecution);
            }
            return List.of(data);
        }
        return null;
    }

}
