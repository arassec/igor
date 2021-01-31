package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DeleteFileAction}.
 */
@DisplayName("'Delete file' action tests.")
class DeleteFileActionTest extends FileActionBaseTest {

    /**
     * Tests processing the action with mustache template parameters.
     */
    @Test
    @DisplayName("Tests the action with mustache template parameters.")
    void process() {
        FileConnector fileConnectorMock = mock(FileConnector.class);

        DeleteFileAction action = new DeleteFileAction();
        action.setSource(fileConnectorMock);
        action.setDirectory("{{data.directory}}");
        action.setFilename("{{data.filename}}");

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));

        verify(fileConnectorMock, times(1)).delete("/directory/test/filename.txt");
    }

    /**
     * Tests processing the action with unresolved parameters.
     */
    @Test
    @DisplayName("Tests the action with unresolved parameters.")
    void testProcessUnresolvedParameter() {
        DeleteFileAction action = new DeleteFileAction();
        action.setDirectory("/tmp");
        action.setFilename(null);

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));
    }

}