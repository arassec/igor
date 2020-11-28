package com.arassec.igor.plugin.common.file.connector;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for file based connectors.
 */
public interface FileConnector {

    /**
     * Returns the names of all files in the specified directory, including the supplied directory in the name.
     *
     * @param directory  The directory to search for files.
     * @param fileEnding An optional file ending to filter unwanted files.
     *
     * @return The file names as List.
     */
    @IgorSimulationSafe
    List<FileInfo> listFiles(String directory, String fileEnding);

    /**
     * Reads the content of the specified file an returns it as string.
     *
     * @param file The file to read.
     *
     * @return The content of the file.
     */
    @IgorSimulationSafe
    String read(String file);

    /**
     * Reads the content of the specified file into the returned {@link InputStream}.
     *
     * @param file The name of the file to read.
     *
     * @return The content of the file as stream of data.
     */
    @IgorSimulationSafe
    FileStreamData readStream(String file);

    /**
     * Writes the data from the supplied {@link FileStreamData} into the provided file.
     *
     * @param file           The file to write into.
     * @param fileStreamData Information about the file like the name or the length.
     * @param workInProgress The work in progress container.
     * @param jobExecution   The container for job execution data.
     */
    void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgress, JobExecution jobExecution);

    /**
     * Moves a file from source to target.
     *
     * @param source         The source file.
     * @param target         The target file.
     */
    void move(String source, String target);

    /**
     * Deletes the specified file.
     *
     * @param file           The file to delete.
     */
    void delete(String file);

    /**
     * {@inheritDoc}
     */
    void finalizeStream(FileStreamData fileStreamData);

}
