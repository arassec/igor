package com.arassec.igor.plugin.file;

/**
 * Defines file type IDs.
 */
public final class FileType {

    /**
     * The "FTP" file connector type.
     */
    public static final String FTP_CONNECTOR = "ftp-file-connector";

    /**
     * The "FTPS" file connector type.
     */
    public static final String FTPS_CONNECTOR = "ftps-file-connector";

    /**
     * The "SCP" file connector type.
     */
    public static final String SCP_CONNECTOR = "scp-file-connector";

    /**
     * The "SFTP" file connector type.
     */
    public static final String SFTP_CONNECTOR = "sftp-file-connector";

    /**
     * Creates a new instance.
     */
    private FileType() {
        // prevent instantiation.
    }

}
