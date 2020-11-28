package com.arassec.igor.plugin.common.web.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.common.CommonCategory;
import com.arassec.igor.plugin.common.web.connector.FallbackHttpConnector;
import com.arassec.igor.plugin.common.web.connector.HttpConnector;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

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
    private static final String DEFAULT_KEY_WEB_RESPONSE = "webResponse";

    /**
     * Contains all HTTP methods which are unsafe to be executed during simulated job executions.
     */
    private static final Set<String> SIMULATION_UNSAFE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    /**
     * The connector to use for requests.
     */
    @NotNull
    @IgorParam
    private HttpConnector httpConnector;

    /**
     * The URL to request.
     */
    @NotBlank
    @IgorParam
    private String url;

    /**
     * The HTTP method to use.
     */
    @NotBlank
    @Pattern(regexp = "GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH")
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
     * A username for authentication.
     */
    @IgorParam(advanced = true)
    private String username;

    /**
     * The password for authentication.
     */
    @IgorParam(advanced = true, secured = true)
    private String password;

    /**
     * The target key to put the web response in the data item.
     */
    @NotBlank
    @IgorParam(advanced = true, defaultValue = DEFAULT_KEY_WEB_RESPONSE)
    private String targetKey;

    /**
     * Enables igor to treat any HTTP result code as if it was HTTP 200.
     */
    @IgorParam(advanced = true)
    private boolean ignoreErrors;

    /**
     * Doesn't execute non-idempotent web requests if {@code true}.
     */
    @IgorParam(advanced = true, defaultValue = "true")
    private boolean simulationSafe;

    /**
     * Contains the parsed headers.
     */
    private List<String> parsedHeaders = new LinkedList<>();

    /**
     * Creates a new HTTP connector instance.
     */
    public HttpRequestAction() {
        super(CommonCategory.WEB.getId(), "http-request-action");
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
                if (header.contains(":")) {
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
        String content = getString(data, body);

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .method(this.method, HttpRequest.BodyPublishers.ofString(content));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(getString(data, header.split(":")[0]),
                getString(data, header.split(":")[1])));
        addBasicAuthHeaderIfConfigured(httpRequestBuilder);

        if (isSimulation(data) && simulationSafe && SIMULATION_UNSAFE_METHODS.contains(method)) {
            data.put(DataKey.SIMULATION_LOG.getKey(), "Would have executed '" + method + "' against: " + requestUrl);
        } else {
            try {
                HttpResponse<String> httpResponse = httpConnector.getHttpClient().send(httpRequestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());

                if (!ignoreErrors && (httpResponse.statusCode() < 200 || httpResponse.statusCode() > 226)) {
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

                data.put(getString(data, targetKey), responseData);
            } catch (IOException e) {
                throw new IgorException("Could not request URL: " + url, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IgorException("Interrupted during HTTP request!", e);
            }
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
