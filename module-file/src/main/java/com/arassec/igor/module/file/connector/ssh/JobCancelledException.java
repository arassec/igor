package com.arassec.igor.module.file.connector.ssh;

/**
 * Indicates that the job has been cancelled. Used in the {@link SftpFileConnector} to abort transmission of a file.
 */
public class JobCancelledException extends RuntimeException {

    /**
     * {@inheritDoc}
     */
    public JobCancelledException(String message) {
        super(message);
    }

}
