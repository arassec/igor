package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;

/**
 * Deletes a file.
 */
@Slf4j
@IgorAction(label = "Delete file")
public class DeleteFileAction extends BaseFileAction {

    /**
     * The service providing the file to delete.
     */
    @IgorParam
    private FileService service;

    /**
     * The key to the directory the source files are in.
     */
    @IgorParam
    private String directoryKey = "directory";

    /**
     * Creates a new CopyFileAction.
     */
    public DeleteFileAction() {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    /**
     * Deletes the supplied file on the target service.
     *
     * @param data     The data to process.
     * @param isDryRun Set to {@code true}, if this is a dry-run and the file should not actually be copied. Set to
     *                 {@code false} for an actual run.
     * @return {@code true} if the data should further be processed, {@code false} otherwise.
     */
    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isValid(data)) {
            String file = (String) data.get(dataKey);
            if (isDryRun) {
                data.put(DRY_RUN_COMMENT_KEY, "Delete file " + file);
            } else {
                service.delete((String) data.get(dataKey));
            }
        }
        return true;
    }

}
