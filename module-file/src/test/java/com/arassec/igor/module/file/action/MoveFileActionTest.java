package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.module.file.provider.ListFilesProvider;
import com.arassec.igor.module.file.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link MoveFileAction}.
 */
@DisplayName("'Move file' action tests.")
class MoveFileActionTest extends FileActionBaseTest {

    /**
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters.")
    void testProcess() {
        FileService fileServiceMock = mock(FileService.class);

        MoveFileAction moveFileAction = new MoveFileAction();
        moveFileAction.setService(fileServiceMock);
        moveFileAction.setSourceDirectory("$." + DataKey.DATA.getKey() + "." + ListFilesProvider.DIRECTORY_KEY);
        moveFileAction.setSourceFilename("$." + DataKey.DATA.getKey() + "." + ListFilesProvider.FILENAME_KEY);
        moveFileAction.setTargetDirectory("/dev/null");
        moveFileAction.setTargetFilename("deleted.txt");

        moveFileAction.process(createData(), new JobExecution());

        verify(fileServiceMock, times(1)).move(eq("/directory/test/filename.txt"), eq("/dev/null/deleted.txt"),
                any(WorkInProgressMonitor.class));
    }

    /**
     * Tests failing safe when parameter values cannot be resolved.
     */
    @Test
    @DisplayName("Tests failing safe when parameter values cannot be resolved.")
    void testPreocessFailSafe() {
        MoveFileAction moveFileAction = new MoveFileAction();
        moveFileAction.setSourceDirectory("/tmp");
        moveFileAction.setSourceFilename("$.INVALID");
        moveFileAction.setTargetDirectory("/dev/null");
        moveFileAction.setTargetFilename("$.INVALID");

        Map<String, Object> data = createData();
        List<Map<String, Object>> processedData = moveFileAction.process(data, new JobExecution());

        assertEquals(data, processedData.get(0));
    }

    /**
     * Tests default parameter values.
     */
    @Test
    @DisplayName("Tests default parameter values.")
    void testDefaults() {
        MoveFileAction action = new MoveFileAction();
        assertEquals("$.data.directory", action.getSourceDirectory());
        assertEquals("$.data.filename", action.getSourceFilename());
    }

}