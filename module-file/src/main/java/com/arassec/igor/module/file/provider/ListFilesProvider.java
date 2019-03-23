package com.arassec.igor.module.file.provider;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.provider.BaseProvider;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.file.service.FileService;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides file names from a specified directory.
 */
@IgorProvider(label = "List Files")
public class ListFilesProvider extends BaseProvider implements FileProvider {

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
    public void initialize(Long jobId, String taskId) {
        super.initialize(jobId, taskId);
        files = sourceService.listFiles(directory);
        currentFile = 0;
        if (!directory.endsWith("/")) {
            directory += "/";
        }
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
        IgorData igorData = new IgorData(getJobId(), getTaskId());
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
