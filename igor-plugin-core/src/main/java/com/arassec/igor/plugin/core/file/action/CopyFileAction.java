package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CoreDataKey;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Copy file' Action</h2>
 *
 * <h3>Description</h3>
 * This action copies a file.<br>
 *
 * Details about the action's parameters are added to processed data items under the 'copiedFile' key.<br>
 *
 * A data item processed by this action could look like this:
 * <pre><code>
 * {
 *   "data": {
 *     "filename": "KeyGenerator.png",
 *     "lastModified": "2007-03-19T20:52:58+01:00",
 *     "directory": "/pub/example/"
 *   },
 *   "meta": {
 *     "jobId": "e0b925ed-104f-45b1-81b7-5d79ea46a633",
 *     "simulation": true,
 *     "timestamp": 1601302694149
 *   },
 *   "copiedFile": {
 *     "sourceDirectory": "/pub/example/",
 *     "targetFilename": "KeyGenerator.png",
 *     "targetDirectory": "/volume1/data/test/",
 *     "sourceFilename": "KeyGenerator.png"
 *   }
 * }
 * </code></pre>
 */
@Slf4j
@Setter
@Getter
@IgorComponent(typeId = "copy-file-action", categoryId = CoreCategory.FILE)
public class CopyFileAction extends BaseFileAction {

    /**
     * A file-connector providing access to the file to copy.
     */
    @NotNull
    @IgorParam
    private FileConnector source;

    /**
     * The directory containing the file to copy. Either a fixed value or a mustache expression selecting a property from the data
     * item. If a mustache expression is used, the property's value will be used as directory name.
     */
    @NotBlank
    @IgorParam
    private String sourceDirectory = DIRECTORY_TEMPLATE;

    /**
     * The name of the file to copy. Either a fixed value or a mustache expression selecting a property from the data item. If a
     * mustache expression is used, the property's value will be used as filename.
     */
    @NotBlank
    @IgorParam
    private String sourceFilename = FILENAME_TEMPLATE;

    /**
     * A file-connector providing access to the filesystem the file should be copied to.
     */
    @NotNull
    @IgorParam
    private FileConnector target;

    /**
     * The target directory for the copied file. Either a fixed value or a mustache expression selecting a property from the data
     * item. If a mustache expression is used, the property's value will be used as directory name.
     */
    @NotBlank
    @IgorParam
    private String targetDirectory;

    /**
     * The target name of the copied file. Either a fixed value or a mustache expression selecting a property from the data item.
     * If a mustache expression is used, the property's value will be used as filename.
     */
    @NotBlank
    @IgorParam
    private String targetFilename;

    /**
     * Enables an ".igor" file suffix during file transfer. The suffix will be removed after the file has been copied completely.
     */
    @IgorParam(advanced = true)
    private boolean appendTransferSuffix = true;

    /**
     * If checked, igor will try to determine the file type and append it to the target filename (e.g. '.html' or '.jpeg').
     */
    @IgorParam(advanced = true)
    private boolean appendFiletypeSuffix = false;

    /**
     * Creates a new component instance.
     */
    public CopyFileAction() {
        source = new FallbackFileConnector();
        target = new FallbackFileConnector();
    }

    /**
     * Copies the supplied source file to the destination connector. During transfer the file is optionally saved with the suffix
     * ".igor", which will be removed after successful transfer.
     *
     * @param data         The data to process.
     * @param jobExecution The job execution log.
     *
     * @return The manipulated data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        var resolvedData = resolveData(data, sourceFilename, sourceDirectory, targetFilename, targetDirectory);
        if (resolvedData == null) {
            return List.of(data);
        }

        var workInProgressMonitor = new WorkInProgressMonitor(resolvedData.getSourceFilename(), 0);
        jobExecution.addWorkInProgress(workInProgressMonitor);

        try {
            String sourceFileWithPath = CorePluginUtils.combineFilePath(resolvedData.getSourceDirectory(), resolvedData.getSourceFilename());

            var fileStreamData = source.readStream(sourceFileWithPath);

            if (fileStreamData == null || fileStreamData.getData() == null) {
                throw new IgorException("Not valid or not a file!");
            }

            String targetFileWithSuffix = CorePluginUtils.appendSuffixIfRequired(resolvedData.getTargetFilename(),
                fileStreamData.getFilenameSuffix(), appendFiletypeSuffix);

            String targetFileWithPath = CorePluginUtils.combineFilePath(resolvedData.getTargetDirectory(), targetFileWithSuffix);

            log.debug("Copying file '{}' to '{}'", sourceFileWithPath, targetFileWithPath);

            String targetFileInTransfer = targetFileWithPath;
            if (appendTransferSuffix) {
                targetFileInTransfer += CorePluginUtils.FILE_IN_TRANSFER_SUFFIX;
            }

            target.writeStream(targetFileInTransfer, fileStreamData, workInProgressMonitor, jobExecution);

            source.finalizeStream(fileStreamData);

            if (appendTransferSuffix) {
                target.move(targetFileInTransfer, targetFileWithPath);
            }

            log.debug("File '{}' copied to '{}'", sourceFileWithPath, targetFileWithPath);

            Map<String, Object> actionData = new HashMap<>();
            actionData.put(CoreDataKey.SOURCE_FILENAME.getKey(), resolvedData.getSourceFilename());
            actionData.put(CoreDataKey.SOURCE_DIRECTORY.getKey(), resolvedData.getSourceDirectory());
            actionData.put(CoreDataKey.TARGET_FILENAME.getKey(), targetFileWithSuffix);
            actionData.put(CoreDataKey.TARGET_DIRECTORY.getKey(), resolvedData.getTargetDirectory());
            data.put(CoreDataKey.COPIED_FILE.getKey(), actionData);
        } finally {
            jobExecution.removeWorkInProgress(workInProgressMonitor);
        }

        return List.of(data);
    }

}
