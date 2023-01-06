package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * <h2>'HTTP Request' Action</h2>
 *
 * <h3>Description</h3>
 * This action issues HTTP requests against web servers.<br>
 *
 * The action adds the results of the request to the data item under the 'webResponse' key.<br>
 *
 * A data item processed by this action could look like this:
 * <pre><code>
 * {
 *   "data": {},
 *   "meta": {
 *     "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90",
 *     "simulation": true,
 *     "timestamp": 1601302794228
 *   },
 *   "webResponse": {
 *     "headers": {
 *       ":status": [
 *         "200"
 *       ],
 *       "access-control-allow-credentials": [
 *         "true"
 *       ],
 *       "access-control-allow-origin": [
 *         "*"
 *       ],
 *       "content-length": [
 *         "451"
 *       ],
 *       "content-type": [
 *         "application/json"
 *       ],
 *       "date": [
 *         "Mon, 28 Sep 2020 14:19:52 GMT"
 *       ],
 *       "server": [
 *         "gunicorn/19.9.0"
 *       ]
 *     },
 *     "body": {
 *       "args": {},
 *       "json": {
 *         "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90"
 *       },
 *       "url": "https://httpbin.org/post"
 *     }
 *   }
 * }
 * </code></pre>
 */
@Getter
@Setter
@IgorComponent(typeId = "http-request-action", categoryId = CoreCategory.WEB)
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
     * The HTTP method. Can be one of 'GET', 'HEAD', 'POST', 'PUT', 'DELETE', 'CONNECT', 'OPTIONS', 'TRACE' or 'PATCH'.
     */
    @NotBlank
    @Pattern(regexp = "GET|HEAD|POST|PUT|DELETE|CONNECT|OPTIONS|TRACE|PATCH")
    @IgorParam(sortIndex = 3)
    private String method = "GET";

    /**
     * The HTTP headers to use for the request. Headers must be entered as 'key: value'-pairs, with each header in a separate
     * line. Mustache templates are <strong>not</strong> supported in headers.
     */
    @IgorParam(sortIndex = 4, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * The HTTP body to use for the request.
     */
    @IgorParam(sortIndex = 5, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String body;

    /**
     * If checked, igor will ignore the HTTP result code and treat every response as HTTP 200 ('OK').
     */
    @IgorParam(sortIndex = Integer.MAX_VALUE - 5, advanced = true)
    private boolean ignoreErrors;

    /**
     * If checked, HTTP POST, PUT and DELETE will not be executed by the action during simulated job executions.
     */
    @IgorParam(sortIndex = Integer.MAX_VALUE - 4, advanced = true)
    private boolean simulationSafe = true;

    /**
     * The target key to put the web response in the data item.
     */
    @NotBlank
    @IgorParam(sortIndex = Integer.MAX_VALUE - 1, advanced = true)
    private String targetKey = DEFAULT_KEY_WEB_RESPONSE;

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

        var requestMethod = CorePluginUtils.evaluateTemplate(data, method);
        var requestUrl = CorePluginUtils.evaluateTemplate(data, url);
        var content = Optional.ofNullable(CorePluginUtils.evaluateTemplate(data, body)).orElse("");

        var httpRequestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .method(requestMethod, HttpRequest.BodyPublishers.ofString(content));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(CorePluginUtils.evaluateTemplate(data, header.split(":")[0]),
            CorePluginUtils.evaluateTemplate(data, header.split(":")[1])));
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

                data.put(CorePluginUtils.evaluateTemplate(data, targetKey), responseData);
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
