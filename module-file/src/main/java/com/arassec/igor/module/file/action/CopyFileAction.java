package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.module.file.service.FileService;
import com.arassec.igor.module.file.service.FileStreamData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;


/**
 * Copies a file from one service to another.
 */
@Slf4j
@Component
@Scope("prototype")
@ConditionalOnBean(FileService.class)
public class CopyFileAction extends BaseFileAction {

    /**
     * Key to the source file's name.
     */
    private static final String KEY_SOURCE_FILENAME = "sourceFilename";

    /**
     * Key to the source file's directory.
     */
    private static final String KEY_SOURCE_DIRECTORY = "sourceDirectory";

    /**
     * Key to the target file's name.
     */
    private static final String KEY_TARGET_FILENAME = "targetFilename";

    /**
     * Key to the target directory.
     */
    private static final String KEY_TARGET_DIRECTORY = "targetDirectory";

    /**
     * File-suffix appended to files during transfer.
     */
    private static final String IN_TRANSFER_SUFFIX = ".igor";

    /**
     * The service providing the file to copy.
     */
    @NotNull
    @IgorParam
    private FileService sourceService;

    /**
     * Source directory to copy the file from.
     */
    @NotBlank
    @IgorParam
    private String sourceDirectory;

    /**
     * Source file to copy.
     */
    @NotBlank
    @IgorParam
    private String sourceFilename;

    /**
     * The destination for the copied file.
     */
    @NotBlank
    @IgorParam
    private FileService targetService;

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
    @IgorParam
    private boolean appendTransferSuffix = true;

    /**
     * If set to {@code true}, igor appends a filetype suffix if avaliable (e.g. '.html' or '.jpeg').
     */
    @IgorParam
    private boolean appendFiletypeSuffix = false;

    /**
     * Copies the supplied source file to the destination service. During transfer the file is optionally saved with the suffix
     * ".igor", which will be removed after successful transfer.
     *
     * @param data         The data to process.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
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
                        "Couldn't resolve variables for copying: source ("
                                + resolvedSourceFilename + ") / target (" + resolvedTargetFilename + ")");
            }
            return List.of(data);
        }

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor(resolvedSourceFilename, 0);
        jobExecution.addWorkInProgress(workInProgressMonitor);

        try {
            String sourceFileWithPath = getFilePath(resolvedSourceDirectory, resolvedSourceFilename);

            FileStreamData fileStreamData = sourceService.readStream(sourceFileWithPath, VOID_WIP_MONITOR);

            String targetFileWithSuffix = appendSuffixIfRequired(resolvedTargetFilename, fileStreamData.getFilenameSuffix());

            String targetFileWithPath = getFilePath(resolvedTargetDirectory, targetFileWithSuffix);

            log.debug("Copying file '{}' to '{}'", sourceFileWithPath, targetFileWithPath);

            String targetFileInTransfer = targetFileWithPath;
            if (appendTransferSuffix) {
                targetFileInTransfer += IN_TRANSFER_SUFFIX;
            }

            targetService.writeStream(targetFileInTransfer, fileStreamData, workInProgressMonitor);

            sourceService.finalizeStream(fileStreamData);

            if (appendTransferSuffix) {
                targetService.move(targetFileInTransfer, targetFileWithPath, VOID_WIP_MONITOR);
            }

            log.debug("File '{}' copied to '{}'", sourceFileWithPath, targetFileWithPath);

            data.put(KEY_SOURCE_FILENAME, resolvedSourceFilename);
            data.put(KEY_SOURCE_DIRECTORY, resolvedSourceDirectory);
            data.put(KEY_TARGET_FILENAME, targetFileWithSuffix);
            data.put(KEY_TARGET_DIRECTORY, resolvedTargetDirectory);

        } finally {
            jobExecution.removeWorkInProgress(workInProgressMonitor);
        }

        return List.of(data);
    }

    /**
     * Appends the name of the source file to the destination path, thus creating the target file.
     *
     * @param file   The file.
     * @param suffix An optional file suffix to append to the filename.
     *
     * @return The file with the appended suffix.
     */
    private String appendSuffixIfRequired(String file, String suffix) {
        String targetFile = file;
        if (!StringUtils.isEmpty(suffix) && !targetFile.contains(".") && appendFiletypeSuffix) {
            if (!suffix.startsWith("\\.")) {
                suffix = "." + suffix;
            }
            targetFile += suffix;
        }
        return targetFile;
    }

    /**
     * Combines the provided directory with the provided file. Adds a separator if needed.
     *
     * @param directory The path to the source directory.
     * @param file      The filename.
     *
     * @return The path with the added filename.
     */
    private String getFilePath(String directory, String file) {
        if (directory == null) {
            directory = "";
        }
        if (!directory.endsWith("/")) {
            directory += "/";
        }

        // Cleanup slashes in the filename. The HTTP-FileService introduced those as part of its implementation.
        if (file.contains("/")) {
            String[] fileParts = file.split("/");
            if (file.length() == 1) {
                file = "index";
            } else {
                file = fileParts[fileParts.length - 1];
            }
        }

        return directory + file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "d8564415-7dd9-4814-9b46-c2c5b56ed5cc";
    }

}
