package com.arassec.igor.module.file.service.http;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileService;
import com.arassec.igor.module.file.service.FileStreamData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
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
    protected String host;

    /**
     * The server's port.
     */
    @IgorParam
    protected int port = 80;

    /**
     * Enables or disables following redirects.
     */
    @IgorParam
    protected boolean followRedirects = true;

    /**
     * A username for authentication.
     */
    @IgorParam(optional = true)
    protected String username;

    /**
     * The password for authentication.
     */
    @IgorParam(optional = true)
    protected String password;

    /**
     * A timeout for the HTTP operations.
     */
    @IgorParam(optional = true)
    protected long timeout = 3600;

    /**
     * The protocol to use.
     */
    protected String protocol = "http";

    /**
     * Creates a new HTTP-Client.
     *
     * @return A newly created {@link HttpClient}.
     */
    protected HttpClient connect() {
        HttpClient.Redirect redirectPolicy = HttpClient.Redirect.ALWAYS;
        if (!followRedirects) {
            redirectPolicy = HttpClient.Redirect.NEVER;
        }

        HttpClient client = HttpClient.newBuilder().followRedirects(redirectPolicy).build();

        return client;
    }

    /**
     * Creates the URI to the service.
     *
     * @param suffix The suffix to append after host and port.
     * @return The base URI to the service.
     */
    private String buildUri(String suffix) {
        String result = protocol + "://";
        result += host;
        result += ":" + port;
        if (!suffix.startsWith("/")) {
            result += "/";
        }
        result += suffix;
        return result;
    }

    /**
     * Creates the HTTP-Request.
     *
     * @param uriPart The variable part of the URI.
     * @return The {@link HttpRequest}.
     */
    private HttpRequest.Builder getRequestBuilder(String uriPart) {
        HttpRequest.Builder httpRequestBuilder =
                HttpRequest.newBuilder().uri(URI.create(buildUri(uriPart))).timeout(Duration.ofSeconds(timeout));
        String authorizationHeader = createBasicAuthHeader();
        if (!StringUtils.isEmpty(authorizationHeader)) {
            httpRequestBuilder.headers(authorizationHeader);
        }
        return httpRequestBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding, JobExecution jobExecution) {
        HttpClient client = connect();
        HttpRequest request = getRequestBuilder(directory).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode());
            }
            Document document = Jsoup.parse(response.body());
            Elements elements = document.select("a");
            List<String> result = new LinkedList<>();
            for (Element element : elements) {
                Attributes attributes = element.attributes();
                String href = attributes.get("href");
                if (href != null && !StringUtils.isEmpty(href)) {
                    if (href.contains("#")) {
                        href = href.substring(0, href.indexOf("#"));
                    }
                    if (href.contains("?")) {
                        href = href.substring(0, href.indexOf("?"));
                    }
                    if (StringUtils.isEmpty(fileEnding)) {
                        result.add(href);
                    } else if (href.endsWith(fileEnding)) {
                        result.add(href);
                    }
                }
            }

            // @formatter:off
            return result.stream().filter(Objects::nonNull)
                    .filter(file -> !StringUtils.isEmpty(file))
                    .filter(file -> !"..".equals(file))
                    .filter(file -> !".".equals(file))
                    .filter(file -> !directory.startsWith(file))
                    .filter(file -> !directory.equals(file))
                    .map(file -> new FileInfo(file, null))
                    .collect(Collectors.toList());
            // @formatter:on

        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage() + " (" + request.uri().toString() + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file, JobExecution jobExecution) {
        HttpClient client = connect();
        HttpRequest request = getRequestBuilder(file).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode() + " (" + request.uri().toString() + ")");
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage() + " (" + request.uri().toString() + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String file, String data, JobExecution jobExecution) {
        HttpRequest request = getRequestBuilder(file).PUT(HttpRequest.BodyPublishers.ofString(data)).build();
        sendRequest(connect(), request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file, JobExecution jobExecution) {
        HttpClient client = connect();
        HttpRequest request = getRequestBuilder(file).GET().build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode() + " (" + request.uri().toString() + ")");
            }

            String filenameSuffix = null;
            Optional<String> optionalContentType = response.headers().firstValue("Content-Type");
            if (optionalContentType.isPresent()) {
                String[] contentTypeParts = optionalContentType.get().split(" ");
                if (contentTypeParts.length > 0) {
                    String[] mimeTypeParts = contentTypeParts[0].split("/");
                    if (mimeTypeParts.length > 1) {
                        filenameSuffix = mimeTypeParts[1].substring(0, mimeTypeParts[1].length() - 1);
                    }
                }
            }

            OptionalLong optionalContentLength = response.headers().firstValueAsLong("content-length");
            if (optionalContentLength.isPresent()) {
                FileStreamData fsd = new FileStreamData();
                fsd.setFileSize(optionalContentLength.getAsLong());
                fsd.setData(response.body());
                fsd.setFilenameSuffix(filenameSuffix);
                return fsd;
            } else {
                // Fallback if no content-length header is available!
                response.body().close();
                String fileContent = read(file, jobExecution);
                FileStreamData fsd = new FileStreamData();
                fsd.setFileSize(fileContent.getBytes().length);
                fsd.setData(new ByteArrayInputStream(fileContent.getBytes()));
                fsd.setFilenameSuffix(filenameSuffix);
                return fsd;
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage() + " (" + request.uri().toString() + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, JobExecution jobExecution) {
        HttpRequest request = getRequestBuilder(file).PUT(HttpRequest.BodyPublishers.ofInputStream(fileStreamData::getData))
                .build();
        sendRequest(connect(), request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file, JobExecution jobExecution) {
        sendRequest(connect(), getRequestBuilder(file).DELETE().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target, JobExecution jobExecution) {
        FileStreamData fileStreamData = readStream(source, jobExecution);
        writeStream(target, fileStreamData, jobExecution);
        finalizeStream(fileStreamData);
        delete(source, jobExecution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        if (fileStreamData != null && fileStreamData.getData() != null) {
            try {
                fileStreamData.getData().close();
            } catch (IOException e) {
                throw new ServiceException("Could not finalize HTTP stream!", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() throws ServiceException {
        HttpClient client = connect();
        HttpRequest request = getRequestBuilder("").GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("GET failed for URL with HTTP status: " + response.statusCode() + " (" + request.uri().toASCIIString() + ")");
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("GET failed for URL: " + request.uri().toASCIIString(), e);
        }
    }

    /**
     * Creates an authorization header with the configured username and password.
     *
     * @return The header or {@code null}, if username and password are not configured.
     */
    private String createBasicAuthHeader() {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            return "Authorization: Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        }
        return null;
    }

    /**
     * Sends an HTTP request and handles errors by throwing a {@link ServiceException}.
     *
     * @param client  The HTTP-Client.
     * @param request The request to send.
     */
    private void sendRequest(HttpClient client, HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceException("HTTP request failed with status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("HTTP request failed: " + e.getMessage() + " (" + request.uri().toString() + ")");
        }
    }
}
