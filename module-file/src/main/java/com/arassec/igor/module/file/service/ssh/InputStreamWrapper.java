package com.arassec.igor.module.file.service.ssh;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream {

    private InputStream inputStream;

    private long totalBytes;

    private long numBytesRead;

    private boolean readingFile = false;

    public InputStreamWrapper(InputStream inputStream, long totalBytes) {
        this.inputStream = inputStream;
        this.totalBytes = totalBytes;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (readingFile) {
            int n = inputStream.read(b, off, len);
            numBytesRead += n;
            if ((numBytesRead >= totalBytes)) {
                return 0;
            }
            return n;
        } else {
            return inputStream.read(b, off, len);
        }
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    public void setReadingFile(boolean readingFile) {
        this.readingFile = readingFile;
    }

}
