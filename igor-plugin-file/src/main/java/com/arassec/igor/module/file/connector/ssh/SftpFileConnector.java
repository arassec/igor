package com.arassec.igor.module.file.connector.ssh;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.common.file.connector.FileInfo;
import com.arassec.igor.plugin.common.file.connector.FileStreamData;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * File-Connector for SFTP file handling.
 */
@Slf4j
@IgorComponent
public class SftpFileConnector extends BaseSshFileConnector {

    /**
     * Creates a new component instance.
     */
    public SftpFileConnector() {
        super("sftp-file-connector");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        try {
            Session session = connect(getHost(), getPort(), getUsername(), getPassword());
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            List<ChannelSftp.LsEntry> files = new LinkedList<>();
            channel.ls(directory, entry -> {
                if ((fileEnding == null || StringUtils.isEmpty(fileEnding) || entry.getFilename().endsWith(fileEnding))
                        && !entry.getAttrs().isDir()) {
                    files.add(entry);
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            });
            channel.disconnect();
            session.disconnect();
            return files.stream().map(lsEntry -> new FileInfo(lsEntry.getFilename(),
                    formatInstant(Instant.ofEpochMilli(lsEntry.getAttrs().getMTime() * 1000L)))).collect(Collectors.toList());
        } catch (JSchException | SftpException e) {
            throw new IgorException("Could not list files via SFTP!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Session session = connect(getHost(), getPort(), getUsername(), getPassword());
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.get(file, outputStream);
            channel.disconnect();
            session.disconnect();
            return outputStream.toString();
        } catch (IOException | JSchException | SftpException e) {
            throw new IgorException("Could not read file via SFTP!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        try {
            Session session = connect(getHost(), getPort(), getUsername(), getPassword());
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            // Unmodifiable JSCH code.
            // noinspection rawtypes
            @SuppressWarnings({"squid:S1149", "java:S3740"})
            Vector lsEntries = channel.ls(file);

            if (lsEntries != null && !lsEntries.isEmpty() && lsEntries.firstElement() instanceof ChannelSftp.LsEntry) {
                FileStreamData result = new FileStreamData();
                result.setFileSize(((ChannelSftp.LsEntry) lsEntries.firstElement()).getAttrs().getSize());
                result.setData(channel.get(file));

                SshConnectionData sshConnectionData = new SshConnectionData();
                sshConnectionData.setSession(session);
                sshConnectionData.setChannel(channel);
                result.setSourceConnectionData(sshConnectionData);

                // No need to disconnect. finalizeStream() will handle that...

                return result;
            } else {
                throw new IgorException("File " + file + " found at the server!");
            }
        } catch (SftpException | JSchException e) {
            throw new IgorException("Could not read file (sftp/stream)!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor,
                            JobExecution jobExecution) {
        try {
            Session session = connect(getHost(), getPort(), getUsername(), getPassword());
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.put(fileStreamData.getData(), file,
                    new IgorSftpProgressMonitor(fileStreamData.getFileSize(), workInProgressMonitor, jobExecution), ChannelSftp.OVERWRITE);
            channel.disconnect();
            session.disconnect();
        } catch (JobCancelledException e) {
            // No need to log error or warning, the job has simply been cancelled by the user.
            log.debug("SFTP PUT aborted due to job cancellation.", e);
        } catch (SftpException | JSchException e) {
            throw new IgorException("Could not write file (sftp/stream)!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        if (fileStreamData.getSourceConnectionData() instanceof SshConnectionData) {
            SshConnectionData sshConnectionData = (SshConnectionData) fileStreamData.getSourceConnectionData();
            sshConnectionData.getChannel().disconnect();
            sshConnectionData.getSession().disconnect();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file) {
        try {
            Session session = connect(getHost(), getPort(), getUsername(), getPassword());
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.rm(file);
            channel.disconnect();
            session.disconnect();
        } catch (SftpException | JSchException e) {
            throw new IgorException("Could not delete file: " + file);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target) {
        try {
            moveInternal(source, target);
        } catch (SftpException | JSchException e) {
            try {
                // Workaround: sometimes there's an "SftpException: File not found" thrown and a retry a few seconds later works
                // without problems...
                Thread.sleep(30000);
                moveInternal(source, target);
            } catch (InterruptedException i) {
                log.error("Interrupted while waiting for the SFTP server!", e);
                Thread.currentThread().interrupt();
            } catch (SftpException | JSchException e1) {
                throw new IgorException("Could not move file: " + source + " to " + target, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        Session session = connect(getHost(), getPort(), getUsername(), getPassword());
        session.disconnect();
    }

    /**
     * Moves the source file into the target file.
     *
     * @param source The source file to move.
     * @param target The target file name.
     *
     * @throws JSchException In case of SSH protocol errors.
     * @throws SftpException In case of SFTP errors.
     */
    private void moveInternal(String source, String target) throws JSchException, SftpException {
        Session session = connect(getHost(), getPort(), getUsername(), getPassword());
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        channel.rename(source, target);
        channel.disconnect();
        session.disconnect();
    }

}
