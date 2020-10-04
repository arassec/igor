package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.file.connector.FileConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReadFileAction}.
 */
@DisplayName("'Read file' action tests.")
class ReadFileActionTest extends FileActionBaseTest {

    /**
     * Tests processing the action with mustache template parameters.
     */
    @Test
    @DisplayName("Tests processing the action with mustache template parameters.")
    void testProcess() {
        FileConnector fileConnectorMock = mock(FileConnector.class);
        when(fileConnectorMock.read(eq("/directory/test/filename.txt"))).thenReturn("igor-junit-test");

        ReadFileAction action = new ReadFileAction();
        action.setSource(fileConnectorMock);
        action.setDirectory("{{data.directory}}");
        action.setFilename("{{data.filename}}");

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        assertEquals("igor-junit-test", result.get(0).get(ReadFileAction.KEY_FILE_CONTENTS));
    }

    /**
     * Tests processing the action with unresolved parameters.
     */
    @Test
    @DisplayName("Tests the action with unresolved parameters.")
    void testProcessUnresolvedParameter() {
        ReadFileAction action = new ReadFileAction();
        action.setFilename("{{INVALID}}");

        Map<String, Object> data = createData();
        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));
    }

}
