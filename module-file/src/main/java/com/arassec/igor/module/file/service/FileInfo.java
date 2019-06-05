package com.arassec.igor.module.file.service;

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

    private String fileName;

    private String lastModified;

}
