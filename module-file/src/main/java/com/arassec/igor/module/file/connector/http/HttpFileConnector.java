package com.arassec.igor.module.file.connector.http;

import com.arassec.igor.core.model.annotation.IgorComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.net.http.HttpClient;

/**
 * File-Connector that uses HTTP as protocol.
 */
@Slf4j
@ConditionalOnClass(HttpClient.class)
@IgorComponent
public class HttpFileConnector extends BaseHttpFileConnector {

    /**
     * Creates a new component instance.
     */
    public HttpFileConnector() {
        super("http-file-connector");
    }

    /**
     * Creates a new HTTP-Client.
     *
     * @return A newly created {@link HttpClient}.
     */
    @Override
    protected HttpClient connect() {
        HttpClient.Redirect redirectPolicy = HttpClient.Redirect.ALWAYS;
        if (!isFollowRedirects()) {
            redirectPolicy = HttpClient.Redirect.NEVER;
        }
        return HttpClient.newBuilder().followRedirects(redirectPolicy).build();
    }

}
