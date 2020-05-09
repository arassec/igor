package com.arassec.igor.module.file.service.http;


import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * File-Service that uses HTTPS as protocol.
 */
@Slf4j
@IgorComponent
public class HttpsFileService extends BaseHttpFileService {

    /**
     * Enables or disables SSL certificate verification.
     */
    @Getter
    @Setter
    @IgorParam(advanced = true, value = 2)
    private boolean certificateVerification = true;

    /**
     * Creates a new Instance.
     */
    public HttpsFileService() {
        super("https-file-connector");
        this.port = 443;
        this.protocol = "https";
    }

    /**
     * Creates a new HTTP-Client.
     *
     * @return A newly created {@link HttpClient}.
     */
    @Override
    protected HttpClient connect() {

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();

        if (followRedirects) {
            httpClientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        } else {
            httpClientBuilder.followRedirects(HttpClient.Redirect.NEVER);
        }

        if (!certificateVerification) {
            TrustManager[] trustAllCerts = new TrustManager[]{
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

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, null);
                httpClientBuilder.sslContext(sslContext);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                log.warn("Disabling SSL certificate verification failed!", e);
            }
        }

        return httpClientBuilder.build();
    }

}
