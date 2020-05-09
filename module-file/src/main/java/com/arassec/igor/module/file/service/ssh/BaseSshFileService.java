package com.arassec.igor.module.file.service.ssh;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * Base for SSH based File-Services (like SFTP and SCP).
 */
@Getter
@Setter
public abstract class BaseSshFileService extends BaseFileService {

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
    private int port = 22;

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
     * Connection timeout.
     */
    @IgorParam(advanced = true)
    private int timeout = 30000;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseSshFileService(String typeId) {
        super(typeId);
    }

    /**
     * Initializes the SSH session.
     *
     * @return A new SSH session.
     */
    Session connect(String host, int port, String username, String password) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", strictHostkeyChecking ? "yes" : "no");
            session.setConfig("PreferredAuthentications", preferredAuthentications);
            session.connect(timeout);
            return session;
        } catch (JSchException e) {
            throw new IgorException("Could not connect to server " + host + ":" + port, e);
        }
    }

}
