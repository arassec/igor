package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.plugin.core.web.connector.FallbackHttpConnector;
import com.arassec.igor.plugin.core.web.connector.HttpConnector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.net.http.HttpRequest;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for web actions.
 */
@Setter
@Getter
public abstract class BaseHttpAction extends BaseAction {

    /**
     * An 'HTTP Connector' that is configured to execute the request.
     */
    @NotNull
    @IgorParam(sortIndex = 1)
    protected HttpConnector httpConnector;

    /**
     * The HTTP(S) URL for the request.
     */
    @NotBlank
    @IgorParam(sortIndex = 2)
    protected String url;

    /**
     * A username for HTTP Basic Authentication. If set together with a password, a HTTP Authorization header with the encoded password will be added to every request.
     */
    @IgorParam(sortIndex = Integer.MAX_VALUE - 3, advanced = true)
    protected String username;

    /**
     * A password for HTTP Basic Authentication.
     */
    @IgorParam(sortIndex = Integer.MAX_VALUE - 2, advanced = true, secured = true)
    protected String password;

    /**
     * Contains the parsed headers.
     */
    protected List<String> parsedHeaders = new LinkedList<>();

    /**
     * Creates a new component instance.
     */
    protected BaseHttpAction() {
        httpConnector = new FallbackHttpConnector();
    }

    /**
     * Parses configured headers to later use them in web requests.
     *
     * @param headers The headers as unformatted string. Each header has to be on a separate line and in the form 'key:value'.
     */
    protected void parseHeaders(String headers) {
        if (StringUtils.hasText(headers)) {
            String[] headerParts = headers.split("\\r?\\n");
            for (String header : headerParts) {
                if (header.contains(":")) {
                    parsedHeaders.add(header);
                }
            }
        }
    }

    /**
     * Creates an authorization header with the configured username and password and adds it to the builder.
     *
     * @param httpRequestBuilder The HTTP Request-Build to add the Basic-Auth header to.
     */
    protected void addBasicAuthHeaderIfConfigured(HttpRequest.Builder httpRequestBuilder) {
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            httpRequestBuilder.header("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        }
    }

}
