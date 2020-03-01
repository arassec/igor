package com.arassec.igor.module.file.service.http;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileStreamData;
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
 * Base class for {@link HttpFileService} and {@link HttpsFileService} tests.
 */
public abstract class HttpFileServiceBaseTest {

    /**
     * The service under test. Either HTTP-Fileservice or HTTPS-Fileservice.
     */
    protected static BaseHttpFileService service;

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

        List<FileInfo> fileInfos = service.listFiles("/directory/", null);
        assertEquals(3, fileInfos.size());
        assertAll("Filenames in order",
                () -> assertEquals("sample/file.txt", fileInfos.get(0).getFilename()),
                () -> assertEquals("fileA.jpeg", fileInfos.get(1).getFilename()),
                () -> assertEquals("fileB.jpg", fileInfos.get(2).getFilename())
        );

        List<FileInfo> filteredFileInfos = service.listFiles("/directory/", "jpg");
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
                                .withBody("Http(s)FileServiceTests")
                )
        );

        String fileContent = service.read("/sample/file.txt", new WorkInProgressMonitor("http-read-file-test"));
        assertEquals("Http(s)FileServiceTests", fileContent);
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
                                .withBody("Http(s)FileServiceTests")
                )
        );

        FileStreamData fileStreamData = service.readStream("/sample/file.txt", new WorkInProgressMonitor("http-read-file-test"));

        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(fileStreamData.getData(), stringWriter);
        assertEquals("Http(s)FileServiceTests", stringWriter.toString());
        assertEquals(23, fileStreamData.getFileSize());
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
                                .withBody("Http(s)FileServiceTests")
                )
        );

        FileStreamData fileStreamData = service.readStream("/sample/file.txt", new WorkInProgressMonitor("http-read-file-test"));
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

        service.writeStream("/target/file.txt", fileStreamData, new WorkInProgressMonitor("http-write-stream-test"));

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

        service.delete("/target/file.txt", new WorkInProgressMonitor("http-delete-file-test"));

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

        service.move("/sample/file.txt.igor", "/target/file.txt", new WorkInProgressMonitor("http-move-file-test"));

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
        assertDoesNotThrow(() -> service.finalizeStream(null));

        InputStream inputStreamMock = mock(InputStream.class);
        FileStreamData fileStreamData = new FileStreamData();
        fileStreamData.setData(inputStreamMock);
        service.finalizeStream(fileStreamData);
        Mockito.verify(inputStreamMock, times(1)).close();

        doThrow(new IOException("test-exception")).when(inputStreamMock).close();
        assertThrows(IgorException.class, () -> service.finalizeStream(fileStreamData));
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

        service.testConfiguration();
        wireMockServer.verify(getRequestedFor(urlEqualTo("/")));

        wireMockServer.stubFor(
                get("/").willReturn(aResponse().withStatus(404))
        );

        assertThrows(IgorException.class, () -> service.testConfiguration());
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

        String content = service.read("/test.html", new WorkInProgressMonitor("http-follow-redirects-test"));
        assertEquals("Http(s)FollowRedirectsTest", content);

        service.setFollowRedirects(false);

        assertThrows(IgorException.class, () ->
                service.read("/test.html", new WorkInProgressMonitor("http-follow-redirects-test")));
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

        service.setUsername("igor");
        service.setPassword("s3cr3t");

        assertEquals("Http(s)BasicAuthTest", service.read("/test.html", new WorkInProgressMonitor("http-basic-auth-test")));
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

        assertAll("HTTP errors must be handled safely.",
                () -> assertThrows(IgorException.class, () -> service.listFiles("/test.html", null)),
                () -> assertThrows(IgorException.class, () -> service.read("/test.html",
                        new WorkInProgressMonitor("http-error-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.readStream("/test.html",
                        new WorkInProgressMonitor("http-error-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.writeStream("/test.html", mock(FileStreamData.class),
                        new WorkInProgressMonitor("http-error-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.delete("/test.html",
                        new WorkInProgressMonitor("http-error-handling-test")))
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

        assertAll("Exceptions must be handled safely.",
                () -> assertThrows(IgorException.class, () -> service.listFiles("/test.html", null)),
                () -> assertThrows(IgorException.class, () -> service.read("/test.html",
                        new WorkInProgressMonitor("http-exception-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.readStream("/test.html",
                        new WorkInProgressMonitor("http-exception-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.writeStream("/test.html", mock(FileStreamData.class),
                        new WorkInProgressMonitor("http-exception-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.delete("/test.html",
                        new WorkInProgressMonitor("http-exception-handling-test"))),
                () -> assertThrows(IgorException.class, () -> service.testConfiguration())
        );
    }

}
