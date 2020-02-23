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
            return new CommandMock(1);
        } else if ("cd src/test/resources/ssh/ && ls -Alp --time-style=full-iso | grep -v / *.test".equals(commandString)) {
            return new CommandMock(2);
        } else if ("scp -f src/test/resources/ssh/alpha.txt".equals(commandString)) {
            return new CommandMock(3);
        } else if ("scp -f non-existing-file".equals(commandString)) {
            return new CommandMock(4);
        } else if ("scp -t target/ssh-write-stream-test.txt".equals(commandString)) {
            return new CommandMock(5);
        } else if ("rm -f target/ssh-delete-test.txt".equals(commandString)) {
            return new CommandMock(6);
        } else if ("mv target/ssh-move-test.txt target/ssh-move-test.txt.moved".equals(commandString)) {
            return new CommandMock(7);
        }
        throw new IllegalStateException("un-mocked SSH command: " + commandString);
    }

}
