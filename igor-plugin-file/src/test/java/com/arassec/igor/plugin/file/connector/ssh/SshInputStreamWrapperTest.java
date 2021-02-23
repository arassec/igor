package com.arassec.igor.plugin.file.connector.ssh;

import com.arassec.igor.core.util.IgorException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link SshInputStreamWrapper}.
 */
class SshInputStreamWrapperTest {

    /**
     * Tests reading a certain amount of bytes.
     */
    @Test
    @DisplayName("Tests reading a certain amount of bytes.")
    @SneakyThrows
    void testReadBytes() {
        InputStream inputStreamMock = mock(InputStream.class);
        SshInputStreamWrapper wrapper = new SshInputStreamWrapper(inputStreamMock, 2);

        byte[] buffer = new byte[16];

        when(inputStreamMock.read(buffer, 0, 1)).thenReturn(1);

        // First byte is read:
        assertEquals(1, wrapper.read(buffer, 0, 1));

        // Second byte is read:
        assertEquals(1, wrapper.read(buffer, 0, 1));

        // All is read:
        assertEquals(-1, wrapper.read(buffer, 0, 1));

        // Read more than 'filesize':
        wrapper = new SshInputStreamWrapper(inputStreamMock, 2);

        when(inputStreamMock.read(buffer, 0, 2)).thenReturn(2);

        assertEquals(2, wrapper.read(buffer, 0, 10));
        assertEquals(-1, wrapper.read(buffer, 0, 1));
    }

    /**
     * Tests error handling.
     */
    @Test
    @DisplayName("Tests error handling.")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    void testErrorHandling() {
        InputStream inputStreamMock = mock(InputStream.class);
        SshInputStreamWrapper wrapper = new SshInputStreamWrapper(inputStreamMock, 2);
        byte[] buffer = new byte[16];

        when(inputStreamMock.read(eq(buffer), eq(0), anyInt())).thenReturn(-1);

        assertThrows(IgorException.class, () -> wrapper.read(buffer, 0, 1));
        assertThrows(IgorException.class, () -> wrapper.read(buffer, 0, 2));
        assertThrows(IgorException.class, () -> wrapper.read(buffer, 0, 3));
    }

    /**
     * Tests reading from the input stream.
     */
    @Test
    @DisplayName("Tests reading from the input stream.")
    @SneakyThrows
    void testRead() {
        InputStream inputStreamMock = mock(InputStream.class);
        SshInputStreamWrapper wrapper = new SshInputStreamWrapper(inputStreamMock, 666);

        when(inputStreamMock.read()).thenReturn(123);

        assertEquals(123, wrapper.read());
    }

}
