package com.arassec.igor.core.model.action.file;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.file.FileService;
import com.arassec.igor.core.model.service.file.FileStreamData;


/**
 * Copies a file from one service to another.
 */
@IgorAction
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
     * The target directory to copy/move the file to.
     */
    @IgorParam
    private String targetDirectory;

    /**
     * The destination for the copied file.
     */
    @IgorParam
    private FileService targetService;

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
        if (isValid(data)) {
            String sourceFile = (String) data.get(dataKey);
            String targetFile = getTargetFile(sourceFile);
            String targetFileInTransfer = targetFile + IN_TRANSFER_SUFFIX;
            FileStreamData fileStreamData = sourceService.readStream(sourceFile);
            targetService.writeStream(targetFileInTransfer, fileStreamData);
            sourceService.finalizeStream(fileStreamData);
            targetService.move(targetFileInTransfer, targetFile);
            data.put(KEY_SOURCE_FILENAME, sourceFile);
            data.put(KEY_TARGET_FILENAME, targetFile);
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

}
