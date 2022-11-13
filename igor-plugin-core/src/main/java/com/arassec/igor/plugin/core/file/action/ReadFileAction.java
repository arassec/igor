package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Read file' Action</h2>
 *
 * <h3>Description</h3>
 * This action reads a file from a file-connector and adds its contents to the processed data item.<br>
 * <p>
 * The file's contents will be available in the data item under the key 'fileContents'.<br>
 * <p>
 * A data item processed by this action could look like this:
 * <pre><code>
 * {
 *   "data": {
 *     ...
 *   },
 *   "meta": {
 *     ...
 *   }
 *   "fileContents": "THIS IS THE CONTENT READ FROM THE FILE!"
 * }
 * </code></pre>
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "read-file-action", categoryId = CoreCategory.FILE)
public class ReadFileAction extends BaseFileAction {

    /**
     * The key in the data object where the content of the file should be placed.
     */
    public static final String KEY_FILE_CONTENTS = "fileContents";

    /**
     * A connector providing access to the file to read.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * The directory containing the file to delete. Either a fixed value or a mustache expression selecting a property from the
     * data item. If a mustache expression is used, the property's value will be used as directory name.
     */
    @NotBlank
    @IgorParam
    private String directory = DIRECTORY_TEMPLATE;

    /**
     * The name of the file to delete. Either a fixed value or a mustache expression selecting a property from the data item. If a
     * mustache expression is used, the property's value will be used as filename.
     */
    @NotBlank
    @IgorParam
    private String filename = FILENAME_TEMPLATE;

    /**
     * Creates a new component instance.
     */
    public ReadFileAction() {
        source = new FallbackFileConnector();
    }

    /**
     * Reads the content of a file and returns it as string.
     *
     * @param data         The data to processData.
     * @param jobExecution The job's execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String filePath = resolveFilePath(data, filename, directory);
        if (filePath == null) {
            return List.of(data);
        }

        log.debug("Reading file: '{}'", filePath);
        data.put(KEY_FILE_CONTENTS, source.read(filePath));
        log.debug("File '{}' read", filePath);

        return List.of(data);
    }

}
