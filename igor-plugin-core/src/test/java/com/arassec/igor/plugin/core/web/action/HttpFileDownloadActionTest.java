package com.arassec.igor.plugin.core.web.action;


import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import com.arassec.igor.plugin.core.CoreDataKey;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import com.arassec.igor.plugin.core.web.connector.FallbackHttpConnector;
import com.arassec.igor.plugin.core.web.connector.StandardHttpConnector;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link HttpRequestAction}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("'HTTP File Download' action tests.")
class HttpFileDownloadActionTest extends CoreActionBaseTest {

    /**
     * Mock for the action's {@link StandardHttpConnector}.
     */
    @Mock
    private StandardHttpConnector httpConnector;

    /**
     * Mock for the action's "target" property.
     */
    @Mock
    private FileConnector fileConnector;

    /**
     * Tests default values of a new instance.
     */
    @Test
    @DisplayName("Tests default values of a new instance.")
    void testDefaults() {
        HttpFileDownloadAction action = new HttpFileDownloadAction();
        assertInstanceOf(FallbackHttpConnector.class, action.getHttpConnector());
        assertInstanceOf(FallbackFileConnector.class, action.getTarget());
    }

    /**
     * Tests initializing the action.
     */
    @Test
    @DisplayName("Tests initializing the action.")
    void testInitialize() {
        HttpFileDownloadAction action = new HttpFileDownloadAction();
        action.setHeaders("a: b\nc:d\nalpha\r\ne:f");

        action.initialize(new JobExecution());

        assertEquals(3, action.getParsedHeaders().size());
        assertEquals("a: b", action.getParsedHeaders().getFirst());
        assertEquals("c:d", action.getParsedHeaders().get(1));
        assertEquals("e:f", action.getParsedHeaders().get(2));
    }

    /**
     * Tests downloading a file without a content-length HTTP header in the server's response.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @DisplayName("Tests downloading a file without a content-length HTTP header in the server's response.")
    void testDownloadFileNoContentLengthHeader() {
        // Prepare the data item:
        Map<String, Object> data = createData();
        data.put("request-url", "http://request.url");
        data.put("target-dir", "/target/dir");
        data.put("target-filename", "filename");
        data.put("target-key", "actionResult");

        // Configure the action under test:
        HttpFileDownloadAction action = new HttpFileDownloadAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("{{request-url}}");
        action.setTarget(fileConnector);
        action.setTargetDirectory("{{target-dir}}");
        action.setTargetFilename("{{target-filename}}");
        action.setTargetKey("{{target-key}}");

        // Prepare mocks:
        HttpHeaders httpHeaders = HttpHeaders.of(Map.of("key", List.of()), (header, unused) -> true);

        InputStream inputStreamMock = mock(InputStream.class);

        // First response containing an input stream:
        HttpResponse<InputStream> inputStreamHttpResponse = mock(HttpResponse.class);
        when(inputStreamHttpResponse.statusCode()).thenReturn(200);
        when(inputStreamHttpResponse.headers()).thenReturn(httpHeaders);
        when(inputStreamHttpResponse.body()).thenReturn(inputStreamMock);

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(inputStreamHttpResponse);

        // If no Content-Length header is found, the request is repeated to download the wohle file as string:
        HttpResponse<String> stringHttpResponse = mock(HttpResponse.class);
        when(stringHttpResponse.statusCode()).thenReturn(200);
        when(stringHttpResponse.body()).thenReturn("response-body");

        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(stringHttpResponse);

        // Use the mocked HTTP-Client:
        when(httpConnector.getHttpClient()).thenReturn(httpClient);

        List<Map<String, Object>> result = action.process(data, new JobExecution());

        assertEquals(1, result.size());
        assertEquals("filename", getString(result.getFirst(), "$.actionResult." + CoreDataKey.TARGET_FILENAME.getKey()));
        assertEquals("/target/dir", getString(result.getFirst(), "$.actionResult." + CoreDataKey.TARGET_DIRECTORY.getKey()));

        // The first response's input stream must have been closed!
        verify(inputStreamMock, times(1)).close();
    }

    /**
     * Tests downloading a file with a content-length HTTP header in the server's response.
     */
    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @DisplayName("Tests downloading a file with a content-length HTTP header in the server's response.")
    void testDownloadFileWithContentLengthHeader() {
        // Configure the action under test:
        HttpFileDownloadAction action = new HttpFileDownloadAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("http://test.url");
        action.setTarget(fileConnector);
        action.setTargetDirectory("/target");
        action.setTargetFilename("file");
        action.setTargetKey("actionResult");

        // Prepare mocks:
        HttpHeaders httpHeaders = HttpHeaders.of(Map.of(HttpFileDownloadAction.HTTP_HEADER_CONTENT_LENGTH, List.of("12")),
                (header, unused) -> true);

        InputStream inputStreamMock = mock(InputStream.class);

        // First response containing an input stream:
        HttpResponse<InputStream> inputStreamHttpResponse = mock(HttpResponse.class);
        when(inputStreamHttpResponse.statusCode()).thenReturn(200);
        when(inputStreamHttpResponse.headers()).thenReturn(httpHeaders);
        when(inputStreamHttpResponse.body()).thenReturn(inputStreamMock);

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(inputStreamHttpResponse);

        // Use the mocked HTTP-Client:
        when(httpConnector.getHttpClient()).thenReturn(httpClient);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        assertEquals("file", getString(result.getFirst(), "$.actionResult." + CoreDataKey.TARGET_FILENAME.getKey()));
        assertEquals("/target", getString(result.getFirst(), "$.actionResult." + CoreDataKey.TARGET_DIRECTORY.getKey()));

        // The response's input stream must have been closed!
        verify(inputStreamMock, times(1)).close();
    }

    /**
     * Tests determining the file type appending a suffix to the filename.
     */
    @Test
    @SneakyThrows
    @DisplayName("Tests determining the file type appending a suffix to the filename.")
    void testDetermineAndAppendFilenameSuffix() {
        // Configure the action under test:
        HttpFileDownloadAction action = new HttpFileDownloadAction();
        action.setHttpConnector(httpConnector);
        action.setUrl("http://test.url");
        action.setTarget(fileConnector);
        action.setTargetDirectory("/target");
        action.setTargetFilename("file");
        action.setTargetKey("actionResult");
        action.setAppendFiletypeSuffix(true);

        // Prepare mocks:
        HttpHeaders httpHeaders = HttpHeaders.of(Map.of(
                HttpFileDownloadAction.HTTP_HEADER_CONTENT_LENGTH, List.of("12"),
                HttpFileDownloadAction.HTTP_HEADER_CONTENT_TYPE, List.of("application/json")),
                (header, unused) -> true);

        InputStream inputStreamMock = mock(InputStream.class);

        // First response containing an input stream:
        @SuppressWarnings("unchecked")
        HttpResponse<InputStream> inputStreamHttpResponse = mock(HttpResponse.class);
        when(inputStreamHttpResponse.statusCode()).thenReturn(200);
        when(inputStreamHttpResponse.headers()).thenReturn(httpHeaders);
        when(inputStreamHttpResponse.body()).thenReturn(inputStreamMock);

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofInputStream()))).thenReturn(inputStreamHttpResponse);

        // Use the mocked HTTP-Client:
        when(httpConnector.getHttpClient()).thenReturn(httpClient);

        List<Map<String, Object>> result = action.process(createData(), new JobExecution());

        assertEquals(1, result.size());
        assertEquals("file.json", getString(result.getFirst(), "$.actionResult." + CoreDataKey.TARGET_FILENAME.getKey()));
        assertEquals("/target", getString(result.getFirst(), "$.actionResult." + CoreDataKey.TARGET_DIRECTORY.getKey()));

        // The first response's input stream must have been closed!
        verify(inputStreamMock, times(1)).close();
    }

}
