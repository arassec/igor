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
     * The host of the remote server.
     */
    @NotBlank
    @IgorParam
    private String host;

    /**
     * The port of the remote server.
     */
    @Positive
    @IgorParam
    private int port = 25;

    /**
     * The username to login with.
     */
    @NotBlank
    @IgorParam
    private String username;

    /**
     * The password used for authentication.
     */
    @NotBlank
    @IgorParam(secured = true)
    private String password;

    /**
     * Enables or disables strict host-key checking.
     */
    @IgorParam(advanced = true)
    private boolean strictHostkeyChecking = false;

    /**
     * Preferred authentications.
     */
    @IgorParam(advanced = true)
    private String preferredAuthentications = "publickey,keyboard-interactive,password";

    /**
     * Connection timeout in milliseconds.
     */
    @Positive
    @IgorParam(advanced = true)
    private int timeout = 30000;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseSshFileConnector(String typeId) {
        super(typeId);
    }

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
