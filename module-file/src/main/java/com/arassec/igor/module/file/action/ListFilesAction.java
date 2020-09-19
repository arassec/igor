package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.connector.FallbackFileConnector;
import com.arassec.igor.module.file.connector.FileConnector;
import com.arassec.igor.module.file.connector.FileInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action to list files in a directory of a {@link com.arassec.igor.module.file.connector.FileConnector}.
 */
@Getter
@Setter
@IgorComponent
public class ListFilesAction extends BaseFileAction {

    /**
     * The connector to use for file listing.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * Defines the directory to list files in.
     */
    @NotBlank
    @IgorParam
    private String directory;

    /**
     * An optional file ending to filter provided files by their extension.
     */
    @IgorParam(advanced = true)
    private String fileEnding;

    /**
     * Limits the data items during simulated job executions.
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "25")
    private int simulationLimit;

    /**
     * Creates a new component instance.
     */
    public ListFilesAction() {
        super("list-files-action");
        source = new FallbackFileConnector();
        setNumThreads(1);
        getUnEditableProperties().add("numThreads");
    }

    /**
     * Lists all files in a directory and returns them as data items.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job's execution log.
     *
     * @return One new data item for each listed file.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        List<FileInfo> fileInfos = source.listFiles(directory, fileEnding);

        if (isSimulation(data) && fileInfos.size() > simulationLimit) {
            fileInfos = fileInfos.subList(0, simulationLimit);
        }

        if (!directory.endsWith("/")) {
            directory += "/";
        }

        List<Map<String, Object>> result = new LinkedList<>();

        fileInfos.forEach(fileInfo -> {
            Map<String, Object> newDataItem = clone(data);

            Map<String, Object> item = new HashMap<>();
            String filename = fileInfo.getFilename();
            if (filename.startsWith(directory)) {
                filename = filename.replace(directory, "");
            }

            item.put(FILENAME_KEY, filename);
            item.put(DIRECTORY_KEY, directory);
            if (fileInfo.getLastModified() != null) {
                item.put(LAST_MODIFIED_KEY, fileInfo.getLastModified());
            }

            newDataItem.put(DataKey.DATA.getKey(), item);

            result.add(newDataItem);
        });

        return result;
    }
}
