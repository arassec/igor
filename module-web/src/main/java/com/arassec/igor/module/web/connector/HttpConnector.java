package com.arassec.igor.module.web.connector;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
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
 * A connector to HTTP(S) servers.
 */
@Slf4j
@IgorComponent
public class HttpConnector extends BaseConnector {

    /**
     * Protocol for the SSL-Context.
     */
    private static final String SSL_CONTEXT_PROTOCOL = "TLSv1.2";

    /**
     * An URL for testing the configuration.
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
    @IgorParam(advanced = true, defaultValue = "80")
    private Integer proxyPort;

    /**
     * Path to a keystore file for the SSL {@link KeyManager}.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private String keymanagerKeystore;

    /**
     * The password for the {@link KeyManager}'s keystore file.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, secured = true)
    private String keymanagerPassword;

    /**
     * The type of the {@link KeyManager}'s keystore file.
     */
    @Getter
    @Setter
    @NotBlank
    @IgorParam(advanced = true, defaultValue = "pkcs12")
    private String keymanagerType;

    /**
     * Path to a keystore file for the SSL {@link TrustManager}.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true)
    private String trustmanagerKeystore;

    /**
     * The password for the {@link TrustManager}'s keystore file.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, secured = true)
    private String trustmanagerPassword;

    /**
     * The type of the {@link TrustManager}'s keystore file.
     */
    @Getter
    @Setter
    @NotBlank
    @IgorParam(advanced = true, defaultValue = "pkcs12")
    private String trustmanagerType;

    /**
     * Enables or disables SSL certificate verification.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, defaultValue = "true")
    private boolean certificateVerification;

    /**
     * Enables or disables following redirects.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, defaultValue = "true")
    private boolean followRedirects;

    /**
     * The HTTP-Client for web requests.
     */
    private HttpClient httpClient;

    /**
     * Creates a new connector instance.
     */
    public HttpConnector() {
        super("web-connectors", "http-web-connector");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, JobExecution jobExecution) {
        super.initialize(jobId, jobExecution);

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();

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
                KeyStore keyStore = KeyStore.getInstance(keymanagerType);
                keyStore.load(keystoreInputStream, password.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
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
                KeyStore keyStore = KeyStore.getInstance(trustmanagerType);
                keyStore.load(keystoreInputStream, password.toCharArray());

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                trustManagers = trustManagerFactory.getTrustManagers();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new IgorException("Could not create TrustManager!", e);
            }
        }

        if (keyManagers != null || trustManagers != null) {
            try {
                SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
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
        initialize("test", new JobExecution());
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
