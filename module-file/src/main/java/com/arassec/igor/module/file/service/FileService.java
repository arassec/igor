package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for file based services.
 */
public interface FileService {

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
     * @param file           The file to read.
     * @param workInProgress The work in progress container.
     *
     * @return The content of the file.
     */
    @IgorSimulationSafe
    String read(String file, WorkInProgressMonitor workInProgress);

    /**
     * Reads the content of the specified file into the returned {@link InputStream}.
     *
     * @param file           The name of the file to read.
     * @param workInProgress The work in progress container.
     *
     * @return The content of the file as stream of data.
     */
    @IgorSimulationSafe
    FileStreamData readStream(String file, WorkInProgressMonitor workInProgress);

    /**
     * Writes the data from the supplied {@link FileStreamData} into the provided file.
     *
     * @param file           The file to write into.
     * @param fileStreamData Information about the file like the name or the length.
     * @param workInProgress The work in progress container.
     */
    void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgress);

    /**
     * Moves a file from source to target.
     *
     * @param source         The source file.
     * @param target         The target file.
     * @param workInProgress The work in progress container.
     */
    void move(String source, String target, WorkInProgressMonitor workInProgress);

    /**
     * Deletes the specified file.
     *
     * @param file           The file to delete.
     * @param workInProgress The work in progress container.
     */
    void delete(String file, WorkInProgressMonitor workInProgress);

    /**
     * {@inheritDoc}
     */
    void finalizeStream(FileStreamData fileStreamData);

}
