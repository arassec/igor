package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FallbackFileService;
import com.arassec.igor.module.file.service.FileService;
import lombok.*;
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
     * Service where the file can be found.
     */
    @NotNull
    @IgorParam
    private FileService service;

    /**
     * Source file to copy.
     */
    @NotBlank
    @IgorParam
    private String sourceFilename;

    /**
     * Source directory to copy the file from.
     */
    @NotBlank
    @IgorParam
    private String sourceDirectory;

    /**
     * The target file name.
     */
    @NotBlank
    @IgorParam
    private String targetFilename;

    /**
     * The target directory to copy/move the file to.
     */
    @NotBlank
    @IgorParam
    private String targetDirectory;

    /**
     * Creates a new component instance.
     */
    public MoveFileAction() {
        super("a3b7f9e9-9eea-4944-a867-62cdab3ddc07");
        service = new FallbackFileService();
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
        service.move(sourceFilePath, targetFilePath, VOID_WIP_MONITOR);
        log.debug("File '{}' moved to '{}'", sourceFilePath, targetFilePath);

        return List.of(data);

    }

}
