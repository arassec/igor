package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
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
public class HttpRequestAction extends BaseHttpAction {

    /**
     * Key for the web request action's data.
     */
    private static final String DEFAULT_KEY_WEB_RESPONSE = "webResponse";

    /**
     * Contains all HTTP methods which are unsafe to be executed during simulated job executions.
     */
    private static final Set<String> SIMULATION_UNSAFE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    /**
     * Jackson's {@link ObjectMapper} to parse response JSON.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The HTTP method to use.
     */
    @NotBlank
    @Pattern(regexp = "GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH")
    @IgorParam(sortIndex = 3)
    private String method = "GET";

    /**
     * The request's headers.
     */
    @IgorParam(sortIndex = 4, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * The request's body.
     */
    @IgorParam(sortIndex = 5, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String body;

    /**
     * Enables igor to treat any HTTP result code as if it was HTTP 200.
     */
    @IgorParam(advanced = true)
    private boolean ignoreErrors;

    /**
     * Only executes idempotent web requests if {@code true}.
     */
    @IgorParam(advanced = true)
    private boolean simulationSafe = true;

    /**
     * The target key to put the web response in the data item.
     */
    @NotBlank
    @IgorParam(sortIndex = Integer.MAX_VALUE - 1, advanced = true)
    private String targetKey = DEFAULT_KEY_WEB_RESPONSE;

    /**
     * Creates a new HTTP connector instance.
     */
    public HttpRequestAction() {
        super(CorePluginType.HTTP_REQUEST_ACTION.getId());
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

        var requestMethod = CorePluginUtils.getString(data, method);
        var requestUrl = CorePluginUtils.getString(data, url);
        var content = Optional.ofNullable(CorePluginUtils.getString(data, body)).orElse("");

        var httpRequestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .method(requestMethod, HttpRequest.BodyPublishers.ofString(content));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(CorePluginUtils.getString(data, header.split(":")[0]),
            CorePluginUtils.getString(data, header.split(":")[1])));
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

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("headers", httpResponse.headers().map());
                responseData.put("body", parseResponseBody(httpResponse));

                data.put(CorePluginUtils.getString(data, targetKey), responseData);
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
     * Tries to parse the response body as JSON and returns it if possible.
     *
     * @param httpResponse The received HTTP response.
     *
     * @return The
     */
    private Object parseResponseBody(HttpResponse<String> httpResponse) {
        try {
            var jsonNode = objectMapper.readTree(httpResponse.body());
            return objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return httpResponse.body();
        }
    }

}
