package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.Service;

import java.io.InputStream;
import java.util.List;

/**
 * Base interface for all file based services.
 */
@IgorServiceCategory(label = "File")
public interface FileService extends Service {

    /**
     * Returns the names of all files in the specified directory, including the supplied directory in the name.
     *
     * @param directory The directory to search for files.
     * @return The file names as List.
     */
    List<FileInfo> listFiles(String directory, JobExecution jobExecution);

    /**
     * Reads the content of the specified file an returns it as string.
     *
     * @param file The file to read.
     * @return The content of the file.
     */
    String read(String file, JobExecution jobExecution);

    /**
     * Writes the supplied data into the specified file.
     *
     * @param file The file to write.
     * @param data The data to write.
     */
    void write(String file, String data, JobExecution jobExecution);

    /**
     * Reads the content of the specified file into the returned {@link InputStream}.
     *
     * @param file The name of the file to read.
     * @return The content of the file as stream of data.
     */
    FileStreamData readStream(String file, JobExecution jobExecution);

    /**
     * Writes the data from the supplied {@link FileStreamData} into the provided file.
     *
     * @param fileStreamData Information about the file like the name or the length.
     */
    void writeStream(String file, FileStreamData fileStreamData, JobExecution jobExecution);

    /**
     * Moves a file from source to target.
     *
     * @param source The source file.
     * @param target The target file.
     */
    void move(String source, String target, JobExecution jobExecution);

    /**
     * Deletes the specified file.
     *
     * @param file The file to delete.
     */
    void delete(String file, JobExecution jobExecution);

    /**
     * Must be called after working with streams.
     */
    void finalizeStream(FileStreamData fileStreamData);

}
