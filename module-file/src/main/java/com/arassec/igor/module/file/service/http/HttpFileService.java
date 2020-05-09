package com.arassec.igor.module.file.service.http;

import com.arassec.igor.core.model.annotation.IgorComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.net.http.HttpClient;

/**
 * File-Service that uses HTTP as protocol.
 */
@Slf4j
@ConditionalOnClass(HttpClient.class)
@IgorComponent
public class HttpFileService extends BaseHttpFileService {

    /**
     * Creates a new component instance.
     */
    public HttpFileService() {
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
