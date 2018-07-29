package com.arassec.igor.core.model.service.file;

import java.io.InputStream;

/**
 * Data container for file streams.
 */
public class FileStreamData {

    private Object sourceConnectionData;

    private long fileSize;

    private InputStream data;

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public Object getSourceConnectionData() {
        return sourceConnectionData;
    }

    public void setSourceConnectionData(Object sourceConnectionData) {
        this.sourceConnectionData = sourceConnectionData;
    }

}
