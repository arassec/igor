package com.arassec.igor.module.file.connector.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests the {@link HttpsFileConnector}.
 *
 * WARNING: These tests may fail in IntelliJ if run together with the {@link HttpFileConnectorTest}.
 */
@DisplayName("HTTPS-File-Connector Tests")
class HttpsFileConnectorTest extends HttpFileConnectorBaseTest {

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    public static void initialize() {
        // Disable host name verification for tests:
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

        wireMockServer = new WireMockServer(new WireMockConfiguration()
                .dynamicHttpsPort()
                .httpDisabled(true));
        wireMockServer.start();
    }

    /**
     * Prepares the test environment.
     */
    @BeforeEach
    public void prepare() {
        connector = new HttpsFileConnector();
        connector.setHost("localhost");
        connector.setPort(wireMockServer.httpsPort());
        connector.setTimeout(3600);

        // For now, we simply accept the WireMock-SSL-Certificate:
        ((HttpsFileConnector) connector).setCertificateVerification(false);
    }

}