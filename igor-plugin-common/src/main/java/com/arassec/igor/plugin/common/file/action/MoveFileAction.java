package com.arassec.igor.plugin.common.file.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.common.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.common.file.connector.FileConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Moves a file.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class MoveFileAction extends BaseFileAction {

    /**
     * Connector where the file can be found.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * Source directory to copy the file from.
     */
    @NotBlank
    @IgorParam(defaultValue = DIRECTORY_TEMPLATE)
    private String sourceDirectory;

    /**
     * Source file to copy.
     */
    @NotBlank
    @IgorParam(defaultValue = FILENAME_TEMPLATE)
    private String sourceFilename;

    /**
     * The target directory to copy/move the file to.
     */
    @NotBlank
    @IgorParam
    private String targetDirectory;

    /**
     * The target file name.
     */
    @NotBlank
    @IgorParam
    private String targetFilename;

    /**
     * Creates a new component instance.
     */
    public MoveFileAction() {
        super("move-file-action");
        source = new FallbackFileConnector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        ResolvedData resolvedData = resolveData(data, sourceFilename, sourceDirectory, targetFilename, targetDirectory);
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
