package com.arassec.igor.core.model.provider.file;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.provider.BaseProvider;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.core.model.service.file.FileService;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides file names from a specified directory.
 */
@IgorProvider(label = "List Files")
public class ListFilesProvider extends BaseProvider {

    /**
     * The service to use for file listing.
     */
    @IgorParam
    private FileService sourceService;

    /**
     * Defines the directory to list files in.
     */
    @IgorParam
    private String directory;

    /**
     * The key the files are listed under.
     */
    @IgorParam
    private String dataKey = "data";

    /**
     * The key that contains the configured directory.
     */
    @IgorParam
    private String directoryKey = "directory";

    /**
     * The files in the configured directory.
     */
    private List<String> files = new LinkedList<>();

    /**
     * The index into the listed files, pointing to the next file to serve.
     */
    private int currentFile = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskName) {
        super.initialize(jobId, taskName);
        files.clear();
        files.addAll(sourceService.listFiles(directory));
        currentFile = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        if (currentFile < files.size()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IgorData next() {
        IgorData igorData = new IgorData(getJobId(), getTaskName());
        igorData.put(directoryKey, directory);
        String file = files.get(currentFile);
        if (file.startsWith(directory)) {
            file = file.replace(directory, "");
        }
        igorData.put(dataKey, file);
        currentFile++;
        return igorData;
    }

}
