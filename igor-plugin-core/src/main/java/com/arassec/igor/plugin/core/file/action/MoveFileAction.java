package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Move file' Action</h2>
 *
 * <h3>Description</h3>
 * This action moves/renames a file.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "move-file-action", categoryId = CoreCategory.FILE)
public class MoveFileAction extends BaseFileAction {

    /**
     * A file-connector providing access to the file.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * The directory containing the file to move. Either a fixed value or a mustache expression selecting a property from the data
     * item. If a mustache expression is used, the property's value will be used as directory name.
     */
    @NotBlank
    @IgorParam
    private String sourceDirectory = DIRECTORY_TEMPLATE;

    /**
     * The name of the file to move. Either a fixed value or a mustache expression selecting a property from the data item. If a
     * mustache expression is used, the property's value will be used as filename.
     */
    @NotBlank
    @IgorParam
    private String sourceFilename = FILENAME_TEMPLATE;

    /**
     * The target directory of the moved file. Either a fixed value or a mustache expression selecting a property from the data
     * item. If a mustache expression is used, the property's value will be used as directory name.
     */
    @NotBlank
    @IgorParam
    private String targetDirectory;

    /**
     * The target name of the moved file. Either a fixed value or a mustache expression selecting a property from the data item.
     * If a mustache expression is used, the property's value will be used as filename.
     */
    @NotBlank
    @IgorParam
    private String targetFilename;

    /**
     * Creates a new component instance.
     */
    public MoveFileAction() {
        source = new FallbackFileConnector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        var resolvedData = resolveData(data, sourceFilename, sourceDirectory, targetFilename, targetDirectory);
        if (resolvedData == null) {
            return List.of(data);
        }

        String sourceFilePath = resolvedData.getSourceDirectory() + resolvedData.getSourceFilename();
        String targetFilePath = resolvedData.getTargetDirectory() + resolvedData.getTargetFilename();

        log.debug("Moving file '{}' to '{}'", sourceFilePath, targetFilePath);
        source.move(sourceFilePath, targetFilePath);
        log.debug("File '{}' moved to '{}'", sourceFilePath, targetFilePath);

        return List.of(data);

    }

}
