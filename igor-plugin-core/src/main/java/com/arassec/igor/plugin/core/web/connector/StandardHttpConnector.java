package com.arassec.igor.plugin.core.web.connector;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Optional;

/**
 * <h2>HTTP Connector</h2>
 *
 * <h3>Description</h3>
 * The HTTP connector can be used to query web servers via HTTP/HTTPS.<br>
 * <p>
 * The java.net.http.HttpClient is used internally to connect to servers. All configuration options available, e.g. with JVM
 * parameters, will automatically be used by the connector.
 */
@Slf4j
@IgorComponent(typeId = "http-web-connector", categoryId = CoreCategory.WEB)
public class StandardHttpConnector extends BaseConnector implements HttpConnector {

    /**
     * Protocol for the SSL-Context.
     */
    private static final String SSL_CONTEXT_PROTOCOL = "TLSv1.2";

    /**
     * An HTTP(S) URL that is queried via HTTP-GET when the connector's configuration is tested by the user.
     */
    @Getter
    @Setter
    @IgorParam
    private String testUrl;

    /**
     * An optional proxy server for HTTP(S) connections.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private String proxyHost;

    /**
     * The port of the proxy server.
     */
    @Getter
    @Setter
    @Positive
    @IgorParam(advanced = true)
    private Integer proxyPort = 80;

    /**
     * Path to a keystore file containing the HTTP client's keys.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private String keymanagerKeystore;

    /**
     * Password for the keymanager's keystore file.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, secured = true)
    private String keymanagerPassword;

    /**
     * Type of the keymanager's keystore.
     */
    @Getter
    @Setter
    @NotBlank
    @IgorParam(advanced = true)
    private String keymanagerType = "pkcs12";

    /**
     * Path to a keystore file containing trusted server certificates.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private String trustmanagerKeystore;

    /**
     * Password for the trustmanager's keystore file.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, secured = true)
    private String trustmanagerPassword;

    /**
     * Type of the trustmanager's keystore.
     */
    @Getter
    @Setter
    @NotBlank
    @IgorParam(advanced = true)
    private String trustmanagerType = "pkcs12";

    /**
     * If checked, server SSL certificates will be validated by the connector. If unchecked, all provided certificates will be
     * accepted, <strong>THUS DISABLING SECURITY!</strong>
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private boolean certificateVerification = true;

    /**
     * If checked, the connector will follow redirects in a secure way (e.g. not leaving SSL connections). If unchecked, HTTP
     * redirects will be treated as errors.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private boolean followRedirects = true;

    /**
     * The HTTP-Client for web requests.
     */
    private HttpClient httpClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);

        var httpClientBuilder = HttpClient.newBuilder();

        if (followRedirects) {
            httpClientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
        } else {
            httpClientBuilder.followRedirects(HttpClient.Redirect.NEVER);
        }

        if (StringUtils.hasText(proxyHost) && proxyPort != null) {
            httpClientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
        }

        KeyManager[] keyManagers = null;
        TrustManager[] trustManagers = null;

        if (StringUtils.hasText(keymanagerKeystore)) {
            String password = Optional.ofNullable(keymanagerPassword).orElse("");
            try (InputStream keystoreInputStream = new FileInputStream(keymanagerKeystore)) {
                var keyStore = KeyStore.getInstance(keymanagerType);
                keyStore.load(keystoreInputStream, password.toCharArray());

                var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, password.toCharArray());

                keyManagers = keyManagerFactory.getKeyManagers();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
                throw new IgorException("Could not create KeyManager!", e);
            }
        }

        if (!certificateVerification) {
            trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }

                    // For now, all certificates are accepted if the user wishes to do so. In the future, igor will provide
                    // the ability to accept specific, invalid certificates.
                    @SuppressWarnings({"java:S4830", "squid:S4424"})
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                        // Accept all certificates...
                    }

                    // For now, all certificates are accepted if the user wishes to do so. In the future, igor will provide
                    // the ability to accept specific, invalid certificates.
                    @SuppressWarnings({"java:S4830", "squid:S4424"})
                    public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                        // Accept all certificates...
                    }
                }
            };
        } else if (StringUtils.hasText(trustmanagerKeystore)) {
            String password = Optional.ofNullable(trustmanagerPassword).orElse("");
            try (InputStream keystoreInputStream = new FileInputStream(trustmanagerKeystore)) {
                var keyStore = KeyStore.getInstance(trustmanagerType);
                keyStore.load(keystoreInputStream, password.toCharArray());

                var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                trustManagers = trustManagerFactory.getTrustManagers();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new IgorException("Could not create TrustManager!", e);
            }
        }

        if (keyManagers != null || trustManagers != null) {
            try {
                var sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
                sslContext.init(keyManagers, trustManagers, null);
                httpClientBuilder.sslContext(sslContext);
                httpClientBuilder.sslParameters(sslContext.getDefaultSSLParameters());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IgorException("Could not initialize SSLContext!", e);
            }
        }

        httpClient = httpClientBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        if (!StringUtils.hasText(testUrl)) {
            return;
        }
        initialize(new JobExecution());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(testUrl)).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 226) {
                throw new IgorException("Received HTTP error code: " + response.statusCode());
            }
        } catch (IOException e) {
            throw new IgorException("Exception during request: " + e.getMessage() + " (" + request.uri().toString() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IgorException("Exception during request: " + e.getMessage() + " (" + request.uri().toString() + ")");
        }
    }

    /**
     * Returns the configured {@link HttpClient}.
     *
     * @return The configured HTTP client.
     */
    @IgorSimulationSafe
    public HttpClient getHttpClient() {
        return httpClient;
    }

}
