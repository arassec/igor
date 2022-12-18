package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * <h2>'Delete file' Action</h2>
 *
 * <h3>Description</h3>
 * This action deletes a file.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "delete-file-action", categoryId = CoreCategory.FILE)
public class DeleteFileAction extends BaseFileAction {

    /**
     * A file-connector to delete the file from.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * The directory containing the file to delete. Either a fixed value or a mustache expression selecting a property from the
     * data item. If a mustache expression is used, the property's value will be used as directory name.
     */
    @NotBlank
    @IgorParam
    private String directory = DIRECTORY_TEMPLATE;

    /**
     * The name of the file to delete. Either a fixed value or a mustache expression selecting a property from the data item. If a
     * mustache expression is used, the property's value will be used as filename.
     */
    @NotBlank
    @IgorParam
    private String filename = FILENAME_TEMPLATE;

    /**
     * Creates a new component instance.
     */
    public DeleteFileAction() {
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
        source.delete(filePath);
        log.debug("File '{}' deleted", filePath);

        return List.of(data);
    }

}
