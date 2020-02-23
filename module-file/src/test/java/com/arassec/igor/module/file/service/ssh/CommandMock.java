package com.arassec.igor.module.file.service.ssh;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Mock {@link Command} to test {@link ScpFileService}.
 */
@Slf4j
public class CommandMock implements Command {

    /**
     * SSH input stream.
     */
    private InputStream inputStream;

    /**
     * SSH output stream.
     */
    private OutputStream outputStream;

    /**
     * SSH error stream.
     */
    private OutputStream errorStream;

    /**
     * An exit callback.
     */
    private ExitCallback exitCallback;

    /**
     * Used to return different results based on the configured variant.
     */
    private int testVariant;

    /**
     * Creates a new instance.
     *
     * @param testVariant Indicates the output to produces with this command.
     */
    public CommandMock(int testVariant) {
        this.testVariant = testVariant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setErrorStream(OutputStream errorStream) {
        this.errorStream = errorStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        this.exitCallback = exitCallback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(ChannelSession channelSession, Environment environment) throws IOException {
        log.debug("Start called with test variant: {}", testVariant);

        if (testVariant == 1) { // list files without filter
            outputStream.write((
                    "total 2\n" +
                            "-rw-r--r--  1 root   root 129997 2020-02-22 17:43:13.410083727 +0100 alpha.txt\n" +
                            "-rw-r--r--  1 root   root   1634 2020-02-22 17:43:18.633492983 +0100 beta.test\n"
            ).getBytes());
        } else if (testVariant == 2) { // list files with file-ending filter
            outputStream.write((
                    "-rw-r--r--  1 root   root   1634 2020-02-22 17:43:18.633492983 +0100 beta.test\n"
            ).getBytes());
        } else if (testVariant == 3) { // read file alpha.txt
            byte[] result = "C0644 28 alpha.txt".getBytes();
            result = ArrayUtils.add(result, (byte) 0x0a);
            result = ArrayUtils.addAll(result, "ALPHA-igor-ssh-service-tests".getBytes());
            result = ArrayUtils.add(result, (byte) 0x00);
            outputStream.write(result);
        } else if (testVariant == 4) { // read invalid file
            outputStream.write("-1".getBytes());
        } else if (testVariant == 5) { // write stream
            Files.writeString(Paths.get("target/ssh-write-stream-test.txt"), "igor-ssh-write-test", StandardOpenOption.CREATE_NEW);
            byte[] acks = {0x00, 0x00, 0x00};
            outputStream.write(acks);
        } else if (testVariant == 6) { // delete file
            Files.deleteIfExists(Paths.get("target/ssh-delete-test.txt"));
        } else if (testVariant == 7) { // move file
            Files.move(Paths.get("target/ssh-move-test.txt"), Paths.get("target/ssh-move-test.txt.moved"));
        }

        outputStream.flush();

        exitCallback.onExit(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(ChannelSession channelSession) {
        // nothing to do here...
    }

}
