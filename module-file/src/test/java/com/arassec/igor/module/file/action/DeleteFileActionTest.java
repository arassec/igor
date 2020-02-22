package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.module.file.service.FileService;
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
        FileService fileServiceMock = mock(FileService.class);

        DeleteFileAction action = new DeleteFileAction();
        action.setService(fileServiceMock);
        action.setDirectory("$.data.directory");
        action.setFilename("$.data.filename");

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));

        verify(fileServiceMock, times(1))
                .delete(eq("/directory/test/filename.txt"), any(WorkInProgressMonitor.class));
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