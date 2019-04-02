package com.arassec.igor.module.message.service;

import lombok.Data;

/**
 * Models a message to send with a {@link MessageService}.
 */
@Data
public class Message {

    /**
     * The messages content.
     */
    private String content;

}
