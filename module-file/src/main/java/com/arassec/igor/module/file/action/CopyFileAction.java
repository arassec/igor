package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.module.file.service.FileService;
import com.arassec.igor.module.file.service.FileStreamData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


/**
 * Copies a file from one service to another.
 */
@Slf4j
@IgorComponent("Copy file")
public class CopyFileAction extends BaseFileAction {

    /**
     * Key to the source file's name.
     */
    private static final String KEY_SOURCE_FILENAME = "sourceFilename";

    /**
     * Key to the target file's name.
     */
    private static final String KEY_TARGET_FILENAME = "targetFilename";

    /**
     * Key to the target file's path.
     */
    private static final String KEY_TARGET_FILEPATH = "targetFilepath";

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
    @IgorParam
    private FileService sourceService;

    /**
     * The destination for the copied file.
     */
    @IgorParam
    private FileService targetService;

    /**
     * The target directory to copy/move the file to.
     */
    @IgorParam
    private String targetDirectory;

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
     * The key to the directory the source files are in.
     */
    @IgorParam
    private String directoryKey = "directory";

    /**
     * Copies the supplied source file to the destination service. During transfer the file is optionally saved with the suffix "
     * .igor", which will be removed after successful transfer.
     *
     * @param data         The data to process.
     * @param isDryRun     Set to {@code true}, if this is a dry-run and the file should not actually be copied. Set to {@code
     *                     false} for an actual run.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            String sourceFile = (String) data.get(dataKey);
            String sourceDirectory = (String) data.get(directoryKey);

            if (!targetDirectory.endsWith("/")) {
                targetDirectory += "/";
            }

            if (!isDryRun) {

                WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor(sourceFile, 0);
                jobExecution.addWorkInProgress(workInProgressMonitor);

                try {
                    String sourceFileWithPath = getSourceFileWithPath(sourceDirectory, sourceFile);
                    FileStreamData fileStreamData = sourceService.readStream(sourceFileWithPath, VOID_WORK_IN_PROGRESS_MONITOR);
                    String targetFile = getTargetFile(sourceFile, fileStreamData.getFilenameSuffix());
                    String targetFilePath = targetDirectory + targetFile;
                    log.debug("Copying file '{}' to '{}'", sourceFileWithPath, targetFilePath);
                    String targetFileInTransfer = targetFilePath;
                    if (appendTransferSuffix) {
                        targetFileInTransfer += IN_TRANSFER_SUFFIX;
                    }
                    targetService.writeStream(targetFileInTransfer, fileStreamData, workInProgressMonitor);
                    sourceService.finalizeStream(fileStreamData);
                    if (appendTransferSuffix) {
                        targetService.move(targetFileInTransfer, targetFilePath, VOID_WORK_IN_PROGRESS_MONITOR);
                    }
                    log.debug("File '{}' copied to '{}'", sourceFileWithPath, targetFilePath);
                    data.put(KEY_TARGET_FILENAME, targetFile);
                    data.put(KEY_TARGET_FILEPATH, targetFilePath);
                    data.put(KEY_TARGET_DIRECTORY, targetDirectory);
                } finally {
                    jobExecution.removeWorkInProgress(workInProgressMonitor);
                }

            }
            data.put(KEY_SOURCE_FILENAME, sourceFile);
            if (isDryRun) {
                String targetFile = getTargetFile(sourceFile, "dryRun");
                String targetFilePath = targetDirectory + targetFile;
                data.put(DRY_RUN_COMMENT_KEY,
                        getSourceFileWithPath(sourceDirectory, sourceFile) + " copied to " + targetFilePath);
                data.put(KEY_TARGET_FILENAME, targetFile);
                data.put(KEY_TARGET_FILEPATH, targetFilePath);
                data.put(KEY_TARGET_DIRECTORY, targetDirectory);
            }
            return List.of(data);
        }
        return null;
    }

    /**
     * Appends the name of the source file to the destination path, thus creating the target file.
     *
     * @param file   The source file.
     * @param suffix An optional file suffix to append to the target filename.
     *
     * @return The filename with path of the target file.
     */
    private String getTargetFile(String file, String suffix) {
        String targetFile = file;

        // Cleanup slashes in the filename. The HTTP-FileService introduced those as part of its implementation.
        if (targetFile.contains("/")) {
            String[] fileParts = targetFile.split("/");
            if (targetFile.length() == 1) {
                targetFile = "index";
            } else {
                targetFile = fileParts[fileParts.length - 1];
            }
        }

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
     * @param sourceDirectory The path to the source directory.
     * @param file            The filename.
     *
     * @return The path with the added filename.
     */
    private String getSourceFileWithPath(String sourceDirectory, String file) {
        if (sourceDirectory == null) {
            sourceDirectory = "";
        }
        if (!sourceDirectory.endsWith("/")) {
            sourceDirectory += "/";
        }
        return sourceDirectory + file;
    }

}
