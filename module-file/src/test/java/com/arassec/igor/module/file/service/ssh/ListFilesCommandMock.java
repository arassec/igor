package com.arassec.igor.module.file.service.ssh;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Mock {@link Command} to test {@link ScpFileService#listFiles}.
 */
@Slf4j
public class ListFilesCommandMock implements Command {

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
    public ListFilesCommandMock(int testVariant) {
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
        if (testVariant == 1) {
            outputStream.write((
                    "total 2\n" +
                            "-rw-r--r--  1 root   root 129997 2020-02-22 17:43:13.410083727 +0100 alpha.txt\n" +
                            "-rw-r--r--  1 root   root   1634 2020-02-22 17:43:18.633492983 +0100 beta.test\n"
            ).getBytes());
        } else if (testVariant == 2) {
            outputStream.write((
                    "-rw-r--r--  1 root   root   1634 2020-02-22 17:43:18.633492983 +0100 beta.test\n"
            ).getBytes());
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
