package com.arassec.igor.module.file.service.ssh;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is a wrapper for an JSch-MyPipedInputStream returned from a ChannelExec. This is required for file transfer
 * since the JSch-InputStream provides not only the file data in the stream, but also SSH commands. When used as
 * input into a JSch ChannelSftp's put() method, this method won't return, because the read() call never returns a value
 * smaller or equals to zero.
 * <p>
 * This wrapper counts the bytes of the file that have been transferred and signals the completion of the file transfer
 * eventually.
 */
public class SshInputStreamWrapper extends InputStream {

    /**
     * The original input stream provided by a JSch channel.
     */
    private InputStream inputStream;

    /**
     * The size of the file that is being transferred.
     */
    private long fileSize;

    /**
     * Indicates whether the whole file has been transferred ({@code true}) or not ({@code false}).
     */
    private boolean allRead;

    /**
     * Creates a new wrapper instance.
     *
     * @param inputStream The original input stream.
     * @param fileSize    The size of the file that is being transferred.
     */
    public SshInputStreamWrapper(InputStream inputStream, long fileSize) {
        this.inputStream = inputStream;
        this.fileSize = fileSize;
        this.allRead = false;
    }

    /**
     * This method is used to count the number of bytes transferred. If the file has been transferred completely, the
     * method will return -1, thus indicating that the transfer is complete.
     *
     * @param b   the target array to store the data in.
     * @param off the offset.
     * @param len the number of bytes to try to read.
     * @return The number of bytes actually read or -1, if no more bytes should be read.
     * @throws IOException In case of communication problems.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (allRead) {
            return -1;
        }
        int n;
        if ((fileSize - len) > 0) {
            n = inputStream.read(b, off, len);
            fileSize -= n;
        } else if ((fileSize - len) == 0) {
            n = inputStream.read(b, off, len);
            fileSize -= n;
            if (fileSize == 0) {
                allRead = true;
            }
        } else {
            n = inputStream.read(b, off, (int) fileSize);
            fileSize -= n;
            if (fileSize == 0) {
                allRead = true;
            }
        }
        return n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

}
