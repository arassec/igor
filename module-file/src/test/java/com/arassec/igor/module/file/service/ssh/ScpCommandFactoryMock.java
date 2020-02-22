package com.arassec.igor.module.file.service.ssh;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;

/**
 * Mock implementation of a {@link CommandFactory} for the Apache Mina SSHD. Used to test the {@link ScpFileService}.
 */
public class ScpCommandFactoryMock implements CommandFactory {

    /**
     * Creates commands based on the input received via SCP.
     *
     * @param channelSession The session's channel.
     * @param commandString  The SCP command as String.
     *
     * @return A newly created {@link Command} instance.
     */
    @Override
    public Command createCommand(ChannelSession channelSession, String commandString) {
        if ("cd src/test/resources/ssh/ && ls -Alp --time-style=full-iso | grep -v /".equals(commandString)) {
            return new ListFilesCommandMock(1);
        } else if ("cd src/test/resources/ssh/ && ls -Alp --time-style=full-iso | grep -v / *.test".equals(commandString)) {
            return new ListFilesCommandMock(2);
        }
        return null;
    }

}
