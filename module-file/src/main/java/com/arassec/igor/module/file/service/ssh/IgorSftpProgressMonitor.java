package com.arassec.igor.module.file.service.ssh;

import com.jcraft.jsch.SftpProgressMonitor;
import lombok.Getter;
import lombok.Setter;
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
    @Getter
    @Setter
    private long fileSize;

    /**
     * Creates a new instance.
     *
     * @param fileSize The total file size.
     */
    public IgorSftpProgressMonitor(long fileSize) {
        this.fileSize = fileSize;
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
