package com.arassec.igor.plugin.file.connector.ssh;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import com.arassec.igor.plugin.file.FilePluginType;
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
 * File-Connector for SCP file handling.
 */
@Slf4j
@IgorComponent
public class ScpFileConnector extends BaseSshFileConnector {

    /**
     * Creates a new component instance.
     */
    public ScpFileConnector() {
        super(FilePluginType.SCP_FILE_CONNECTOR.getId());
    }

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
    private static int checkAck(InputStream in, StringBuilder log) throws IOException {
        int b = in.read();
        // b may be 0 for success, 1 for error, 2 for fatal error, -1 (e.g. for closed input stream)
        if (b == 0 || b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            var sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            log.append(sb);
        }

        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        final String dir = directory.endsWith("/") ? directory : directory + "/";
        var numResultsToSkip = 1; // Without filter the total number of files is the first line of the result
        String command = "cd " + dir + " && ls -Alp --time-style=full-iso | grep -v /";
        if (StringUtils.hasText(fileEnding)) {
            numResultsToSkip = 0;
            command += " *." + fileEnding;
        }
        try {
            StringBuilder result = execute(command);
            return Arrays.stream(result.toString().split("\n")).skip(numResultsToSkip)
                    .map(lsResult -> new FileInfo(extractFilename(lsResult), extractLastModified(lsResult)))
                    .collect(Collectors.toList());
        } catch (IgorException e) {
            if (StringUtils.hasText(fileEnding) && e.getMessage().contains("No such file or directory")) {
                return new LinkedList<>();
            } else {
                throw e;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
        var fileStreamData = readStream(file);
        try (var outputStream = new ByteArrayOutputStream()) {
            copyStream(fileStreamData.getData(), outputStream, fileStreamData.getFileSize(), new WorkInProgressMonitor(),
                    JobExecution.builder().executionState(JobExecutionState.RUNNING).build());
            outputStream.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new IgorException("Could not read file: " + file, e);
        } finally {
            finalizeStream(fileStreamData);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        try {
            var result = new FileStreamData();

            // exec 'scp -f rfile' remotely
            String command = "scp -f " + file;
            var session = connect(getHost(), getPort(), getUsername(), getPassword());

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // get I/O streams for remote scp
            var sshOutputStream = channel.getOutputStream();
            var sshInputStream = channel.getInputStream();

            channel.connect();

            var buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            sshOutputStream.write(buf, 0, 1);
            sshOutputStream.flush();

            var log = new StringBuilder();
            int c = checkAck(sshInputStream, log);
            if (c != 'C') {
                throw new IgorException("Could not read remote SSH file " + file + ": " + log);
            }

            // read '0644 '
            //noinspection ResultOfMethodCallIgnored
            sshInputStream.read(buf, 0, 5);

            var fileSize = 0L;
            while (true) {
                if (sshInputStream.read(buf, 0, 1) < 0) {
                    // error
                    throw new IgorException("Could not read remote SSH file's size: " + file);
                }
                if (buf[0] == ' ') {
                    break;
                }
                fileSize = fileSize * 10L + (long) (buf[0] - '0');
            }

            // Consume the file name from the stream...
            for (var i = 0; ; i++) {
                //noinspection ResultOfMethodCallIgnored
                sshInputStream.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    break;
                }
            }

            // send '\0'
            buf[0] = 0;
            sshOutputStream.write(buf, 0, 1);
            sshOutputStream.flush();

            var sshInputStreamWrapper = new SshInputStreamWrapper(sshInputStream, fileSize);

            result.setData(sshInputStreamWrapper);
            result.setFileSize(fileSize);

            var sshConnectionData = new SshConnectionData();
            sshConnectionData.setSession(session);
            sshConnectionData.setChannel(channel);
            sshConnectionData.setSshOutputStream(sshOutputStream);
            sshConnectionData.setSshInputStream(sshInputStreamWrapper);
            result.setSourceConnectionData(sshConnectionData);

            return result;

        } catch (IOException | JSchException e) {
            throw new IgorException("Could not read file stream via SSH!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor,
                            JobExecution jobExecution) {
        try {
            String command = "scp -t " + file;
            var session = connect(getHost(), getPort(), getUsername(), getPassword());
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            var sshOutputStream = channel.getOutputStream();
            var sshInputStream = channel.getInputStream();

            channel.connect();

            var log = new StringBuilder();
            int sshReturnCode = checkAck(sshInputStream, log);
            if (sshReturnCode != 0) {
                throw new IgorException("Error during SCP file transfer (" + sshReturnCode + "): " + log);
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

            log = new StringBuilder();
            sshReturnCode = checkAck(sshInputStream, log);
            if (sshReturnCode != 0) {
                throw new IgorException("Error during SCP file transfer (" + sshReturnCode + "): " + log);
            }

            copyStream(fileStreamData.getData(), sshOutputStream, fileStreamData.getFileSize(), workInProgressMonitor, jobExecution);

            finalizeStreams(session, channel, sshOutputStream, sshInputStream);
        } catch (IOException | JSchException e) {
            throw new IgorException("Could not write file stream via SSH!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        if (fileStreamData.getSourceConnectionData() instanceof SshConnectionData) {
            var sshConnectionData = (SshConnectionData) fileStreamData.getSourceConnectionData();
            finalizeStreams(sshConnectionData.getSession(), sshConnectionData.getChannel(), sshConnectionData.getSshOutputStream(),
                    sshConnectionData.getSshInputStream());
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
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        var session = connect(getHost(), getPort(), getUsername(), getPassword());
        session.disconnect();
    }

    /**
     * Creates the filename from the given input. The 'ls' command e.g. returns symlinks in the form of 'tmp -> /home/tmp', which
     * is not desired for igor.
     *
     * @param input The file name from the 'ls' command.
     *
     * @return The sanitized file name.
     */
    private String extractFilename(String input) {
        String[] split = input.split("\\s");
        if (split.length >= 3 && split[split.length - 2].equals("->")) {
            return split[split.length - 3];
        }
        return split[split.length - 1];
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
    @SuppressWarnings("squid:S4784") // Using RegExps is OK...
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
     * Finalizes the supplied streams and closes the SSH session and channel.
     *
     * @param session         The SSH session.
     * @param channel         The SSH channel.
     * @param sshOutputStream The SSH output stream.
     * @param sshInputStream  The SSH input stream.
     */
    private void finalizeStreams(Session session, Channel channel, OutputStream sshOutputStream, InputStream sshInputStream) {
        try {
            // send '\0'
            byte[] buf = {0}; //NOSONAR - var is not applicable here...
            sshOutputStream.write(buf, 0, 1);
            sshOutputStream.flush();
            var log = new StringBuilder();
            int sshReturnCode = checkAck(sshInputStream, log);
            if (sshReturnCode != 0) {
                throw new IgorException("SSH command not successful (" + sshReturnCode + "): " + log);
            }
            sshOutputStream.close();
            sshInputStream.close();
            channel.disconnect();
            session.disconnect();
        } catch (IOException e) {
            throw new IgorException("Could not complete SSH streams!", e);
        }
    }

    /**
     * Executes the supplied shell command on the remote SSH server and returns the output as StringBuffer.
     *
     * @param command The shell command to execute.
     *
     * @return The command's output.
     */
    private StringBuilder execute(String command) {
        var result = new StringBuilder();

        var session = connect(getHost(), getPort(), getUsername(), getPassword());
        ChannelExec channel;
        try {
            channel = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e) {
            throw new IgorException("Could not open channel to SSH server!", e);
        }
        channel.setCommand(command);
        channel.setInputStream(null);
        var errorStream = new ByteArrayOutputStream();
        channel.setErrStream(errorStream);

        try (var in = channel.getInputStream()) {
            channel.connect();

            var tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.append(new String(tmp, 0, i));
                }
                if (shouldExecutionStop(channel, in, errorStream)) {
                    break;
                }
                idle();
            }

            channel.disconnect();
            session.disconnect();

            return result;
        } catch (IOException | JSchException e) {
            throw new IgorException("Could not execute command on SSH server!", e);
        }
    }

    /**
     * Checks whether the execution should be stopped or not.
     *
     * @param channel     The execution channel.
     * @param in          The SSH input stream from the server.
     * @param errorStream The SSH error stream.
     *
     * @return {@code true}, if the exeuction should be stopped, {@code false} otherwise.
     */
    private boolean shouldExecutionStop(ChannelExec channel, InputStream in, ByteArrayOutputStream errorStream) throws IOException {
        if (channel.isClosed()) {
            if (in.available() > 0) {
                return false;
            }
            if (channel.getExitStatus() != 0) {
                throw new IgorException(
                        "Exit status != 0 received: " + channel.getExitStatus() + "\n(" + errorStream.toString()
                                .replace("\n", "") + ")");
            }
            return true;
        }
        return false;
    }

    /**
     * Puts the thread to sleep for a fixed amount of time.
     */
    private void idle() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Connector interrupted during file listing via SSH!", e);
            Thread.currentThread().interrupt();
        }
    }

}
