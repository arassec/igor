package com.arassec.igor.module.file.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.connector.FallbackFileConnector;
import com.arassec.igor.module.file.connector.FileConnector;
import com.arassec.igor.module.file.connector.FileInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ListFilesProvider}.
 */
@DisplayName("'List files' provider tests.")
class ListFilesProviderTest {

    /**
     * Tests default values on instance creation.
     */
    @Test
    @DisplayName("Tests default values on instance creation.")
    void testCreation() {
        ListFilesProvider provider = new ListFilesProvider();
        assertEquals("file-providers", provider.getCategoryId());
        assertEquals("list-files-provider", provider.getTypeId());
        assertTrue(provider.getSource() instanceof FallbackFileConnector);
    }

    /**
     * Tests the provider.
     */
    @Test
    @DisplayName("Tests the provider.")
    void testProvider() {
        FileConnector fileConnectorMock = mock(FileConnector.class);
        when(fileConnectorMock.listFiles(eq("/tmp"), eq("jpg"))).thenReturn(List.of(
                new FileInfo("/tmp/fileA", "now"), new FileInfo("fileB", null)));

        ListFilesProvider provider = new ListFilesProvider();
        provider.setSource(fileConnectorMock);
        provider.setDirectory("/tmp");
        provider.setFileEnding("jpg");

        provider.initialize("job-id", new JobExecution());

        assertEquals("/tmp/", provider.getDirectory());

        assertTrue(provider.hasNext());

        Map<String, Object> data = provider.next();
        assertEquals("/tmp/", data.get(ListFilesProvider.DIRECTORY_KEY));
        assertEquals("fileA", data.get(ListFilesProvider.FILENAME_KEY));
        assertEquals("now", data.get(ListFilesProvider.LAST_MODIFIED_KEY));

        data = provider.next();
        assertEquals("/tmp/", data.get(ListFilesProvider.DIRECTORY_KEY));
        assertEquals("fileB", data.get(ListFilesProvider.FILENAME_KEY));
        assertNull(data.get(ListFilesProvider.LAST_MODIFIED_KEY));

        assertFalse(provider.hasNext());
    }

}