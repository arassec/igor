package com.arassec.igor.module.file.service.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests the {@link HttpFileService}.
 */
@DisplayName("HTTPS-File-Service Tests")
class HttpsFileServiceTest extends HttpFileServiceBaseTest {

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    public static void initialize() {
        wireMockServer = new WireMockServer(new WireMockConfiguration()
                .dynamicHttpsPort()
                .httpDisabled(true));
        wireMockServer.start();

        // Disable host name verification for tests:
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
    }

    /**
     * Prepares the test environment.
     */
    @BeforeEach
    public void prepare() {
        service = new HttpsFileService();
        service.setHost("localhost");
        service.setPort(wireMockServer.httpsPort());

        // For now, we simply accept the WireMock-SSL-Certificate:
        ((HttpsFileService) service).setCertificateVerification(false);
    }

}