package com.arassec.igor.module.file.connector.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests the {@link HttpFileConnector}.
 */
@DisplayName("HTTP-File-Connector Tests")
class HttpFileConnectorTest extends HttpFileConnectorBaseTest {

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    public static void initialize() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();
    }

    /**
     * Prepares the test environment.
     */
    @BeforeEach
    public void prepare() {
        connector = new HttpFileConnector();
        connector.setHost("localhost");
        connector.setPort(wireMockServer.port());
    }

}