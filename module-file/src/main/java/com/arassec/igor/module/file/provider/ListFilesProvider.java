package com.arassec.igor.module.file.provider;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.BaseProvider;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        files = sourceService.listFiles(directory, jobExecution);
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
        item.put(Action.JOB_ID_KEY, getJobId());
        item.put(Action.TASK_ID_KEY, getTaskId());
        item.put(directoryKey, directory);
        FileInfo fileInfo = files.get(currentFile);
        String file = fileInfo.getFileName();
        if (file.startsWith(directory)) {
            file = file.replace(directory, "");
        }
        item.put(dataKey, file);
        if (fileInfo.getLastModified() != null) {
            item.put("lastModified", fileInfo.getLastModified());
        }
        currentFile++;
        return item;
    }

}
