package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.IgorCategory;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorSimulationSafe;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Base for file services.
 */
@Slf4j
@IgorCategory("File")
public abstract class BaseFileService extends BaseService {

    /**
     * Configures a timezone for the file service as String.
     */
    @IgorParam(optional = true)
    protected String timezone;

    /**
     * Returns the names of all files in the specified directory, including the supplied directory in the name.
     *
     * @param directory  The directory to search for files.
     * @param fileEnding An optional file ending to filter unwanted files.
     *
     * @return The file names as List.
     */
    @IgorSimulationSafe
    public abstract List<FileInfo> listFiles(String directory, String fileEnding);

    /**
     * Reads the content of the specified file an returns it as string.
     *
     * @param file           The file to read.
     * @param workInProgress The work in progress container.
     *
     * @return The content of the file.
     */
    @IgorSimulationSafe
    public abstract String read(String file, WorkInProgressMonitor workInProgress);

    /**
     * Reads the content of the specified file into the returned {@link InputStream}.
     *
     * @param file           The name of the file to read.
     * @param workInProgress The work in progress container.
     *
     * @return The content of the file as stream of data.
     */
    @IgorSimulationSafe
    public abstract FileStreamData readStream(String file, WorkInProgressMonitor workInProgress);

    /**
     * Writes the data from the supplied {@link FileStreamData} into the provided file.
     *
     * @param file           The file to write into.
     * @param fileStreamData Information about the file like the name or the length.
     * @param workInProgress The work in progress container.
     */
    public abstract void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgress);

    /**
     * Moves a file from source to target.
     *
     * @param source         The source file.
     * @param target         The target file.
     * @param workInProgress The work in progress container.
     */
    public abstract void move(String source, String target, WorkInProgressMonitor workInProgress);

    /**
     * Deletes the specified file.
     *
     * @param file           The file to delete.
     * @param workInProgress The work in progress container.
     */
    public abstract void delete(String file, WorkInProgressMonitor workInProgress);

    /**
     * {@inheritDoc}
     */
    public void finalizeStream(FileStreamData fileStreamData) {
        // Nothing to do here by default...
    }

    /**
     * Copies the content of the input stream into the output stream.
     *
     * @param in             The input stream.
     * @param out            The output stream.
     * @param fileSize       The max number of bytes to copy.
     * @param workInProgressMonitor The {@link WorkInProgressMonitor} that keeps track of the copy progress.
     */
    protected void copyStream(InputStream in, OutputStream out, long fileSize, WorkInProgressMonitor workInProgressMonitor) {
        try {
            long totalSize = fileSize;
            byte[] buf = new byte[1024 * 1024];
            int foo;
            while (true) {
                if (buf.length < fileSize) {
                    foo = buf.length;
                } else {
                    foo = (int) fileSize;
                }
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    break; // error
                }
                out.write(buf, 0, foo);
                fileSize -= foo;
                if (fileSize == 0L) {
                    break;
                }
                out.flush();
                workInProgressMonitor
                        .setProgressInPercent(calculatePercentage(fileSize, totalSize));
            }
            out.flush();
        } catch (IOException e) {
            throw new ServiceException("Could not copy data via streams!", e);
        }
    }

    /**
     * Formats an {@link Instant} as a String.
     *
     * @param instant The timestamp to format.
     * @return The formatted time as String.
     */
    protected String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }

        ZonedDateTime zonedDateTime;
        if (timezone != null && !timezone.isEmpty()) {
            zonedDateTime = instant.atZone(ZoneId.of(timezone));
        } else {
            zonedDateTime = instant.atZone(ZoneId.systemDefault());
        }

        return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Calculates the percentage.
     *
     * @param obtained The optained part of the total.
     * @param total    The total amount.
     * @return The percentage.
     */
    private double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }

}
