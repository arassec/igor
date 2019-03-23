package com.arassec.igor.module.file.service.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import lombok.Data;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Stores connection data to an SSH server.
 */
@Data
public class SshConnectionData {

    /**
     * The SSH-Session.
     */
    private Session session;

    /**
     * The SSH-Channel.
     */
    private Channel channel;

    /**
     * The SSH-OutputStream to send SSH commands to the remote server.
     */
    private OutputStream sshOutputStream;

    /**
     * The SSH-InputStream to receive data from the remote server.
     */
    private InputStream sshInputStream;

}
