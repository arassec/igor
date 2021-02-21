package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreDataKey;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CoreUtils;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Copies a file from one connector to another.
 */
@Slf4j
@Setter
@Getter
@IgorComponent
public class CopyFileAction extends BaseFileAction {

    /**
     * The connector providing the file to copy.
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
     * The destination for the copied file.
     */
    @NotNull
    @IgorParam
    private FileConnector target;

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
     * Enables a ".igor" file suffix during file transfer. The suffix will be removed after the file has been copied completely.
     */
    @IgorParam(advanced = true)
    private boolean appendTransferSuffix = true;

    /**
     * If set to {@code true}, igor appends a filetype suffix if avaliable (e.g. '.html' or '.jpeg').
     */
    @IgorParam(advanced = true)
    private boolean appendFiletypeSuffix = false;

    /**
     * Creates a new component instance.
     */
    public CopyFileAction() {
        super(CorePluginType.COPY_FILE_ACTION.getId());
        source = new FallbackFileConnector();
        target = new FallbackFileConnector();
    }

    /**
     * Copies the supplied source file to the destination connector. During transfer the file is optionally saved with the suffix
     * ".igor", which will be removed after successful transfer.
     *
     * @param data         The data to process.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        ResolvedData resolvedData = resolveData(data, sourceFilename, sourceDirectory, targetFilename, targetDirectory);
        if (resolvedData == null) {
            return List.of(data);
        }

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor(resolvedData.getSourceFilename(), 0);
        jobExecution.addWorkInProgress(workInProgressMonitor);

        try {
            String sourceFileWithPath = CoreUtils.combineFilePath(resolvedData.getSourceDirectory(), resolvedData.getSourceFilename());

            FileStreamData fileStreamData = source.readStream(sourceFileWithPath);

            if (fileStreamData == null || fileStreamData.getData() == null) {
                throw new IgorException("Not valid or not a file!");
            }

            String targetFileWithSuffix = CoreUtils.appendSuffixIfRequired(resolvedData.getTargetFilename(),
                    fileStreamData.getFilenameSuffix(), appendFiletypeSuffix);

            String targetFileWithPath = CoreUtils.combineFilePath(resolvedData.getTargetDirectory(), targetFileWithSuffix);

            log.debug("Copying file '{}' to '{}'", sourceFileWithPath, targetFileWithPath);

            String targetFileInTransfer = targetFileWithPath;
            if (appendTransferSuffix) {
                targetFileInTransfer += CoreUtils.FILE_IN_TRANSFER_SUFFIX;
            }

            target.writeStream(targetFileInTransfer, fileStreamData, workInProgressMonitor, jobExecution);

            source.finalizeStream(fileStreamData);

            if (appendTransferSuffix) {
                target.move(targetFileInTransfer, targetFileWithPath);
            }

            log.debug("File '{}' copied to '{}'", sourceFileWithPath, targetFileWithPath);

            Map<String, Object> actionData = new HashMap<>();
            actionData.put(CoreDataKey.SOURCE_FILENAME.getKey(), resolvedData.getSourceFilename());
            actionData.put(CoreDataKey.SOURCE_DIRECTORY.getKey(), resolvedData.getSourceDirectory());
            actionData.put(CoreDataKey.TARGET_FILENAME.getKey(), targetFileWithSuffix);
            actionData.put(CoreDataKey.TARGET_DIRECTORY.getKey(), resolvedData.getTargetDirectory());
            data.put(CoreDataKey.COPIED_FILE.getKey(), actionData);
        } finally {
            jobExecution.removeWorkInProgress(workInProgressMonitor);
        }

        return List.of(data);
    }

}
