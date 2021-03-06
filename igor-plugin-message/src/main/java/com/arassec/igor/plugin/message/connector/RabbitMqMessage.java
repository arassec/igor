package com.arassec.igor.plugin.message.connector;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Models a RabbitMQ message to send with a igor.
 */
@Data
public class RabbitMqMessage {

    /**
     * The message's headers.
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * The message's content encoding.
     */
    private String contentEncoding = "UTF-8";

    /**
     * The message's content type.
     */
    private String contentType = "text/plain";

    /**
     * The messages content.
     */
    private String content;

}
