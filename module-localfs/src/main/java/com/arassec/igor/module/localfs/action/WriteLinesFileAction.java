package com.arassec.igor.module.localfs.action;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.localfs.service.LocalFilesystemFileService;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Writes all provided data into a file. Each {@link IgorData} is processed as one line of the target file.
 */
@IgorAction
public class WriteLinesFileAction extends BaseAction {

    /**
     * The data key that points to the newly written file's name.
     */
    private static final String KEY_WRITTEN_FILENAME = "writtenFilename";

    /**
     * The name of the file to write.
     */
    @IgorParam
    private String file;

    /**
     * The service to write the file to.
     */
    @IgorParam
    private LocalFilesystemFileService targetService;

    /**
     * Writer for the data.
     */
    private BufferedWriter bufferedWriter;

    /**
     * Creates a new WriteLinesFileAction.
     */
    public WriteLinesFileAction() {
        // Writing into a file is always single threaded!
        numThreads = 1;
        addProvidedDataKeys(KEY_WRITTEN_FILENAME);
    }

    /**
     * Creates the output file.
     */
    @Override
    public void initialize() {
        super.initialize();
        bufferedWriter = targetService.openFileForWriting(file);
    }

    /**
     * Writes the provided content into the configured file and returns the filename.
     *
     * @param data The data to processData.
     * @return The filename.
     */
    @Override
    public boolean process(IgorData data) {
        if (isValid(data)) {
            try {
                bufferedWriter.write((String) data.get(dataKey));
                bufferedWriter.newLine();
            } catch (IOException e) {
                throw new IllegalStateException("Could not write output file: " + file, e);
            }
            data.put(KEY_WRITTEN_FILENAME, file);
        }
        return true;
    }

    /**
     * Closes the output file.
     */
    @Override
    public void complete() {
        super.complete();
        try {
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            throw new IllegalStateException("Could not close output file: " + file, e);
        }
    }

}
