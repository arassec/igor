package com.arassec.igor.module.file.service.http;


import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.module.file.service.FileService;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * {@link FileService} that uses HTTPS as protocol.
 */
@Slf4j
@IgorComponent("HTTPS")
public class HttpsFileService extends HttpFileService {

    /**
     * Enables or disables SSL certificate verification.
     */
    @IgorParam(optional = true)
    private boolean certificateVerification = true;

    /**
     * Creates a new Instance.
     */
    public HttpsFileService() {
        this.port = 443;
        this.protocol = "https";
    }

    /**
     * Creates a new HTTP-Client.
     *
     * @return A newly created {@link HttpClient}.
     */
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
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, null);
                httpClientBuilder.sslContext(sslContext);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                log.warn("Disabling SSL certificate verification failed!", e);
            }
        }

        return httpClientBuilder.build();
    }

}
