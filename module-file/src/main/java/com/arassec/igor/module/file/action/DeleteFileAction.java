package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.connector.FallbackFileConnector;
import com.arassec.igor.module.file.connector.FileConnector;
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
     * The connector providing the file to delete.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * The directory the file is in.
     */
    @NotBlank
    @IgorParam(defaultValue = DIRECTORY_QUERY)
    private String directory;

    /**
     * The name of the file.
     */
    @NotBlank
    @IgorParam(defaultValue = FILENAME_QUERY)
    private String filename;

    /**
     * Creates a new component instance.
     */
    public DeleteFileAction() {
        super("delete-file-action");
        source = new FallbackFileConnector();
    }

    /**
     * Deletes the configured file from the configured connector.
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
        source.delete(filePath, VOID_WIP_MONITOR);
        log.debug("File '{}' deleted", filePath);

        return List.of(data);
    }

}
