package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.web.connector.StandardHttpConnector;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link HttpRequestAction}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'HTTP Request' action tests.")
class HttpRequestActionTest {

    /**
     * Tests initializing the action.
     */
    @Test
    @DisplayName("Tests initializing the action.")
    void testInitialize() {
        HttpRequestAction action = new HttpRequestAction();
        action.setHeaders("a: b\nc:d\nalpha\r\ne:f");

        action.initialize(new JobExecution());

        assertEquals(3, action.getParsedHeaders().size());
        assertEquals("a: b", action.getParsedHeaders().get(0));
        assertEquals("c:d", action.getParsedHeaders().get(1));
        assertEquals("e:f", action.getParsedHeaders().get(2));
    }

    /**
     * Tests sending an HTTP POST request with custom headers and body.
     */
    @Test
    @DisplayName("Tests sending an HTTP POST request with custom headers and body.")
    void testHttpPostWithCustomHeaderAndBody() {
        WireMockServer httpServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        httpServer.start();
        httpServer.stubFor(
                post("/test")
                        .withHeader("X-Igor-Test", equalTo("variable-value"))
                        .withRequestBody(equalTo("The POST request body: variable-value"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody("{\"a\": \"b\", \"c\": 123}".getBytes())
                        )
        );

        JobExecution jobExecution = new JobExecution();

        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.initialize(jobExecution);

        HttpRequestAction action = new HttpRequestAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("http://localhost:" + httpServer.port() + "/test");
        action.setMethod("POST");
        action.setHeaders("X-Igor-Test: {{variable}}");
        action.setBody("The POST request body: {{variable}}");
        action.setTargetKey("webResponse");

        action.initialize(jobExecution);

        Map<String, Object> data = new HashMap<>();
        data.put("variable", "variable-value");
        List<Map<String, Object>> result = action.process(data, jobExecution);

        assertEquals(1, result.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> webResponse = (Map<String, Object>) result.get(0).get("webResponse");
        assertTrue(webResponse.containsKey("headers"));

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>)  webResponse.get("body");
        assertEquals("b", body.get("a"));
        assertEquals(123, body.get("c"));

        httpServer.resetAll();
        httpServer.stop();
    }

    /**
     * Tests configuring username and password for Basic Auth.
     */
    @Test
    @DisplayName("Tests configuring username and password for Basic Auth.")
    void testBasicAuth() {
        WireMockServer httpServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        httpServer.start();
        httpServer.stubFor(
                get("/auth-test")
                        .withBasicAuth("igor", "action-test")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody("OK".getBytes())
                        )
        );

        JobExecution jobExecution = new JobExecution();

        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.initialize(jobExecution);

        HttpRequestAction action = new HttpRequestAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("http://localhost:" + httpServer.port() + "/auth-test");
        action.setMethod("GET");

        action.initialize(jobExecution);

        Map<String, Object> data = new HashMap<>();
        assertThrows(IgorException.class, () -> action.process(data, jobExecution));

        action.setUsername("igor");
        assertThrows(IgorException.class, () -> action.process(data, jobExecution));

        action.setPassword("action-test");
        assertDoesNotThrow(() -> action.process(data, jobExecution));

        httpServer.resetAll();
        httpServer.stop();
    }

    /**
     * Tests sending an HTTP DELETE request while simulating the job.
     */
    @Test
    @DisplayName("Tests sending an HTTP DELETE request while simulating the job.")
    void testHttpDeleteInSimulationMode() {
        JobExecution jobExecution = new JobExecution();

        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.initialize(jobExecution);

        HttpRequestAction action = new HttpRequestAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("http://localhost/must-not-be-called");
        action.setMethod("DELETE");
        action.setSimulationSafe(true);

        action.initialize(jobExecution);

        Map<String, Object> data = new HashMap<>();
        data.put(DataKey.META.getKey(), Map.of(DataKey.SIMULATION.getKey(), true));

        List<Map<String, Object>> result = action.process(data, jobExecution);

        assertEquals(1, result.size());
        assertEquals("Would have executed 'DELETE' against: http://localhost/must-not-be-called",
                result.get(0).get(DataKey.SIMULATION_LOG.getKey()));
    }

    /**
     * Tests error handling.
     */
    @Test
    @DisplayName("Tests error handling.")
    void testErrorHandling() {
        JobExecution jobExecution = new JobExecution();

        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.initialize(jobExecution);

        HttpRequestAction action = new HttpRequestAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("http://no-server-running");
        action.setMethod("DELETE");
        action.setSimulationSafe(true);

        action.initialize(jobExecution);

        Map<String, Object> data = Map.of();
        assertThrows(IgorException.class, () -> action.process(data, jobExecution));

        WireMockServer httpServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        httpServer.start();

        action.setUrl("http://localhost:" + httpServer.port() + "/not-existing");
        assertThrows(IgorException.class, () -> action.process(data, jobExecution));

        httpServer.resetAll();
        httpServer.stop();
    }

}
