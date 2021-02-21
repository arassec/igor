package com.arassec.igor.plugin.core.web.connector;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;

import java.net.http.HttpClient;

/**
 * Fallback {@link StandardHttpConnector} to support {@link javax.validation.constraints.NotNull} annotations on connector params.
 */
public class FallbackHttpConnector extends BaseHttpConnector {

    /**
     * Error message for exceptions that are thrown during method invocations.
     */
    private static final String ERROR_MESSAGE = "Configure a real HTTP connector!";

    /**
     * Creates a new instance.
     */
    public FallbackHttpConnector() {
        super(CorePluginType.FALLBACK_HTTP_CONNECTOR.getId());
    }

    /**
     * Always throws an exception to prevent usage in real jobs.
     *
     * @param jobExecution Ignore job execution.
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Always throws an exception to prevent usage in real jobs.
     */
    @Override
    public void testConfiguration() {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Always throws an exception to prevent usage in real jobs.
     */
    @Override
    public HttpClient getHttpClient() {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

}
