package com.arassec.igor.module.file.provider;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides file names from a specified directory.
 */
@IgorComponent("List Files")
public class ListFilesProvider extends BaseFileProvider {

    /**
     * The key to the filename.
     */
    private static final String FILENAME_KEY = "filename";

    /**
     * The key to the directory.
     */
    private static final String DIRECTORY_KEY = "directory";

    /**
     * The key to the file's last modification timestamp.
     */
    private static final String LAST_MODIFIED_KEY = "lastModified";

    /**
     * The service to use for file listing.
     */
    @IgorParam
    private FileService service;

    /**
     * Defines the directory to list files in.
     */
    @IgorParam
    private String directory;

    /**
     * An optional file ending to filter provided files by their extension.
     */
    @IgorParam(optional = true)
    private String fileEnding;

    /**
     * The files in the configured directory.
     */
    private List<FileInfo> files = new LinkedList<>();

    /**
     * The index into the listed files, pointing to the next file to serve.
     */
    private int currentFile = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Long jobId, String taskId, JobExecution jobExecution) {
        super.initialize(jobId, taskId, jobExecution);
        files = service.listFiles(directory, fileEnding);
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
        return currentFile < files.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> next() {
        Map<String, Object> item = new HashMap<>();

        FileInfo fileInfo = files.get(currentFile);

        String filename = fileInfo.getFilename();
        if (filename.startsWith(directory)) {
            filename = filename.replace(directory, "");
        }

        item.put(FILENAME_KEY, filename);
        item.put(DIRECTORY_KEY, directory);
        if (fileInfo.getLastModified() != null) {
            item.put(LAST_MODIFIED_KEY, fileInfo.getLastModified());
        }

        currentFile++;

        return item;
    }

}
