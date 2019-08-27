package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Deletes a file.
 */
@Slf4j
@IgorComponent("Delete file")
public class DeleteFileAction extends BaseFileAction {

    /**
     * The service providing the file to delete.
     */
    @IgorParam
    private FileService service;

    /**
     * The key to the directory the source files are in.
     */
    @IgorParam
    private String directoryKey = "directory";

    /**
     * Copies the supplied source file to the destination service. During transfer the file is saved with the suffix ".igor",
     * which will be removed after successful transfer.
     *
     * @param data         The data to process.
     * @param isDryRun     Set to {@code true}, if this is a dry-run and the file should not actually be deleted. Set to {@code
     *                     false} for an actual run.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            String file = getString(data, dataKey);
            String directory = getString(data, directoryKey);
            if (!directory.endsWith("/")) {
                directory += "/";
            }
            String path = directory + file;
            if (isDryRun) {
                data.put(DRY_RUN_COMMENT_KEY, "Delete file: " + path);
            } else {
                log.debug("Deleting file: '{}'", path);
                service.delete(path, VOID_WORK_IN_PROGRESS_MONITOR);
                log.debug("File '{}' deleted", path);
            }
            return List.of(data);
        }
        return null;
    }

}
