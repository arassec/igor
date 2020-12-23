package com.arassec.igor.plugin.common.web.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.plugin.common.CommonCategory;
import com.arassec.igor.plugin.common.web.connector.FallbackHttpConnector;
import com.arassec.igor.plugin.common.web.connector.HttpConnector;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.http.HttpRequest;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for web actions.
 */
@Setter
@Getter
public abstract class BaseWebAction extends BaseAction {

    /**
     * The connector to use for requests.
     */
    @NotNull
    @IgorParam(1)
    protected HttpConnector httpConnector;

    /**
     * The URL to request.
     */
    @NotBlank
    @IgorParam(2)
    protected String url;

    /**
     * A username for authentication.
     */
    @IgorParam(value = Integer.MAX_VALUE - 3, advanced = true)
    protected String username;

    /**
     * The password for authentication.
     */
    @IgorParam(value = Integer.MAX_VALUE - 2, advanced = true, secured = true)
    protected String password;

    /**
     * Contains the parsed headers.
     */
    protected List<String> parsedHeaders = new LinkedList<>();

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseWebAction(String typeId) {
        super(CommonCategory.WEB.getId(), typeId);
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
     */
    protected void addBasicAuthHeaderIfConfigured(HttpRequest.Builder httpRequestBuilder) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            httpRequestBuilder.header("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        }
    }

}
