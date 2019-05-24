package com.arassec.igor.module.file.action;


import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.file.service.FileService;

/**
 * Renames a file.
 */
@IgorAction(label = "Rename file")
public class RenameFileAction extends BaseFileAction {

    /**
     * IgorParam for the service to rename the file.
     */
    @IgorParam
    private FileService sourceService;

    /**
     * The target name of the file.
     */
    @IgorParam
    private String targetFilename;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            sourceService.move((String) data.get(dataKey), targetFilename);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dryRun(IgorData data) {
        if (isValid(data)) {
            data.put(DRY_RUN_COMMENT_KEY, "Renamed file " + data.get(dataKey) + " to " + targetFilename);
            return true;
        }
        return false;
    }
}
