package com.arassec.igor.module.ssh.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.file.FileStreamData;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link com.arassec.igor.core.model.service.file.FileService} for SCP file handling.
 */
@IgorService(label = "SCP")
public class ScpFileService extends BaseSshFileService {

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
        final String dir = directory.endsWith("/") ? directory : directory + "/";
        StringBuffer result = execute("cd " + dir + " && ls");
        return Arrays.stream(result.toString().split("\n")).map(s -> dir + s).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
        FileStreamData fileStreamData = readStream(file);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            copyStream(fileStreamData.getData(), outputStream, fileStreamData.getFileSize());
            outputStream.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new ServiceException("Could not read file: " + file, e);
        } finally {
            finalizeStream(fileStreamData);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String file, String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes())) {
            FileStreamData fileStreamData = new FileStreamData();
            fileStreamData.setFileSize(data.getBytes().length);
            fileStreamData.setData(inputStream);
            writeStream(file, fileStreamData);
        } catch (IOException e) {
            throw new ServiceException("Could not write file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        try {
            FileStreamData result = new FileStreamData();

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + file;
            Session session = connect(host, port, username, password);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // get I/O streams for remote scp
            OutputStream sshOutputStream = channel.getOutputStream();
            InputStream sshInputStream = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            sshOutputStream.write(buf, 0, 1);
            sshOutputStream.flush();

            StringBuffer log = new StringBuffer();
            int c = checkAck(sshInputStream, log);
            if (c != 'C') {
                throw new ServiceException("Could not read remote SSH file " + file + ": " + log);
            }

            // read '0644 '
            sshInputStream.read(buf, 0, 5);

            long fileSize = 0L;
            while (true) {
                if (sshInputStream.read(buf, 0, 1) < 0) {
                    // error
                    throw new ServiceException("Could not read remote SSH file's size: " + file);
                }
                if (buf[0] == ' ') {
                    break;
                }
                fileSize = fileSize * 10L + (long) (buf[0] - '0');
            }

            // Consume the file name from the stream...
            for (int i = 0; ; i++) {
                sshInputStream.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    break;
                }
            }

            // send '\0'
            buf[0] = 0;
            sshOutputStream.write(buf, 0, 1);
            sshOutputStream.flush();

            result.setData(sshInputStream);
            result.setFileSize(fileSize);

            SshConnectionData sshConnectionData = new SshConnectionData();
            sshConnectionData.setSession(session);
            sshConnectionData.setChannel(channel);
            sshConnectionData.setSshOutputStream(sshOutputStream);
            sshConnectionData.setSshInputStream(sshInputStream);
            result.setSourceConnectionData(sshConnectionData);

            return result;

        } catch (IOException | JSchException e) {
            throw new ServiceException("Could not read file stream via SSH!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData) {
        try {
            String command = "scp -t " + file;
            Session session = connect(host, port, username, password);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            OutputStream sshOutputStream = channel.getOutputStream();
            InputStream sshInputStream = channel.getInputStream();

            channel.connect();

            StringBuffer log = new StringBuffer();
            int sshReturnCode = checkAck(sshInputStream, log);
            if (sshReturnCode != 0) {
                throw new ServiceException("Error during SCP file transfer (" + sshReturnCode + "): " + log);
            }

            command = "C0644 " + fileStreamData.getFileSize() + " ";
            if (file.lastIndexOf('/') > 0) {
                command += file.substring(file.lastIndexOf('/') + 1);
            } else {
                command += file;
            }
            command += "\n";
            sshOutputStream.write(command.getBytes());
            sshOutputStream.flush();

            log = new StringBuffer();
            sshReturnCode = checkAck(sshInputStream, log);
            if (sshReturnCode != 0) {
                throw new ServiceException("Error during SCP file transfer (" + sshReturnCode + "): " + log);
            }

            copyStream(fileStreamData.getData(), sshOutputStream, fileStreamData.getFileSize());

            finalize(session, channel, sshOutputStream, sshInputStream);
        } catch (IOException | JSchException e) {
            throw new ServiceException("Could not write file stream via SSH!", e);
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
            finalize(sshConnectionData.getSession(), sshConnectionData.getChannel(),
                    sshConnectionData.getSshOutputStream(), sshConnectionData.getSshInputStream());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file) {
        execute("rm -f " + file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target) {
        execute("mv " + source + " " + target);
    }

    /**
     * Finalizes the supplied streams and closes the SSH session and channel.
     *
     * @param session         The SSH session.
     * @param channel         The SSH channel.
     * @param sshOutputStream The SSH output stream.
     * @param sshInputStream  The SSH input stream.
     */
    private void finalize(Session session, Channel channel, OutputStream sshOutputStream, InputStream sshInputStream) {
        try {
            // send '\0'
            byte[] buf = {0};
            sshOutputStream.write(buf, 0, 1);
            sshOutputStream.flush();
            StringBuffer log = new StringBuffer();
            int sshReturnCode = checkAck(sshInputStream, log);
            if (sshReturnCode != 0) {
                throw new ServiceException("SSH command not successful (" + sshReturnCode + "): " + log);
            }
            sshOutputStream.close();
            sshInputStream.close();
            channel.disconnect();
            session.disconnect();
        } catch (IOException e) {
            throw new ServiceException("Could not complete SSH streams!", e);
        }
    }

    /**
     * Reads from the provided InputStream to check the return value of the last SSH command sent to the server.
     *
     * @param in  The InputStream to read.
     * @param log Will be filled with an error message from the server if the command was not successful.
     * @return The result of the last SSH command.
     * @throws IOException In case of errors.
     */
    private static int checkAck(InputStream in, StringBuffer log) throws IOException {
        int b = in.read();
        // b may be 0 for success, 1 for error, 2 for fatal error, -1 (e.g. for closed input stream)
        if (b == 0) {
            return b;
        } else if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            log.append(sb.toString());
        }

        return b;
    }

    /**
     * Executes the supplied shell command on the remote SSH server and returns the output as StringBuffer.
     *
     * @param command The shell command to execute.
     * @return The command's output.
     */
    private StringBuffer execute(String command) {
        StringBuffer result = new StringBuffer();

        InputStream in = null;
        try {
            Session session = connect(host, port, username, password);
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            in = channel.getInputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    result.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    if (!(channel.getExitStatus() == 0)) {
                        throw new ServiceException("Exit status != 0 received: " + channel.getExitStatus());
                    }
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new ServiceException("Service interrupted during file listing via SSH!", e);
                }
            }

            channel.disconnect();
            session.disconnect();

            return result;
        } catch (IOException | JSchException e) {
            throw new ServiceException("Could not execute command on SSH server!", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new ServiceException("Could not close input stream (SSH).", e);
                }
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

}
