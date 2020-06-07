package com.arassec.igor.module.file.connector.http;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.module.file.connector.FileInfo;
import com.arassec.igor.module.file.connector.FileStreamData;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Base class for {@link HttpFileConnector} and {@link HttpsFileConnector} tests.
 */
abstract class HttpFileConnectorBaseTest {

    /**
     * The connector under test. Either {@link HttpFileConnector} or {@link HttpsFileConnector}.
     */
    protected static BaseHttpFileConnector connector;

    /**
     * A wiremock server for testing.
     */
    protected static WireMockServer wireMockServer;

    /**
     * Cleans up after tests.
     */
    @AfterEach
    public void cleanup() {
        wireMockServer.resetAll();
    }

    /**
     * Shuts the test environment down.
     */
    @AfterAll
    public static void teardown() {
        wireMockServer.stop();
    }

    /**
     * Tests listing of files.
     */
    @Test
    @DisplayName("Tests listing files.")
    @SneakyThrows(IOException.class)
    void testListFiles() {
        wireMockServer.stubFor(
                get("/directory/").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody(Files.readString(Paths.get("src/test/resources/http/index.html")))
                )
        );

        List<FileInfo> fileInfos = connector.listFiles("/directory/", null);
        assertEquals(3, fileInfos.size());
        assertAll("Filenames in order",
                () -> assertEquals("sample/file.txt", fileInfos.get(0).getFilename()),
                () -> assertEquals("fileA.jpeg", fileInfos.get(1).getFilename()),
                () -> assertEquals("fileB.jpg", fileInfos.get(2).getFilename())
        );

        List<FileInfo> filteredFileInfos = connector.listFiles("/directory/", "jpg");
        assertEquals(1, filteredFileInfos.size());
        assertEquals("fileB.jpg", fileInfos.get(2).getFilename());
    }

    /**
     * Tests reading a file.
     */
    @Test
    @DisplayName("Tests reading a file.")
    void testRead() {
        wireMockServer.stubFor(
                get("/sample/file.txt").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody("Http(s)FileConnectorTests")
                )
        );

        String fileContent = connector.read("/sample/file.txt");
        assertEquals("Http(s)FileConnectorTests", fileContent);
    }

    /**
     * Tests reading a file as stream.
     */
    @Test
    @DisplayName("Tests reading a file as stream.")
    @SneakyThrows(IOException.class)
    void testReadStream() {
        wireMockServer.stubFor(
                get("/sample/file.txt").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody("Http(s)FileConnectorTests")
                )
        );

        FileStreamData fileStreamData = connector.readStream("/sample/file.txt");

        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(fileStreamData.getData(), stringWriter);
        assertEquals("Http(s)FileConnectorTests", stringWriter.toString());
        assertEquals(25, fileStreamData.getFileSize());
    }

    /**
     * Tests reading a file as stream with headers in response.
     */
    @Test
    @DisplayName("Tests reading a file as stream with headers in response.")
    void testReadStreamWithHeaders() {
        wireMockServer.stubFor(
                get("/sample/file.txt").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "text/html; charset=UTF-8")
                                .withHeader("content-length", "11")
                                .withBody("Http(s)FileConnectorTests")
                )
        );

        FileStreamData fileStreamData = connector.readStream("/sample/file.txt");
        assertEquals("html", fileStreamData.getFilenameSuffix());
        assertEquals(11, fileStreamData.getFileSize());
    }

    /**
     * Tests writing a file as stream.
     */
    @Test
    @DisplayName("Tests writing a file as stream.")
    void testWriteStream() {
        wireMockServer.stubFor(put("/target/file.txt").willReturn(aResponse().withStatus(200)));

        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(new ByteArrayInputStream("Http(s)WriteStreamTest".getBytes()));

        connector.writeStream("/target/file.txt", fileStreamData, new WorkInProgressMonitor(),
                JobExecution.builder().executionState(JobExecutionState.RUNNING).build());

        wireMockServer.verify(putRequestedFor(urlEqualTo("/target/file.txt"))
                .withRequestBody(equalTo("Http(s)WriteStreamTest")));
    }


    /**
     * Tests deleting a file.
     */
    @Test
    @DisplayName("Tests deleting a file.")
    void testDelete() {
        wireMockServer.stubFor(delete("/target/file.txt").willReturn(aResponse().withStatus(200)));

        connector.delete("/target/file.txt");

        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/target/file.txt")));
    }

    /**
     * Tests moving a file.
     */
    @Test
    @DisplayName("Tests moving a file.")
    void testMove() {
        wireMockServer.stubFor(
                get("/sample/file.txt.igor").willReturn(
                        aResponse()
                                .withStatus(200)
                                .withBody("Http(s)MoveTest")
                )
        );
        wireMockServer.stubFor(put("/target/file.txt").willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(delete("/sample/file.txt.igor").willReturn(aResponse().withStatus(200)));

        connector.move("/sample/file.txt.igor", "/target/file.txt");

        wireMockServer.verify(putRequestedFor(urlEqualTo("/target/file.txt"))
                .withRequestBody(equalTo("Http(s)MoveTest")));
        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/sample/file.txt.igor")));
    }

    /**
     * Tests finalizing a stream to release allocated resources.
     */
    @Test
    @DisplayName("Tests finalizing a stream to release allocated resources.")
    @SneakyThrows(IOException.class)
    void testFinalizeStream() {
        assertDoesNotThrow(() -> connector.finalizeStream(null));

        InputStream inputStreamMock = mock(InputStream.class);
        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(inputStreamMock);
        connector.finalizeStream(fileStreamData);
        Mockito.verify(inputStreamMock, times(1)).close();

        doThrow(new IOException("test-exception")).when(inputStreamMock).close();
        assertThrows(IgorException.class, () -> connector.finalizeStream(fileStreamData));
    }

    /**
     * Tests testing the configuration.
     */
    @Test
    @DisplayName("Tests testing the configuration.")
    void testTestConfiguration() {
        wireMockServer.stubFor(
                get("/").willReturn(aResponse().withStatus(200))
        );

        connector.testConfiguration();
        wireMockServer.verify(getRequestedFor(urlEqualTo("/")));

        wireMockServer.stubFor(
                get("/").willReturn(aResponse().withStatus(404))
        );

        assertThrows(IgorException.class, () -> connector.testConfiguration());
    }

    /**
     * Tests following redirects.
     */
    @Test
    @DisplayName("Tests following redirects.")
    void testFollowRedirects() {
        wireMockServer.stubFor(
                get("/test.html").willReturn(aResponse().withStatus(302).withHeader("Location", "/redirect/followed.html"))
        );
        wireMockServer.stubFor(
                get("/redirect/followed.html").willReturn(aResponse().withStatus(200).withBody("Http(s)FollowRedirectsTest"))
        );

        connector.setFollowRedirects(true);

        String content = connector.read("/test.html");
        assertEquals("Http(s)FollowRedirectsTest", content);

        connector.setFollowRedirects(false);

        assertThrows(IgorException.class, () -> connector.read("/test.html"));
    }

    /**
     * Tests basic authentication.
     */
    @Test
    @DisplayName("Tests basic authentication.")
    void testBasicAuth() {
        wireMockServer.stubFor(
                get("/test.html").withBasicAuth("igor", "s3cr3t").willReturn(aResponse().withStatus(200).withBody(
                        "Http(s)BasicAuthTest"))
        );

        connector.setUsername("igor");
        connector.setPassword("s3cr3t");

        assertEquals("Http(s)BasicAuthTest", connector.read("/test.html"));
    }

    /**
     * Tests error response handling.
     */
    @Test
    @DisplayName("Tests error response handling.")
    void testErrorResponseHandling() {
        wireMockServer.stubFor(
                get("/test.html").willReturn(aResponse().withStatus(500))
        );
        wireMockServer.stubFor(
                put("/test.html").willReturn(aResponse().withStatus(500))
        );
        wireMockServer.stubFor(
                delete("/test.html").willReturn(aResponse().withStatus(500))
        );
        wireMockServer.stubFor(
                get("/").willReturn(aResponse().withStatus(500))
        );

        FileStreamData fileStreamDataMock = mock(FileStreamData.class);
        WorkInProgressMonitor wipMon = new WorkInProgressMonitor();
        JobExecution jobExecution = JobExecution.builder().executionState(JobExecutionState.RUNNING).build();

        assertAll("HTTP errors must be handled safely.",
                () -> assertThrows(IgorException.class, () -> connector.listFiles("/test.html", null)),
                () -> assertThrows(IgorException.class, () -> connector.read("/test.html")),
                () -> assertThrows(IgorException.class, () -> connector.readStream("/test.html")),
                () -> assertThrows(IgorException.class, () -> connector.writeStream("/test.html", fileStreamDataMock, wipMon,
                        jobExecution)),
                () -> assertThrows(IgorException.class, () -> connector.delete("/test.html"))
        );
    }

    /**
     * Tests exception handling.
     */
    @Test
    @DisplayName("Tests exception handling.")
    void testExceptionHandling() {
        wireMockServer.stubFor(
                get("/test.html").willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        );
        wireMockServer.stubFor(
                put("/test.html").willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        );
        wireMockServer.stubFor(
                delete("/test.html").willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        );
        wireMockServer.stubFor(
                get("/").willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        );

        FileStreamData fileStreamDataMock = mock(FileStreamData.class);
        WorkInProgressMonitor wipMon = new WorkInProgressMonitor();
        JobExecution jobExecution = JobExecution.builder().executionState(JobExecutionState.RUNNING).build();

        assertAll("Exceptions must be handled safely.",
                () -> assertThrows(IgorException.class, () -> connector.listFiles("/test.html", null)),
                () -> assertThrows(IgorException.class, () -> connector.read("/test.html")),
                () -> assertThrows(IgorException.class, () -> connector.readStream("/test.html")),
                () -> assertThrows(IgorException.class, () -> connector.writeStream("/test.html", fileStreamDataMock, wipMon,
                        jobExecution)),
                () -> assertThrows(IgorException.class, () -> connector.delete("/test.html")),
                () -> assertThrows(IgorException.class, () -> connector.testConfiguration())
        );
    }

}
