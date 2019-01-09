package com.arassec.igor.core.model.action.file;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.file.FileService;

/**
 * Reads the content of a file.
 */
@IgorAction(label = "Read file")
public class ReadFileAction extends BaseFileAction {

    /**
     * The key in the data object where the content of the file should be placed.
     */
    private static final String KEY_FILE_CONTENTS = "fileContents";

    /**
     * IgorParam for the service to use for file listing.
     */
    @IgorParam
    private FileService sourceService;

    /**
     * Creates a new ReadFileAction.
     */
    public ReadFileAction() {
        addProvidedDataKeys(KEY_FILE_CONTENTS);
    }

    /**
     * Reads the content of a file and returns it as string.
     *
     * @param data The data to processData.
     * @return The file content as string.
     */
    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            data.put(KEY_FILE_CONTENTS, sourceService.read((String) data.get(dataKey)));
        }
        return true;
    }

}
