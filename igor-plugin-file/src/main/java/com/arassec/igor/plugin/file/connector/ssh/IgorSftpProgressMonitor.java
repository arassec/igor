package com.arassec.igor.plugin.file.connector.ssh;

import com.arassec.igor.core.model.job.execution.JobExecution;
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
    private final long fileSize;

    /**
     * Contains the progress.
     */
    private final WorkInProgressMonitor workInProgressMonitor;

    /**
     * Container for job execution data.
     */
    private final JobExecution jobExecution;

    /**
     * Creates a new instance.
     *
     * @param fileSize              The total file size.
     * @param workInProgressMonitor The work-in-progress monitor.
     * @param jobExecution          The container for job execution information.
     */
    public IgorSftpProgressMonitor(long fileSize, WorkInProgressMonitor workInProgressMonitor, JobExecution jobExecution) {
        this.fileSize = fileSize;
        this.workInProgressMonitor = workInProgressMonitor;
        this.jobExecution = jobExecution;
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
        if (!jobExecution.isRunningOrActive()) {
            throw new JobCancelledException("The job has been cancelled: " + jobExecution.getJobId());
        }
        count += bytes;
        if (count >= fileSize) {
            workInProgressMonitor.setProgressInPercent(100);
            return false;
        }
        workInProgressMonitor.setProgressInPercent((double) count * 100 / fileSize);
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
