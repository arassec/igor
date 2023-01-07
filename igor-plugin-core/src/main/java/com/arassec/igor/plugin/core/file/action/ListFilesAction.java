package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.CoreType;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <h2>'List files' Action</h2>
 *
 * <h3>Description</h3>
 * This action lists all files contained in a directory.
 * <p>
 * The action creates new data items for every file listed. A data item created by this action could look like this:
 * <pre><code>
 * {
 *   "data": {
 *     "filename": "README.TXT",
 *     "lastModified": "2020-04-18T00:00:00+02:00",
 *     "directory": "/"
 *   },
 *   "meta": {
 *     "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
 *     "timestamp": 1587203554775
 *   }
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.FILE, typeId = CoreType.LIST_FILES_ACTION)
public class ListFilesAction extends BaseFileAction {

    /**
     * A file-connector that provides the directory to list files in.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * Path to the directory that contains the files provided by the file connector.
     */
    @NotBlank
    @IgorParam
    private String directory;

    /**
     * An optional file ending which is used to filter files with the file connector, e.g. 'jpeg'.
     */
    @IgorParam(advanced = true)
    private String fileEnding;

    /**
     * Creates a new component instance.
     */
    public ListFilesAction() {
        source = new FallbackFileConnector();
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

        if (!directory.endsWith("/")) {
            directory += "/";
        }

        List<Map<String, Object>> result = new LinkedList<>();

        fileInfos.forEach(fileInfo -> {
            Map<String, Object> newDataItem = CorePluginUtils.clone(data);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enforceSingleThread() {
        return true;
    }

}
