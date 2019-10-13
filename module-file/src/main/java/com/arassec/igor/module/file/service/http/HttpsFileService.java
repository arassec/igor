package com.arassec.igor.module.file.service.http;


import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component
@Scope("prototype")
@ConditionalOnClass(HttpClient.class)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "7244c422-84f5-4ead-a3a9-73df49b1910a";
    }

}
