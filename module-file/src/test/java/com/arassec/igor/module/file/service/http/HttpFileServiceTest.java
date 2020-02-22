package com.arassec.igor.module.file.service.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests the {@link HttpFileService}.
 */
@DisplayName("HTTP-File-Service Tests")
class HttpFileServiceTest extends HttpFileServiceBaseTest {

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
        service = new HttpFileService();
        service.setHost("localhost");
        service.setPort(wireMockServer.port());
    }

}