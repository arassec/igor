package com.arassec.igor.module.file.service.http;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.arassec.igor.module.file.service.FileService;
import com.arassec.igor.module.file.service.FileStreamData;
import jdk.jshell.spi.ExecutionControl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link FileService} that uses HTTP as protocol.
 */
@IgorService(label = "HTTP")
public class HttpFileService extends BaseFileService {

    /**
     * The server's hostname.
     */
    @IgorParam
    private String host;

    /**
     * The server's port.
     */
    @IgorParam
    private int port = 80;

    /**
     * Creates a new HTTP-Client.
     *
     * @return A newly created {@link HttpClient}.
     */
    protected HttpClient connect() {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        return client;
    }

    protected String buildUri(String suffix) {
        String result = "https://";
        result += host;
        result += ":" + port;
        if (!suffix.startsWith("/")) {
            result += "/";
        }
        result += suffix;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listFiles(String directory) {
        HttpClient client = connect();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUri(directory)))
                .timeout(Duration.ofMinutes(1))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode());
            }
            Document document = Jsoup.parse(response.body());
            Elements elements = document.select("a");
            List<String> result = new LinkedList<>();
            for (Element element : elements) {
                result.add(element.absUrl("href"));
            }
            return result.stream().filter(file -> !"..".equals(file)).filter(file -> !".".equals(file)).collect(Collectors.toList());
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
        HttpClient client = connect();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUri(file)))
                .timeout(Duration.ofMinutes(1))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String file, String data) {
        throw new ServiceException("Not yet implemented...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        String result = read(file);
        FileStreamData fsd = new FileStreamData();
        fsd.setFileSize(result.getBytes().length);
        fsd.setData(new ByteArrayInputStream(result.getBytes()));
        return fsd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData) {
        throw new ServiceException("Not yet implemented...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file) {
        HttpClient client = connect();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUri(file)))
                .timeout(Duration.ofMinutes(1))
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target) {
        throw new ServiceException("Not yet implemented...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() throws ServiceException {
        throw new ServiceException("Not yet implemented...");
    }

}
