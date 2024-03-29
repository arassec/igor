package com.arassec.igor.plugin.core.file.connector;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;

import java.util.List;

/**
 * This is used as default assignment for {@link jakarta.validation.constraints.NotNull} connector parameters.
 */
public class FallbackFileConnector extends BaseFileConnector {

    /**
     * The exception message to use.
     */
    private static final String ERROR_MESSAGE = "Configure a real file connector!";

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public String read(String file) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public FileStreamData readStream(String file) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgress, JobExecution jobExecution) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void move(String source, String target) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void delete(String file) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void testConfiguration() {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

}
