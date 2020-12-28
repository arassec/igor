package com.arassec.igor.plugin.core.file.connector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains information about a file.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    /**
     * The file's name.
     */
    private String filename;

    /**
     * Timestamp of the last modification of the file.
     */
    private String lastModified;

}
