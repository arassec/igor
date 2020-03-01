package com.arassec.igor.module.file.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.module.file.service.FileService;
import com.arassec.igor.module.file.service.FileStreamData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CopyFileAction}.
 */
@DisplayName("'Copy file' action tests.")
class CopyFileActionTest extends FileActionBaseTest {

    /**
     * Tests processing the action with JSON-Path parameters.
     */
    @Test
    @DisplayName("Tests the action with JSON-Path parameters.")
    void testProcess() {
        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(new ByteArrayInputStream("test".getBytes()));

        FileService sourceFileServiceMock = mock(FileService.class);
        when(sourceFileServiceMock.readStream(eq("/directory/test/filename.txt"), any(WorkInProgressMonitor.class))).thenReturn(fileStreamData);
        FileService targetFileServiceMock = mock(FileService.class);

        CopyFileAction action = new CopyFileAction();
        action.setSourceService(sourceFileServiceMock);
        action.setSourceDirectory("$.data.directory");
        action.setSourceFilename("$.data.filename");
        action.setTargetService(targetFileServiceMock);
        action.setTargetDirectory("target");
        action.setTargetFilename("copy-file-action-alpha.txt");

        JobExecution jobExecution = new JobExecution();

        List<Map<String, Object>> processedData = action.process(createData(), jobExecution);
        assertEquals(1, processedData.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) processedData.get(0).get(CopyFileAction.KEY_COPY_FILE_ACTION);
        assertAll("Copy file information is added to the data object.",
                () -> assertEquals("/directory/test/", data.get(CopyFileAction.KEY_SOURCE_DIRECTORY)),
                () -> assertEquals("filename.txt", data.get(CopyFileAction.KEY_SOURCE_FILENAME)),
                () -> assertEquals("target/", data.get(CopyFileAction.KEY_TARGET_DIRECTORY)),
                () -> assertEquals("copy-file-action-alpha.txt", data.get(CopyFileAction.KEY_TARGET_FILENAME))
        );

        verify(targetFileServiceMock, times(1)).writeStream(eq("target/copy-file-action-alpha.txt.igor"),
                eq(fileStreamData), any(WorkInProgressMonitor.class));

        verify(sourceFileServiceMock, times(1)).finalizeStream(eq(fileStreamData));

        verify(targetFileServiceMock, times(1)).move(eq("target/copy-file-action-alpha.txt.igor"),
                eq("target/copy-file-action-alpha.txt"), any(WorkInProgressMonitor.class));
    }

    /**
     * Tests processing the action without in-transfer-file-suffix and appending the file type.
     */
    @Test
    @DisplayName("Tests processing the action without in-transfer-file-suffix and appending the file type.")
    void testProcessWithoutInTransferSuffixAndWithFiletypeSuffix() {
        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(new ByteArrayInputStream("test".getBytes()));
        fileStreamData.setFilenameSuffix("jpeg");

        FileService sourceFileServiceMock = mock(FileService.class);
        when(sourceFileServiceMock.readStream(eq("/slashes"), any(WorkInProgressMonitor.class))).thenReturn(fileStreamData);
        FileService targetFileServiceMock = mock(FileService.class);

        CopyFileAction action = new CopyFileAction();
        action.setSourceService(sourceFileServiceMock);
        action.setSourceDirectory(null);
        action.setSourceFilename("file/with/slashes");
        action.setTargetService(targetFileServiceMock);
        action.setTargetDirectory("target");
        action.setTargetFilename("copy-file-action-test");
        action.setAppendTransferSuffix(false);
        action.setAppendFiletypeSuffix(true);

        JobExecution jobExecution = new JobExecution();

        List<Map<String, Object>> processedData = action.process(createData(), jobExecution);
        assertEquals(1, processedData.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) processedData.get(0).get(CopyFileAction.KEY_COPY_FILE_ACTION);
        assertAll("Copy file information is added to the data object.",
                () -> assertEquals("/", data.get(CopyFileAction.KEY_SOURCE_DIRECTORY)),
                () -> assertEquals("file/with/slashes", data.get(CopyFileAction.KEY_SOURCE_FILENAME)),
                () -> assertEquals("target/", data.get(CopyFileAction.KEY_TARGET_DIRECTORY)),
                () -> assertEquals("copy-file-action-test.jpeg", data.get(CopyFileAction.KEY_TARGET_FILENAME))
        );

        verify(targetFileServiceMock, times(1)).writeStream(eq("target/copy-file-action-test.jpeg"),
                eq(fileStreamData), any(WorkInProgressMonitor.class));

        verify(sourceFileServiceMock, times(1)).finalizeStream(eq(fileStreamData));

        verify(targetFileServiceMock, times(0)).move(anyString(), anyString(), any(WorkInProgressMonitor.class));
    }

    /**
     * Tests the action with  unresolved parameters.
     */
    @Test
    @DisplayName("Tests the action with unresolved parameters.")
    void testProcessUnresolvedParameters() {
        FileService sourceFileServiceMock = mock(FileService.class);

        CopyFileAction action = new CopyFileAction();
        action.setSourceService(sourceFileServiceMock);
        action.setSourceDirectory("$.INVALID");
        action.setSourceFilename("$.INVALID");
        action.setTargetDirectory("target");
        action.setTargetFilename("copy-file-action-alpha.txt");

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());

        assertEquals(1, processedData.size());
        assertEquals(data, processedData.get(0));
    }

    /**
     * Tests the action with an invalid file.
     */
    @Test
    @DisplayName("Tests the action with an invalid file.")
    void testProcessInvalidFile() {
        // The mock doesn't return a FileStreamData-Object because the "file" is invalid.
        FileService sourceFileServiceMock = mock(FileService.class);

        CopyFileAction action = new CopyFileAction();
        action.setSourceService(sourceFileServiceMock);
        action.setSourceDirectory("$.data.directory");
        action.setSourceFilename("$.data.filename");
        action.setTargetDirectory("target");
        action.setTargetFilename("copy-file-action-alpha.txt");

        assertThrows(IgorException.class, () -> action.process(createData(), new JobExecution()));
    }

}