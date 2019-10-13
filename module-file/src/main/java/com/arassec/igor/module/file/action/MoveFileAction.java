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
 * Moves a file.
 */
@Slf4j
@Component
@Scope("prototype")
@ConditionalOnBean(FileService.class)
public class MoveFileAction extends BaseFileAction {

    /**
     * Service where the file can be found.
     */
    @IgorParam
    private FileService service;

    /**
     * Source file to copy.
     */
    @IgorParam
    private String sourceFilename;

    /**
     * Source directory to copy the file from.
     */
    @IgorParam
    private String sourceDirectory;

    /**
     * The target file name.
     */
    @IgorParam
    private String targetFilename;

    /**
     * The target directory to copy/move the file to.
     */
    @IgorParam
    private String targetDirectory;

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

        String resolvedSourceFilename = getString(data, sourceFilename);
        String resolvedSourceDirectory = resolveDirectory(data, sourceDirectory);
        String resolvedTargetFilename = getString(data, targetFilename);
        String resolvedTargetDirectory = resolveDirectory(data, targetDirectory);

        if (resolvedSourceFilename == null || resolvedTargetFilename == null) {
            if (isSimulation(data)) {
                data.put(SIMULATION_LOG_KEY,
                        "Couldn't resolve variables for moving: source ("
                                + resolvedSourceFilename + ") / target (" + resolvedTargetFilename + ")");
            }
            return List.of(data);
        }

        String sourceFilePath = resolvedSourceDirectory + resolvedSourceFilename;
        String targetFilePath = resolvedTargetDirectory + resolvedSourceFilename;

        log.debug("Moving file '{}' to '{}'", sourceFilePath, targetFilePath);
        service.move(sourceFilePath, targetFilePath, VOID_WIP_MONITOR);
        log.debug("File '{}' moved to '{}'", sourceFilePath, targetFilePath);

        return List.of(data);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "a3b7f9e9-9eea-4944-a867-62cdab3ddc07";
    }

}
