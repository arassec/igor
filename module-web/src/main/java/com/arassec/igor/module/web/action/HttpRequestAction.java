package com.arassec.igor.module.web.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.module.web.connector.FallbackHttpConnector;
import com.arassec.igor.module.web.connector.HttpConnector;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Action to issue an HTTP(S) request using a
 */
@Getter
@Setter
@IgorComponent
public class HttpRequestAction extends BaseAction {

    /**
     * Key for the web request action's data.
     */
    public static final String KEY_WEB_REQUEST_ACTION = "webResponse";

    /**
     * The connector to use for requests.
     */
    @NotNull
    @IgorParam
    private HttpConnector httpConnector;

    /**
     * The URL to request.
     */
    @NotEmpty
    @IgorParam
    private String url;

    /**
     * The HTTP method to use.
     */
    @NotEmpty
    @IgorParam(defaultValue = "GET")
    private String method;

    /**
     * The request's headers.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * The request's body.
     */
    @IgorParam(advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String body;

    /**
     * Enables igor to treat any HTTP result code as if it was HTTP 200.
     */
    @IgorParam(advanced = true)
    private boolean ignoreErrors;

    /**
     * A username for authentication.
     */
    @IgorParam(advanced = true)
    protected String username;

    /**
     * The password for authentication.
     */
    @IgorParam(advanced = true, secured = true)
    protected String password;

    /**
     * Pattern to extract variables from the message template.
     */
    private final Pattern pattern = Pattern.compile("##(.*?)##");

    /**
     * Contains the parsed headers.
     */
    private List<String> parsedHeaders = new LinkedList<>();

    /**
     * Creates a new HTTP connector instance.
     */
    public HttpRequestAction() {
        super("web-actions", "http-request-action");
        httpConnector = new FallbackHttpConnector();
        body = "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, JobExecution jobExecution) {
        super.initialize(jobId, jobExecution);
        if (StringUtils.hasText(headers)) {
            String[] headerParts = headers.split("\\r?\\n");
            for (String header : headerParts) {
                if (header.contains("=")) {
                    parsedHeaders.add(header);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String requestUrl = getString(data, url);

        Matcher m = pattern.matcher(body);
        while (m.find()) {
            // e.g. ##$.data.targetFilename##
            String variableName = m.group();
            // e.g. file.zip
            String variableContent = getString(data, variableName.replace("##", ""));
            if (variableContent != null) {
                body = body.replace(variableName, variableContent);
            }
        }

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .method(this.method, HttpRequest.BodyPublishers.ofString(body));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(header.split("=")[0], header.split("=")[1]));
        addBasicAuthHeaderIfConfigured(httpRequestBuilder);

        try {
            HttpResponse<String> httpResponse = httpConnector.getHttpClient().send(httpRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            if (!ignoreErrors && httpResponse.statusCode() != 200) {
                throw new IgorException("Received HTTP error code on web request for url '" + requestUrl + "': "
                        + httpResponse.statusCode());
            }

            String responseBody = httpResponse.body();
            Object parsedResponseBody = JSONValue.parse(responseBody);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("headers", httpResponse.headers().map());

            if (parsedResponseBody instanceof JSONObject || parsedResponseBody instanceof JSONArray) {
                responseData.put("body", parsedResponseBody);
            } else {
                responseData.put("body", responseBody);
            }

            data.put(KEY_WEB_REQUEST_ACTION, responseData);
        } catch (IOException e) {
            throw new IgorException("Could not request URL: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IgorException("Interrupted during HTTP request!", e);
        }

        return List.of(data);
    }

    /**
     * Creates an authorization header with the configured username and password and adds it to the builder.
     */
    private void addBasicAuthHeaderIfConfigured(HttpRequest.Builder httpRequestBuilder) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            httpRequestBuilder.header("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        }
    }

}
