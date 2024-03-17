package com.arassec.igor.plugin.core.web.connector;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link StandardHttpConnector}.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("'HTTP' connector tests.")
class StandardHttpConnectorTest {

    /**
     * A wiremock server for testing.
     */
    protected WireMockServer wireMockServer;

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    public static void initialize() {
        // Disable host name verification for tests:
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
    }

    /**
     * Prepares the test environment.
     */
    @BeforeEach
    public void prepare() {
        wireMockServer = new WireMockServer(new WireMockConfiguration()
                .dynamicHttpsPort()
                .keystorePath("src/test/resources/igor-tests-keystore.jks")
                .keystorePassword("password")
                .httpDisabled(true));

        wireMockServer.start();

        wireMockServer.stubFor(
                get("/ok").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody("OK".getBytes())
                )
        );
    }

    /**
     * Cleans up after tests.
     */
    @AfterEach
    public void cleanup() {
        wireMockServer.resetAll();
        wireMockServer.stop();
    }

    /**
     * Tests the connector's SSL configuration capabilities.
     */
    @Test
    @DisplayName("Tests the connector's SSL configuration capabilities.")
    void testSslConfiguration() {
        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.setTestUrl("https://localhost:" + wireMockServer.httpsPort() + "/ok");
        httpConnector.setCertificateVerification(true);

        httpConnector.initialize(new JobExecution());

        // Should throw exception because of the self-signed server certificate used by wiremock:
        assertThrows(IgorException.class, httpConnector::testConfiguration);

        // Ignoring the problem should work:
        httpConnector.setCertificateVerification(false);
        httpConnector.initialize(new JobExecution());

        assertDoesNotThrow(httpConnector::testConfiguration);

        // Or better: add the self-signed certificate to the list of trusted certificates:
        httpConnector.setCertificateVerification(true);
        httpConnector.setTrustmanagerKeystore("src/test/resources/igor-tests-keystore.jks");
        httpConnector.setTrustmanagerPassword("password");
        httpConnector.setTrustmanagerType("jks");
        httpConnector.initialize(new JobExecution());

        assertDoesNotThrow(httpConnector::testConfiguration);
    }


    /**
     * Tests handling redirects with the connector.
     */
    @Test
    @DisplayName("Tests handling redirects with the connector.")
    void testRedirectHandling() {
        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.setTestUrl("https://localhost:" + wireMockServer.httpsPort() + "/redirect");
        httpConnector.setCertificateVerification(false);

        wireMockServer.stubFor(
                get("/redirect").willReturn(
                        temporaryRedirect("/ok")
                )
        );

        httpConnector.setFollowRedirects(false);
        httpConnector.initialize(new JobExecution());
        assertThrows(IgorException.class, httpConnector::testConfiguration);

        httpConnector.setFollowRedirects(true);
        httpConnector.initialize(new JobExecution());
        assertDoesNotThrow(httpConnector::testConfiguration);
    }

    /**
     * Tests using a proxy for requests.
     */
    @Test
    @DisplayName("Tests using a proxy for requests.")
    void testProxyUsage() {
        WireMockServer proxiedServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        proxiedServer.start();
        proxiedServer.stubFor(
                get("/proxied").willReturn(
                        aResponse()
                                .withStatus(400)
                                .withBody("BAD REQUEST".getBytes())
                )
        );
        proxiedServer.stubFor(
                get("/ok/proxied").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody("OK".getBytes())
                )
        );

        WireMockServer httpProxy = new WireMockServer(new WireMockConfiguration().dynamicPort());
        httpProxy.start();
        httpProxy.stubFor(
                get("/proxied").willReturn(
                        aResponse().proxiedFrom("http://localhost:" + proxiedServer.port() + "/ok")
                )
        );

        StandardHttpConnector httpConnector = new StandardHttpConnector();
        // This URL is not stubbed!
        httpConnector.setTestUrl("http://localhost:" + proxiedServer.port() + "/proxied");

        // Without the proxy, an exception must be thrown:
        httpConnector.initialize(new JobExecution());
        assertThrows(IgorException.class, httpConnector::testConfiguration);

        // With the proxy, the request ist served from '/ok' with HTTP 200:
        httpConnector.setProxyHost("localhost");
        httpConnector.setProxyPort(httpProxy.port());
        httpConnector.initialize(new JobExecution());
        assertDoesNotThrow(httpConnector::testConfiguration);

        proxiedServer.resetAll();
        proxiedServer.stop();
        httpProxy.resetAll();
        httpProxy.stop();
    }

    /**
     * Tests authentication with client certificates.
     */
    @Test
    @DisplayName("Tests authentication with client certificates.")
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true") // Disabled on Github due to instability.
    void testClientCertAuth() {
        // Start a server which requires client authentication:
        WireMockServer server = new WireMockServer(new WireMockConfiguration()
                .dynamicPort()
                .dynamicHttpsPort()
                .trustStorePath("src/test/resources/igor-tests-keystore.jks")
                .trustStorePassword("password")
                .keystorePath("src/test/resources/igor-tests-keystore.jks")
                .keystorePassword("password")
                .needClientAuth(true));

        server.start();

        server.stubFor(
                get("/ok").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody("OK".getBytes())
                )
        );

        StandardHttpConnector httpConnector = new StandardHttpConnector();
        httpConnector.setCertificateVerification(false);
        httpConnector.setTestUrl("https://localhost:" + server.httpsPort() + "/ok");

        httpConnector.initialize(new JobExecution());

        // Should throw exception because of the self-signed server certificate used by wiremock:
        try {
            httpConnector.testConfiguration();
            fail("Should have thrown an IgorException due to missing client certificate!");
        } catch (IgorException e) {
            // Extra check here, to make sure the request really failed because of the missing certificate!
            log.info("Expected test exception!", e);
            assertTrue(e.getMessage().contains("bad_certificate"));
        }

        // Use the test keystore as client certificate repository:
        httpConnector.setKeymanagerKeystore("src/test/resources/igor-tests-keystore.jks");
        httpConnector.setKeymanagerPassword("password");
        httpConnector.setKeymanagerType("jks");
        httpConnector.initialize(new JobExecution());

        assertDoesNotThrow(httpConnector::testConfiguration);

        server.resetAll();
        server.stop();
    }

    /**
     * Tests testing the connector without a configuration.
     */
    @Test
    @DisplayName("Tests testing the connector without a configuration.")
    void testTestConfigurationWithoutConfiguration() {
        StandardHttpConnector httpConnector = new StandardHttpConnector();
        assertDoesNotThrow(httpConnector::testConfiguration);
    }

}
