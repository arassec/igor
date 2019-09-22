package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.BaseFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Renames a file.
 */
@Slf4j
@IgorComponent("Rename file")
public class RenameFileAction extends BaseFileAction {

    /**
     * IgorParam for the service to rename the file.
     */
    @IgorParam
    private BaseFileService sourceService;

    /**
     * The target name of the file.
     */
    @IgorParam
    private String targetFilename;

    /**
     * The source directory to use.
     */
    @IgorParam
    private String directoryKey = "directory";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        if (isValid(data)) {
            String directory = getString(data, directoryKey);
            if (StringUtils.isEmpty(directory)) {
                directory = "/";
            } else if (!directory.endsWith("/")) {
                directory += "/";
            }
            String targetFile = directory + getString(data, dataKey);
            log.debug("Renaming file '{}' to '{}'", targetFile, targetFilename);
            sourceService.move(targetFile, targetFilename, VOID_WIP_MONITOR);
            log.debug("File '{}' renamed", targetFile, targetFilename);
            return List.of(data);
        }
        return null;
    }

}
