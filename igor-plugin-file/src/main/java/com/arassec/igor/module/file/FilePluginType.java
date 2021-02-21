package com.arassec.igor.module.file;

import lombok.Getter;

/**
 * Defines types for components of the 'file' plugin.
 */
public enum FilePluginType {

    /**
     * Type of the "SFTP File" connector.
     */
    SFTP_FILE_CONNECTOR("sftp-file-connector"),

    /**
     * Type of the "SCP File" connector.
     */
    SCP_FILE_CONNECTOR("scp-file-connector"),

    /**
     * Type of the "FTPS File" connector.
     */
    FTPS_FILE_CONNECTOR("ftps-file-connector"),

    /**
     * Type of the "FTP File" connector.
     */
    FTP_FILE_CONNECTOR("ftp-file-connector");

    /**
     * The type's ID.
     */
    @Getter
    private final String id;

    /**
     * Creates a new type ID.
     *
     * @param id The ID to use.
     */
    FilePluginType(String id) {
        this.id = id;
    }

}
