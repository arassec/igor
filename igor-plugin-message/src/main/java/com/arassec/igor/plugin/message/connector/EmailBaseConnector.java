package com.arassec.igor.plugin.message.connector;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.plugin.core.message.connector.BaseMessageConnector;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

/**
 * Base connector for E-Mail related messages.
 */
@Getter
@Setter
public abstract class EmailBaseConnector extends BaseMessageConnector {

    /**
     * Hostname of the host providing an E-Mail service (e.g. IMAP or SMTP).
     */
    @NotEmpty
    @IgorParam(value = 1)
    protected String host;

    /**
     * Username for authentication.
     */
    @NotEmpty
    @IgorParam(value = 3)
    protected String username;

    /**
     * Password for authentication.
     */
    @NotEmpty
    @IgorParam(value = 4, secured = true)
    protected String password;

    /**
     * Set to {@code true} to enable TLS.
     */
    @IgorParam(value = 5, advanced = true)
    protected boolean enableTls = true;

    /**
     * If {@code true}, always accepts the provided SSL/TLS certificate from the server.
     */
    @IgorParam(value = 6, advanced = true)
    protected boolean alwaysTrustSsl;

    /**
     * Connection timeout in milliseconds.
     */
    @Positive
    @IgorParam(value = 7, advanced = true)
    protected int connectionTimeout = 5000;

    /**
     * Read timeout in milliseconds.
     */
    @Positive
    @IgorParam(value = 8, advanced = true)
    protected int readTimeout = 15000;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected EmailBaseConnector(String typeId) {
        super(typeId);
    }


}
