package com.arassec.igor.plugin.core.web.connector;

import java.net.http.HttpClient;

/**
 * Interface for HTTP related connectors.
 */
public interface HttpConnector {

    /**
     * Returns the configured HTTP client for web requests.
     *
     * @return A configured {@link HttpClient}.
     */
    HttpClient getHttpClient();

}
