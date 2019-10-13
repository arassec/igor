package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Deletes a file.
 */
@Slf4j
@Component
@Scope("prototype")
@ConditionalOnBean(FileService.class)
public class DeleteFileAction extends BaseFileAction {

    /**
     * The service providing the file to delete.
     */
    @IgorParam
    private FileService service;

    /**
     * The directory the file is in.
     */
    @IgorParam
    private String directory;

    /**
     * The name of the file.
     */
    @IgorParam
    private String filename;

    /**
     * Deletes the configured file from the configured service.
     *
     * @param data         The data to process.
     * @param jobExecution The job execution log.
     *
     * @return The data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String resolvedFilename = getString(data, filename);
        String resolvedDirectory = resolveDirectory(data, directory);

        if (resolvedFilename == null) {
            if (isSimulation(data)) {
                data.put(SIMULATION_LOG_KEY, "Couldn't resolve file to delete for configuration: " + filename);
            }
            return List.of(data);
        }

        String filePath = resolvedDirectory + resolvedFilename;

        log.debug("Deleting file: '{}'", filePath);
        service.delete(filePath, VOID_WIP_MONITOR);
        log.debug("File '{}' deleted", filePath);

        return List.of(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "f83ed584-a6a2-458f-86bd-aaaae347227b";
    }

}
