package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action to issue an HTTP(S) request using a
 */
@Getter
@Setter
@IgorComponent
public class HttpRequestAction extends BaseWebAction {

    /**
     * Key for the web request action's data.
     */
    private static final String DEFAULT_KEY_WEB_RESPONSE = "webResponse";

    /**
     * Contains all HTTP methods which are unsafe to be executed during simulated job executions.
     */
    private static final Set<String> SIMULATION_UNSAFE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    /**
     * The HTTP method to use.
     */
    @NotBlank
    @Pattern(regexp = "GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH")
    @IgorParam(value = 3, defaultValue = "GET")
    private String method;

    /**
     * The request's headers.
     */
    @IgorParam(value = 4, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * The request's body.
     */
    @IgorParam(value = 5, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String body;

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
     * The target key to put the web response in the data item.
     */
    @NotBlank
    @IgorParam(value = Integer.MAX_VALUE - 1, advanced = true, defaultValue = DEFAULT_KEY_WEB_RESPONSE)
    private String targetKey;

    /**
     * Creates a new HTTP connector instance.
     */
    public HttpRequestAction() {
        super("http-request-action");
        body = "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        parseHeaders(headers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String requestMethod = getString(data, method);
        String requestUrl = getString(data, url);
        String content = getString(data, body);

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .method(requestMethod, HttpRequest.BodyPublishers.ofString(content));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(getString(data, header.split(":")[0]),
                getString(data, header.split(":")[1])));
        addBasicAuthHeaderIfConfigured(httpRequestBuilder);

        if (isSimulation(data) && simulationSafe && SIMULATION_UNSAFE_METHODS.contains(requestMethod)) {
            data.put(DataKey.SIMULATION_LOG.getKey(), "Would have executed '" + requestMethod + "' against: " + requestUrl);
        } else {
            try {
                HttpResponse<String> httpResponse = httpConnector.getHttpClient().send(httpRequestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());

                if (!ignoreErrors && (httpResponse.statusCode() < 200 || httpResponse.statusCode() > 226)) {
                    throw new IgorException("Received HTTP " + httpResponse.statusCode() + " on " + requestMethod + " request for" +
                            " url '" + requestUrl + "': " + " with body: " + content);
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

}