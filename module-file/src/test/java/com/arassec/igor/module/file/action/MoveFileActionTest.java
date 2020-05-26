package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.module.file.connector.FileConnector;
import com.arassec.igor.module.file.provider.ListFilesProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        FileConnector fileConnectorMock = mock(FileConnector.class);

        MoveFileAction moveFileAction = new MoveFileAction();
        moveFileAction.setSource(fileConnectorMock);
        moveFileAction.setSourceDirectory("$." + DataKey.DATA.getKey() + "." + ListFilesProvider.DIRECTORY_KEY);
        moveFileAction.setSourceFilename("$." + DataKey.DATA.getKey() + "." + ListFilesProvider.FILENAME_KEY);
        moveFileAction.setTargetDirectory("/dev/null");
        moveFileAction.setTargetFilename("deleted.txt");

        moveFileAction.process(createData(), new JobExecution());

        verify(fileConnectorMock, times(1)).move(eq("/directory/test/filename.txt"), eq("/dev/null/deleted.txt"),
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

}