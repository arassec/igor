package com.arassec.igor.module.file.connector;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;

import java.util.List;

/**
 * This is used as default assignment for {@link javax.validation.constraints.NotNull} connector parameters.
 */
public class FallbackFileConnector extends BaseFileConnector {

    /**
     * The exception message to use.
     */
    private static final String ERROR_MESSAGE = "Configure a real file connector!";

    /**
     * Creates a new instance.
     */
    public FallbackFileConnector() {
        super("fallback-file-connector");
    }

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
    public String read(String file, WorkInProgressMonitor workInProgress) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public FileStreamData readStream(String file, WorkInProgressMonitor workInProgress) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgress) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void move(String source, String target, WorkInProgressMonitor workInProgress) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void delete(String file, WorkInProgressMonitor workInProgress) {
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
