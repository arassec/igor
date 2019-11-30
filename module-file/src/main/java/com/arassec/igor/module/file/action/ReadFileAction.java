package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.service.FileService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Reads the content of a file.
 */
@Slf4j
@Getter
@Setter
@ConditionalOnBean(FileService.class)
@IgorComponent
public class ReadFileAction extends BaseFileAction {

    /**
     * The key in the data object where the content of the file should be placed.
     */
    public static final String KEY_FILE_CONTENTS = "fileContents";

    /**
     * The service providing the file to read.
     */
    @NotNull
    @IgorParam
    private FileService service;

    /**
     * The directory the file is in.
     */
    @NotBlank
    @IgorParam
    private String directory;

    /**
     * The name of the file.
     */
    @NotBlank
    @IgorParam
    private String filename;

    /**
     * Creates a new component instance.
     */
    public ReadFileAction() {
        super("52256687-b1e4-438d-b2f1-e077d1c86193");
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
        data.put(KEY_FILE_CONTENTS, service.read(filePath, VOID_WIP_MONITOR));
        log.debug("File '{}' read", filePath);

        return List.of(data);
    }

}
