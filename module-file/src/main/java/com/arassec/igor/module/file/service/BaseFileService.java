package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamException;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.io.Util;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base for file services.
 */
@Slf4j
public abstract class BaseFileService extends BaseService implements FileService {

    /**
     * Configures a timezone for the file service as String.
     */
    @IgorParam(optional = true)
    protected String timezone;

    /**
     * {@inheritDoc}
     */
    @Override
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
            Util.copyStream(in, out, 4096, fileSize, new CopyStreamListener() {
                @Override
                public void bytesTransferred(CopyStreamEvent copyStreamEvent) {
                    workInProgressMonitor
                            .setProgressInPercent(calculatePercentage(copyStreamEvent.getBytesTransferred(), fileSize));
                }

                @Override
                public void bytesTransferred(long l, int i, long l1) {
                    workInProgressMonitor.setProgressInPercent(calculatePercentage(l, fileSize));
                }
            }, true);
        } catch (CopyStreamException e) {
            throw new ServiceException("Could not copy data via streams!", e.getIOException());
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
    public double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }

}
