package com.arassec.igor.plugin.common;

import lombok.Getter;

/**
 * Defines common data keys used in data items by various actions.
 */
public enum CommonDataKey {

    /**
     * Key for "copy file" action data.
     */
    COPIED_FILE("copiedFile"),

    /**
     * Key for "downloaded file" action data.
     */
    DOWNLOADED_FILE("downloadedFile"),

    /**
     * Key to the source file's name.
     */
    SOURCE_FILENAME("sourceFilename"),

    /**
     * Key to the source file's directory.
     */
    SOURCE_DIRECTORY("sourceDirectory"),

    /**
     * Key to the target file's name.
     */
    TARGET_FILENAME("targetFilename"),

    /**
     * Key to the target directory.
     */
    TARGET_DIRECTORY("targetDirectory");

    /**
     * The key as String.
     */
    @Getter
    private final String key;

    /**
     * Creates a new instance.
     *
     * @param key The key as String.
     */
    CommonDataKey(String key) {
        this.key = key;
    }

    }
