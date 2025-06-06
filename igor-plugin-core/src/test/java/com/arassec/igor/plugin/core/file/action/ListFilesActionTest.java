package com.arassec.igor.plugin.core.file.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ListFilesAction}.
 */
@DisplayName("'List files' action tests.")
class ListFilesActionTest {

    /**
     * Tests processing the action with mustache template parameters.
     */
    @Test
    @DisplayName("Tests the action with mustache template parameters.")
    void testProcess() {
        FileConnector fileConnectorMock = mock(FileConnector.class);
        when(fileConnectorMock.listFiles("/dir", "jpg")).thenReturn(
                List.of(new FileInfo("fileA.jpg", "12345"), new FileInfo("fileB.jpg", "67890"))
        );

        ListFilesAction listFilesAction = new ListFilesAction();
        listFilesAction.setSource(fileConnectorMock);
        listFilesAction.setDirectory("/dir");
        listFilesAction.setFileEnding("jpg");

        List<Map<String, Object>> result = listFilesAction.process(new HashMap<>(), new JobExecution());

        assertEquals(2, result.size());

        DocumentContext json = JsonPath.parse(result.getFirst());
        assertEquals("fileA.jpg", json.read("$."+ DataKey.DATA.getKey() + "." + ListFilesAction.FILENAME_KEY));
        assertEquals("12345", json.read("$."+ DataKey.DATA.getKey() + "." + ListFilesAction.LAST_MODIFIED_KEY));
        assertEquals("/dir/", json.read("$."+ DataKey.DATA.getKey() + "." + ListFilesAction.DIRECTORY_KEY));

        json = JsonPath.parse(result.get(1));
        assertEquals("fileB.jpg", json.read("$."+ DataKey.DATA.getKey() + "." + ListFilesAction.FILENAME_KEY));
        assertEquals("67890", json.read("$."+ DataKey.DATA.getKey() + "." + ListFilesAction.LAST_MODIFIED_KEY));
        assertEquals("/dir/", json.read("$."+ DataKey.DATA.getKey() + "." + ListFilesAction.DIRECTORY_KEY));
    }

}
