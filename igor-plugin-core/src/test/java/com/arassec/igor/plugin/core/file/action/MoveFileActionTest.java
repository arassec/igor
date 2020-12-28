package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link MoveFileAction}.
 */
@DisplayName("'Move file' action tests.")
class MoveFileActionTest extends FileActionBaseTest {

    /**
     * Tests processing the action with mustache template parameters.
     */
    @Test
    @DisplayName("Tests processing the action with mustache template parameters.")
    void testProcess() {
        FileConnector fileConnectorMock = mock(FileConnector.class);

        MoveFileAction moveFileAction = new MoveFileAction();
        moveFileAction.setSource(fileConnectorMock);
        moveFileAction.setSourceDirectory("{{" + DataKey.DATA.getKey() + "." + BaseFileAction.DIRECTORY_KEY + "}}");
        moveFileAction.setSourceFilename("{{" + DataKey.DATA.getKey() + "." + BaseFileAction.FILENAME_KEY + "}}");
        moveFileAction.setTargetDirectory("/dev/null");
        moveFileAction.setTargetFilename("deleted.txt");

        moveFileAction.process(createData(), new JobExecution());

        verify(fileConnectorMock, times(1)).move(eq("/directory/test/filename.txt"), eq("/dev/null/deleted.txt"));
    }

    /**
     * Tests failing safe when parameter values cannot be resolved.
     */
    @Test
    @DisplayName("Tests failing safe when parameter values cannot be resolved.")
    void testPreocessFailSafe() {
        MoveFileAction moveFileAction = new MoveFileAction();
        moveFileAction.setSourceDirectory("/tmp");
        moveFileAction.setSourceFilename("{{INVALID}}");
        moveFileAction.setTargetDirectory("/dev/null");
        moveFileAction.setTargetFilename("{{INVALID}}");

        Map<String, Object> data = createData();
        List<Map<String, Object>> processedData = moveFileAction.process(data, new JobExecution());

        assertEquals(data, processedData.get(0));
    }

}