package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileStreamData;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File-Service for SCP file handling.
 */
@Slf4j
@IgorComponent("SCP")
public class ScpFileService extends BaseSshFileService {

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
     * Reads from the provided InputStream to check the return value of the last SSH command sent to the server.
     *
     * @param in  The InputStream to read.
     * @param log Will be filled with an error message from the server if the command was not successful.
     *
     * @return The result of the last SSH command.
     *
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
            StringBuilder sb = new StringBuilder();
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
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        final String dir = directory.endsWith("/") ? directory : directory + "/";
        int numResultsToSkip = 1; // Without filter the total number of files is the first line of the result
        String command = "cd " + dir + " && ls -Al --time-style=full-iso";
        if (!StringUtils.isEmpty(fileEnding)) {
            numResultsToSkip = 0;
            command += " *." + fileEnding;
        }
        try {
            StringBuffer result = execute(command);
            return Arrays.stream(result.toString().split("\n")).skip(numResultsToSkip)
                    .map(lsResult -> new FileInfo(extractFilename(dir, lsResult), extractLastModified(lsResult)))
                    .collect(Collectors.toList());
        } catch (ServiceException e) {
            if (!StringUtils.isEmpty(fileEnding) && e.getMessage().contains("No such file or directory")) {
                return new LinkedList<>();
            } else {
                throw e;
            }
        }
    }

    /**
     * Creates the filename from the given input. The 'ls' command e.g. returns symlinks in the form of 'tmp -> /home/tmp', which
     * is not desired for igor.
     *
     * @param dir   The directory.
     * @param input The file name from the 'ls' command.
     *
     * @return The sanitized file name.
     */
    private String extractFilename(String dir, String input) {
        String[] split = input.split("\\s");
        if (split.length >= 3) {
            if (split[split.length - 2].equals("->")) {
                return dir + split[split.length - 3];
            }
        }
        return dir + split[split.length - 1];
    }

    /**
     * Extracts the last modified timestamp from the 'ls' command's output. Example output looks like:
     * <p>
     * drwxr-xr-x  10 root root 3760 2019-06-04 13:14:23.965495985 +0200 text.txt
     * </p>
     * This method extracts the timestamp relevant fields from the string and returns the formatted result.
     *
     * @param input One output line from the 'ls' command.
     *
     * @return The extracted timestamp or {@code null}, if none could be extracted.
     */
    private String extractLastModified(String input) {
        String yearPart = null;
        String timePart = null;
        String timezonePart = null;

        String[] split = input.split("\\s");
        for (String part : split) {
            if (part.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) { // 2019-06-04
                yearPart = part;
            } else if (part.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{9}")) { // 13:14:23
                timePart = part;
            } else if (part.matches("\\+[0-9]{4}")) { // +0200
                timezonePart = part;
            }
        }

        if (yearPart != null && timePart != null && timezonePart != null) {
            return yearPart + "T" + timePart.substring(0, 8) + "+" + timezonePart.substring(1, 3) + ":" + timezonePart
                    .substring(3, 5);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file, WorkInProgressMonitor workInProgressMonitor) {
        FileStreamData fileStreamData = readStream(file, workInProgressMonitor);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            copyStream(fileStreamData.getData(), outputStream, fileStreamData.getFileSize(), workInProgressMonitor);
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
    public FileStreamData readStream(String file, WorkInProgressMonitor workInProgressMonitor) {
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

            SshInputStreamWrapper sshInputStreamWrapper = new SshInputStreamWrapper(sshInputStream, fileSize);

            result.setData(sshInputStreamWrapper);
            result.setFileSize(fileSize);

            SshConnectionData sshConnectionData = new SshConnectionData();
            sshConnectionData.setSession(session);
            sshConnectionData.setChannel(channel);
            sshConnectionData.setSshOutputStream(sshOutputStream);
            sshConnectionData.setSshInputStream(sshInputStreamWrapper);
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
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor) {
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

            copyStream(fileStreamData.getData(), sshOutputStream, fileStreamData.getFileSize(), workInProgressMonitor);

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
        if (fileStreamData.getSourceConnectionData() != null && fileStreamData
                .getSourceConnectionData() instanceof SshConnectionData) {
            SshConnectionData sshConnectionData = (SshConnectionData) fileStreamData.getSourceConnectionData();
            finalize(sshConnectionData.getSession(), sshConnectionData.getChannel(), sshConnectionData.getSshOutputStream(),
                    sshConnectionData.getSshInputStream());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file, WorkInProgressMonitor workInProgressMonitor) {
        execute("rm -f " + file);
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
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target, WorkInProgressMonitor workInProgressMonitor) {
        execute("mv " + source + " " + target);
    }

    /**
     * Executes the supplied shell command on the remote SSH server and returns the output as StringBuffer.
     *
     * @param command The shell command to execute.
     *
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
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            channel.setErrStream(errorStream);
            in = channel.getInputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    if (!(channel.getExitStatus() == 0)) {
                        throw new ServiceException(
                                "Exit status != 0 received: " + channel.getExitStatus() + "\n(" + errorStream.toString()
                                        .replace("\n", "") + ")");
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
