package com.arassec.igor.plugin.common.message.connector;

import lombok.Data;

/**
 * Models a message to send with a {@link MessageConnector}.
 */
@Data
public class Message {

    /**
     * The messages content.
     */
    private String content;

}
