package com.arassec.igor.module.ssh.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.file.FileStreamData;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Service for SFTP file handling.
 */
@IgorService(label = "SFTP")
public class SftpFileService extends BaseSshFileService {

    /**
     * The host of the remote server.
     */
    @IgorParam
    protected String host;

    /**
     * The port of the remote server.
     */
    @IgorParam
    protected int port = 22;

    /**
     * The username to login with.
     */
    @IgorParam
    protected String username;

    /**
     * The password used for authentication.
     */
    @IgorParam(secured = true)
    protected String password;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listFiles(String directory) {
        try {
            final String dir = directory.endsWith("/") ? directory : directory + "/";
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            Vector<ChannelSftp.LsEntry> lsEntries = channel.ls(directory);
            channel.disconnect();
            session.disconnect();
            return lsEntries.stream().map(ChannelSftp.LsEntry::getFilename).map(s -> dir + s).collect(Collectors.toList());
        } catch (JSchException | SftpException e) {
            throw new ServiceException("Could not list files via SFTP!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
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
    public void write(String file, String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes())) {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.put(inputStream, file);
            channel.disconnect();
            session.disconnect();
        } catch (IOException | SftpException | JSchException e) {
            throw new ServiceException("Could not write file via SFTP!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            Vector<ChannelSftp.LsEntry> lsEntries = channel.ls(file);
            if (lsEntries != null && !lsEntries.isEmpty()) {
                FileStreamData result = new FileStreamData();
                result.setFileSize(lsEntries.firstElement().getAttrs().getSize());
                result.setData(channel.get(file));

                SshConnectionData sshConnectionData = new SshConnectionData();
                sshConnectionData.setSession(session);
                sshConnectionData.setChannel(channel);
                result.setSourceConnectionData(sshConnectionData);

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
    public void writeStream(String file, FileStreamData fileStreamData) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.put(fileStreamData.getData(), file);
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
        if (fileStreamData.getSourceConnectionData() != null
                && fileStreamData.getSourceConnectionData() instanceof SshConnectionData) {
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
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
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
    public void move(String source, String target) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.rename(source, target);
            channel.disconnect();
            session.disconnect();
        } catch (SftpException | JSchException e) {
            throw new ServiceException("Could not move file: " + source + " to " + target, e);
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
}
