package com.arassec.igor.plugin.file.connector.ssh;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.BaseFileConnector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * Base for SSH based file-connectors (like SFTP and SCP).
 */
@Getter
@Setter
public abstract class BaseSshFileConnector extends BaseFileConnector {

    /**
     * The host running the SSH server.
     */
    @NotBlank
    @IgorParam
    private String host;

    /**
     * The port, the SSH server is listening on.
     */
    @Positive
    @IgorParam
    private int port = 25;

    /**
     * A username for authentication/authorization.
     */
    @NotBlank
    @IgorParam
    private String username;

    /**
     * A password for authentication/authorization.
     */
    @NotBlank
    @IgorParam(secured = true)
    private String password;

    /**
     * If checked, the host key of the server will be verified. If unchecked, the host key is ignored.
     */
    @IgorParam(advanced = true)
    private boolean strictHostkeyChecking = false;

    /**
     * List of preferred SSH authentication methods for this connector.
     */
    @IgorParam(advanced = true)
    private String preferredAuthentications = "publickey,keyboard-interactive,password";

    /**
     * A timeout <strong>in milliseconds</strong> after which requests to the SSH server will be aborted.
     */
    @Positive
    @IgorParam(advanced = true)
    private int timeout = 30000;

    /**
     * Initializes the SSH session.
     *
     * @return A new SSH session.
     */
    Session connect(String host, int port, String username, String password) {
        try {
            var jsch = new JSch();
            var session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", strictHostkeyChecking ? "yes" : "no");
            if (StringUtils.hasText(preferredAuthentications)) {
                session.setConfig("PreferredAuthentications", preferredAuthentications);
            }
            session.connect(timeout);
            return session;
        } catch (JSchException e) {
            throw new IgorException("Could not connect to server " + host + ":" + port, e);
        }
    }

}
