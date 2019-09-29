package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Reads the content of a file.
 */
@Slf4j
@IgorComponent("Read file")
public class ReadFileAction extends BaseFileAction {

    /**
     * The key in the data object where the content of the file should be placed.
     */
    private static final String KEY_FILE_CONTENTS = "fileContents";

    /**
     * The service providing the file to read.
     */
    @IgorParam
    private FileService service;

    /**
     * The directory the file is in.
     */
    @IgorParam
    private String directory;

    /**
     * The name of the file.
     */
    @IgorParam
    private String filename;

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

        String resolvedFilename = getString(data, filename);
        String resolvedDirectory = resolveDirectory(data, directory);

        if (resolvedFilename == null) {
            if (isSimulation(data)) {
                data.put(SIMULATION_LOG_KEY, "Couldn't resolve file to read for configuration: " + filename);
            }
            return List.of(data);
        }

        String filePath = resolvedDirectory + resolvedFilename;

        log.debug("Reading file: '{}'", filePath);
        data.put(KEY_FILE_CONTENTS, service.read(filePath, VOID_WIP_MONITOR));
        log.debug("File '{}' read", filePath);

        return List.of(data);

    }

}
