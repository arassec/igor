package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreDataKey;
import com.arassec.igor.plugin.core.CoreUtils;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Action to download a file and store it using a {@link FileConnector}.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class HttpFileDownloadAction extends BaseWebAction {

    /**
     * Key for the web request action's data.
     */
    public static final String DEFAULT_KEY_WEB_RESPONSE = "downloadedFile";

    /**
     * "Content-Type" HTTP Header name.
     */
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * "Content-Length" HTTP Header name.
     */
    public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";

    /**
     * The destination for the copied file.
     */
    @NotNull
    @IgorParam(4)
    private FileConnector target;

    /**
     * The target directory to copy/move the file to.
     */
    @NotBlank
    @IgorParam(5)
    private String targetDirectory;

    /**
     * The target file name.
     */
    @NotBlank
    @IgorParam(6)
    private String targetFilename;

    /**
     * The request's headers.
     */
    @IgorParam(value = 7, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * Enables a ".igor" file suffix during file transfer. The suffix will be removed after the file has been downloaded
     * completely.
     */
    @IgorParam(value = 8, advanced = true, defaultValue = "true")
    private boolean appendTransferSuffix;

    /**
     * If set to {@code true}, igor appends a filetype suffix if avaliable (e.g. '.html' or '.jpeg').
     */
    @IgorParam(value = 9, advanced = true)
    private boolean appendFiletypeSuffix;

    /**
     * The target key to put the web response in the data item.
     */
    @NotBlank
    @IgorParam(value = Integer.MAX_VALUE - 1, advanced = true, defaultValue = DEFAULT_KEY_WEB_RESPONSE)
    private String targetKey;

    /**
     * Creates a new component instance.
     */
    public HttpFileDownloadAction() {
        super("http-file-download-action");
        target = new FallbackFileConnector();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        parseHeaders(headers);
        targetKey = CoreDataKey.DOWNLOADED_FILE.getKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String requestUrl = getString(data, url);
        String destination = getString(data, targetDirectory);
        String filename = getString(data, targetFilename);
        String resolvedTargetKey = getString(data, targetKey);

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder().GET().uri(URI.create(requestUrl));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(getString(data, header.split(":")[0]),
                getString(data, header.split(":")[1])));
        addBasicAuthHeaderIfConfigured(httpRequestBuilder);

        try {
            WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor(filename, 0);
            jobExecution.addWorkInProgress(workInProgressMonitor);

            HttpResponse<InputStream> httpResponse = httpConnector.getHttpClient().send(httpRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() > 226) {
                throw new IgorException("Received HTTP error code on download file request for url '" + requestUrl + "': "
                        + httpResponse.statusCode());
            }

            FileStreamData fileStreamData = new FileStreamData();
            fileStreamData.setFilenameSuffix(determineFilenameSuffix(httpResponse));
            setFilesizeAndData(httpResponse, fileStreamData, httpRequestBuilder, requestUrl);

            String targetFileWithSuffix = CoreUtils.appendSuffixIfRequired(filename,
                    fileStreamData.getFilenameSuffix(), appendFiletypeSuffix);

            String targetFileWithPath = CoreUtils.combineFilePath(destination, targetFileWithSuffix);

            log.debug("Downloading file '{}' to '{}'", requestUrl, targetFileWithPath);

            String targetFileInTransfer = targetFileWithPath;
            if (appendTransferSuffix) {
                targetFileInTransfer += CoreUtils.FILE_IN_TRANSFER_SUFFIX;
            }

            target.writeStream(targetFileInTransfer, fileStreamData, workInProgressMonitor, jobExecution);

            fileStreamData.getData().close();

            if (appendTransferSuffix) {
                target.move(targetFileInTransfer, targetFileWithPath);
            }

            log.debug("File '{}' downloaded to '{}'", requestUrl, targetFileWithPath);

            Map<String, Object> actionData = new HashMap<>();
            actionData.put(CoreDataKey.TARGET_FILENAME.getKey(), targetFileWithSuffix);
            actionData.put(CoreDataKey.TARGET_DIRECTORY.getKey(), destination);
            data.put(resolvedTargetKey, actionData);

        } catch (IOException e) {
            throw new IgorException("Could not request URL: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IgorException("Interrupted during HTTP request!", e);
        }

        return List.of(data);
    }

    /**
     * Determines the filetype suffix if possible.
     *
     * @param httpResponse The HTTP response containing headers.
     *
     * @return The filetype suffix or {code null}, if none could be determined.
     */
    private String determineFilenameSuffix(HttpResponse<InputStream> httpResponse) {
        Optional<String> optionalContentType = httpResponse.headers().firstValue(HTTP_HEADER_CONTENT_TYPE);
        if (optionalContentType.isPresent()) {
            String[] contentTypeParts = optionalContentType.get().split(";");
            if (contentTypeParts.length > 0) {
                String[] mimeTypeParts = contentTypeParts[0].split("/");
                if (mimeTypeParts.length > 1) {
                    return mimeTypeParts[1];
                }
            }
        }
        return null;
    }

    /**
     * Sets the filesize and data to the supplied FileStreamData instance.
     *
     * @param httpResponse       The previous HTTP response containing the filesize (hopefully).
     * @param fileStreamData     The target to set the file's size in.
     * @param httpRequestBuilder The HTTP request builder in case, the file has to be downloaded completely.
     * @param requestUrl         The original request URL pointing to the file to download.
     */
    private void setFilesizeAndData(HttpResponse<InputStream> httpResponse, FileStreamData fileStreamData,
                                    HttpRequest.Builder httpRequestBuilder, String requestUrl) {
        OptionalLong optionalContentLength = httpResponse.headers().firstValueAsLong(HTTP_HEADER_CONTENT_LENGTH);
        if (optionalContentLength.isPresent()) {
            fileStreamData.setFileSize(optionalContentLength.getAsLong());
            fileStreamData.setData(httpResponse.body());
        } else {
            try {
                // Fallback if no content-length header is available! We read the whole file into memory...
                httpResponse.body().close();

                HttpResponse<String> httpStringResponse = httpConnector.getHttpClient().send(httpRequestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString());

                if (httpStringResponse.statusCode() < 200 || httpStringResponse.statusCode() > 226) {
                    throw new IgorException("Received HTTP error code on download file (string) request for url '"
                            + requestUrl + "': " + httpResponse.statusCode());
                }

                String fileContent = httpStringResponse.body();

                fileStreamData.setFileSize(fileContent.getBytes().length);
                fileStreamData.setData(new ByteArrayInputStream(fileContent.getBytes()));
            } catch (IOException e) {
                throw new IgorException("Could not determine filesize!", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IgorException("Could not determine filesize due to interruption!", e);
            }
        }
    }

}
