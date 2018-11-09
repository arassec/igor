package com.arassec.igor.core.model.action.file;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.file.FileService;
import com.arassec.igor.core.model.service.file.FileStreamData;
import lombok.extern.slf4j.Slf4j;


/**
 * Copies a file from one service to another.
 */
@Slf4j
@IgorAction(label = "Copy file")
public class CopyFileAction extends BaseAction {

    /**
     * Key to the source file's name.
     */
    private static final String KEY_SOURCE_FILENAME = "sourceFilename";

    /**
     * Key to the target file's name.
     */
    private static final String KEY_TARGET_FILENAME = "targetFilename";

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
     * The key to the directory the source files are in.
     */
    @IgorParam
    private String directoryKey = "directory";

    /**
     * Enables a ".igor" file suffix during file transfer. The suffix will be removed after the file has been copied completely.
     */
    @IgorParam
    private boolean appendTransferSuffix = true;

    /**
     * Creates a new CopyFileAction.
     */
    public CopyFileAction() {
        addProvidedDataKeys(KEY_SOURCE_FILENAME, KEY_TARGET_FILENAME);
        addRequiredDataKeys(dataKey);
    }

    /**
     * Copies the supplied source file to the destination service. During transfer the file is saved with the suffix
     * ".igor", which will be removed after successful transfer.
     *
     * @param data The data to process.
     */
    @Override
    public boolean process(IgorData data) {
        return processInternal(data, false);
    }

    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isValid(data)) {
            String sourceFile = (String) data.get(dataKey);
            String sourceDirectory = (String) data.get(directoryKey);
            String targetFile = getTargetFile(sourceFile);
            if (!isDryRun) {
                String targetFileInTransfer = targetFile;
                if (appendTransferSuffix) {
                    targetFileInTransfer += IN_TRANSFER_SUFFIX;
                }
                FileStreamData fileStreamData = sourceService.readStream(getSourceFileWithPath(sourceDirectory, sourceFile));
                targetService.writeStream(targetFileInTransfer, fileStreamData);
                sourceService.finalizeStream(fileStreamData);
                if (appendTransferSuffix) {
                    targetService.move(targetFileInTransfer, targetFile);
                }
                log.debug("{} copied to {}", getSourceFileWithPath(sourceDirectory, sourceFile), getTargetFile(sourceFile));
            }
            data.put(KEY_SOURCE_FILENAME, sourceFile);
            data.put(KEY_TARGET_FILENAME, targetFile);
            if (isDryRun) {
                data.put("dryRunComment", getSourceFileWithPath(sourceDirectory, sourceFile)
                        + " copied to " + getTargetFile(sourceFile));
            }
        }
        return true;
    }

    /**
     * Appends the name of the source file to the destination path, thus creating the target file.
     *
     * @param file The source file.
     * @return The filename with path of the target file.
     */
    private String getTargetFile(String file) {
        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }
        return targetDirectory + file;
    }

    private String getSourceFileWithPath(String sourceDirectory, String file) {
        if (!sourceDirectory.endsWith("/")) {
            sourceDirectory += "/";
        }
        return sourceDirectory + file;
    }

}
