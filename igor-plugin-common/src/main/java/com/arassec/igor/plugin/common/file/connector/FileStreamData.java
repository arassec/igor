package com.arassec.igor.plugin.common.file.connector;

import lombok.Data;

import java.io.InputStream;

/**
 * Data container for file streams.
 */
@Data
public class FileStreamData {

    /**
     * Contains additional information about the connection to the source connector. This is sometimes required, e.g. to close an
     * SSH session propertly after the file hast been copied.
     */
    private Object sourceConnectionData;

    /**
     * The size of the file that is represented by this object.
     */
    private long fileSize;

    /**
     * The input stream to the file.
     */
    private InputStream data;

    /**
     * An optional suffix for the target filename.
     */
    private String filenameSuffix;

}
