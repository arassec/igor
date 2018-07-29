package com.arassec.igor.module.ssh.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Andreas Sensen on 01.05.2017.
 */
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

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public OutputStream getSshOutputStream() {
        return sshOutputStream;
    }

    public void setSshOutputStream(OutputStream sshOutputStream) {
        this.sshOutputStream = sshOutputStream;
    }

    public InputStream getSshInputStream() {
        return sshInputStream;
    }

    public void setSshInputStream(InputStream sshInputStream) {
        this.sshInputStream = sshInputStream;
    }
}
