package com.arassec.igor.plugin.message.connector;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.BaseConnector;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

/**
 * Base connector for E-Mail related messages.
 */
@Getter
@Setter
public abstract class EmailBaseConnector extends BaseConnector {

    /**
     * The host providing the E-Mail service (e.g. IMAP or SMTP).
     */
    @NotEmpty
    @IgorParam(sortIndex = 1)
    protected String host;

    /**
     * The username for authentication.
     */
    @NotEmpty
    @IgorParam(sortIndex = 3)
    protected String username;

    /**
     * The password for authentication.
     */
    @NotEmpty
    @IgorParam(sortIndex = 4, secured = true)
    protected String password;

    /**
     * If checked, TLS is used to secure the communication with the service.
     */
    @IgorParam(sortIndex = 5, advanced = true)
    protected boolean enableTls = true;

    /**
     * If checked, the SSL certificate provided by the service is always accepted.
     */
    @IgorParam(sortIndex = 6, advanced = true)
    protected boolean alwaysTrustSsl;

    /**
     * Timeout <strong>in milliseconds</strong> before a connection attempt is aborted.
     */
    @Positive
    @IgorParam(sortIndex = 7, advanced = true)
    protected int connectionTimeout = 5000;

    /**
     * Timeout <strong>in milliseconds</strong> before reading messages is aborted.
     */
    @Positive
    @IgorParam(sortIndex = 8, advanced = true)
    protected int readTimeout = 15000;

}
