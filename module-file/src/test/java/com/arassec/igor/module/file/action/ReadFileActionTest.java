package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.module.file.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReadFileAction}.
 */
@DisplayName("'Read file' action tests.")
class ReadFileActionTest extends BaseFileActionTest {

    /**
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters.")
    public void testProcess() {
        FileService fileServiceMock = mock(FileService.class);
        when(fileServiceMock.read(eq("/directory/test/filename.txt"), any(WorkInProgressMonitor.class))).thenReturn("igor-junit-test");

        ReadFileAction action = new ReadFileAction();
        action.setService(fileServiceMock);
        action.setDirectory("$.data.directory");
        action.setFilename("$.data.filename");

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        assertEquals("igor-junit-test", result.get(0).get(ReadFileAction.KEY_FILE_CONTENTS));
    }

}
