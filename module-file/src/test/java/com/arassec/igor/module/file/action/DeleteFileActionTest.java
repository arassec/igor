package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.connector.FileConnector;
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
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters.")
    void process() {
        FileConnector fileConnectorMock = mock(FileConnector.class);

        DeleteFileAction action = new DeleteFileAction();
        action.setSource(fileConnectorMock);
        action.setDirectory("$.data.directory");
        action.setFilename("$.data.filename");

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));

        verify(fileConnectorMock, times(1)).delete(eq("/directory/test/filename.txt"));
    }

    /**
     * Tests processing the action with unresolved parameters.
     */
    @Test
    @DisplayName("Tests the action with unresolved parameters.")
    void testProcessUnresolvedParameter() {
        DeleteFileAction action = new DeleteFileAction();
        action.setDirectory("$.INVALID");
        action.setFilename("$.INVALID");

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));
    }

}