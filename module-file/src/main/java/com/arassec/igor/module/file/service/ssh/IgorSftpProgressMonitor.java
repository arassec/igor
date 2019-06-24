package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.jcraft.jsch.SftpProgressMonitor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SftpProgressMonitor} to monitor the progress of the file transfer to an SFTP-Server.
 */
@Slf4j
public class IgorSftpProgressMonitor implements SftpProgressMonitor {

    /**
     * Counts the number of transferred bytes.
     */
    private long count = 0;

    /**
     * The name of the target file.
     */
    private String target;

    /**
     * The total size of the transferred file.
     */
    private long fileSize;

    /**
     * Contains the progress.
     */
    private WorkInProgressMonitor workInProgressMonitor;

    /**
     * Creates a new instance.
     *
     * @param fileSize       The total file size.
     * @param workInProgressMonitor The work-in-progress monitor.
     */
    public IgorSftpProgressMonitor(long fileSize, WorkInProgressMonitor workInProgressMonitor) {
        this.fileSize = fileSize;
        this.workInProgressMonitor = workInProgressMonitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(int op, String src, String dest, long max) {
        target = dest;
        log.debug("Starting SFTP upload of: {}", target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean count(long bytes) {
        count += bytes;
        if (count >= fileSize) {
            return false;
        }
        workInProgressMonitor.setProgressInPercent((double) count * 100 / (double) fileSize);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end() {
        log.debug("Finished SFTP upload of: {}", target);
    }

}
