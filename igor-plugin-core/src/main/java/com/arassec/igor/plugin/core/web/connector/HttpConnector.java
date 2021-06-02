package com.arassec.igor.plugin.core.web.connector;

import com.arassec.igor.core.model.connector.Connector;

import java.net.http.HttpClient;

/**
 * Interface for HTTP related connectors.
 */
public interface HttpConnector extends Connector {

    /**
     * Returns the configured HTTP client for web requests.
     *
     * @return A configured {@link HttpClient}.
     */
    HttpClient getHttpClient();

}
