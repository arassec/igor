package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileStreamData;
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
 * File-Service for SFTP file handling.
 */
@Slf4j
@IgorComponent("SFTP")
public class SftpFileService extends BaseSshFileService {

    /**
     * The host of the remote server.
     */
    @IgorParam
    private String host;

    /**
     * The port of the remote server.
     */
    @IgorParam
    private int port = 22;

    /**
     * The username to login with.
     */
    @IgorParam
    private String username;

    /**
     * The password used for authentication.
     */
    @IgorParam(secured = true)
    private String password;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        try {
            final String dir = directory.endsWith("/") ? directory : directory + "/";
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            List<ChannelSftp.LsEntry> files = new LinkedList<>();
            channel.ls(directory, entry -> {
                if (fileEnding == null || StringUtils.isEmpty(fileEnding)) {
                    files.add(entry);
                } else if (entry.getFilename().endsWith(fileEnding)) {
                    files.add(entry);
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            });
            channel.disconnect();
            session.disconnect();
            return files.stream().map(lsEntry -> new FileInfo(dir + lsEntry.getFilename(),
                    formatInstant(Instant.ofEpochMilli(lsEntry.getAttrs().getMTime() * 1000L)))).collect(Collectors.toList());
        } catch (JSchException | SftpException e) {
            throw new ServiceException("Could not list files via SFTP!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file, WorkInProgressMonitor workInProgressMonitor) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.get(file, outputStream);
            channel.disconnect();
            session.disconnect();
            return outputStream.toString();
        } catch (IOException | JSchException | SftpException e) {
            throw new ServiceException("Could not read file via SFTP!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file, WorkInProgressMonitor workInProgressMonitor) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            Vector<ChannelSftp.LsEntry> lsEntries = channel.ls(file);
            if (lsEntries != null && !lsEntries.isEmpty()) {
                FileStreamData result = new FileStreamData();
                result.setFileSize(lsEntries.firstElement().getAttrs().getSize());
                result.setData(channel.get(file));

                SshConnectionData sshConnectionData = new SshConnectionData();
                sshConnectionData.setSession(session);
                sshConnectionData.setChannel(channel);
                result.setSourceConnectionData(sshConnectionData);

                // No need to disconnect. finalizeStream() will handle that...

                return result;
            } else {
                throw new ServiceException("File " + file + " found at the server!");
            }
        } catch (SftpException | JSchException e) {
            throw new ServiceException("Could not read file (sftp/stream)!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.put(fileStreamData.getData(), file,
                    new IgorSftpProgressMonitor(fileStreamData.getFileSize(), workInProgressMonitor), ChannelSftp.OVERWRITE);
            channel.disconnect();
            session.disconnect();
        } catch (SftpException | JSchException e) {
            throw new ServiceException("Could not write file (sftp/stream)!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        if (fileStreamData.getSourceConnectionData() != null && fileStreamData
                .getSourceConnectionData() instanceof SshConnectionData) {
            SshConnectionData sshConnectionData = (SshConnectionData) fileStreamData.getSourceConnectionData();
            sshConnectionData.getChannel().disconnect();
            sshConnectionData.getSession().disconnect();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file, WorkInProgressMonitor workInProgressMonitor) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.rm(file);
            channel.disconnect();
            session.disconnect();
        } catch (SftpException | JSchException e) {
            throw new ServiceException("Could not delete file: " + file);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target, WorkInProgressMonitor workInProgressMonitor) {
        try {
            moveInternal(source, target);
        } catch (SftpException | JSchException e) {
            try {
                // Workaround: sometimes there's an "SftpException: File not found" thrown and a retry a few seconds later works
                // without problems...
                Thread.sleep(30000);
                moveInternal(source, target);
            } catch (SftpException | JSchException | InterruptedException e1) {
                throw new ServiceException("Could not move file: " + source + " to " + target, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() throws ServiceException {
        Session session = connect(host, port, username, password);
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
        Session session = connect(host, port, username, password);
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        channel.rename(source, target);
        channel.disconnect();
        session.disconnect();
    }

}
