package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FallbackFileService;
import com.arassec.igor.module.file.service.FileService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Deletes a file.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class DeleteFileAction extends BaseFileAction {

    /**
     * The service providing the file to delete.
     */
    @NotNull
    @IgorParam
    private FileService service;

    /**
     * The directory the file is in.
     */
    @NotBlank
    @IgorParam
    private String directory;

    /**
     * The name of the file.
     */
    @NotBlank
    @IgorParam
    private String filename;

    /**
     * Creates a new component instance.
     */
    public DeleteFileAction() {
        super("f83ed584-a6a2-458f-86bd-aaaae347227b");
        service = new FallbackFileService();
    }

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

        String filePath = resolveFilePath(data, filename, directory);
        if (filePath == null) {
            return List.of(data);
        }

        log.debug("Deleting file: '{}'", filePath);
        service.delete(filePath, VOID_WIP_MONITOR);
        log.debug("File '{}' deleted", filePath);

        return List.of(data);
    }

}
