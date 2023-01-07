package com.arassec.igor.plugin.core.web.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CoreDataKey;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.CoreType;
import com.arassec.igor.plugin.core.file.connector.FallbackFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileConnector;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * <h2>'HTTP File Download' Action</h2>
 *
 * <h3>Description</h3>
 * This action Downloads a file from an HTTP(S) server and stores it in the target's filesystem.<br>
 * <p>
 * The action adds data about the downloaded file to the data item under the 'downloadedFile' key.<br>
 * <p>
 * A data item processed by this action could look like this:
 * <pre><code>
 * {
 *   "data": {},
 *   "meta": {
 *     "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90",
 *     "simulation": true,
 *     "timestamp": 1601302794228
 *   },
 *   "downloadedFile": {
 *     "targetFilename": "downloaded-file.txt",
 *     "targetDirectory": "/tmp"
 *   }
 * }
 * </code></pre>
 */
@Slf4j
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.WEB, typeId = CoreType.HTTP_FILE_DOWNLOAD_ACTION)
public class HttpFileDownloadAction extends BaseHttpAction {

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
     * The file connector for the target filesystem.
     */
    @NotNull
    @IgorParam(sortIndex = 4)
    private FileConnector target;

    /**
     * The directory to store the downloaded file in.
     */
    @NotBlank
    @IgorParam(sortIndex = 5)
    private String targetDirectory;

    /**
     * The filename of the downloaded file.
     */
    @NotBlank
    @IgorParam(sortIndex = 6)
    private String targetFilename;

    /**
     * The HTTP headers to use for the request. Headers must be entered as 'key: value'-pairs, with each header in a separate line.
     */
    @IgorParam(sortIndex = 7, advanced = true, subtype = ParameterSubtype.MULTI_LINE)
    private String headers;

    /**
     * If checked, the filename on the target connector will be appended with '.igor' during transfer. This is useful to indicate that the file is currently downloaded.
     */
    @IgorParam(sortIndex = 8, advanced = true)
    private boolean appendTransferSuffix = true;

    /**
     * If checked, igor will try to determine the file type and append it to the target filename (e.g. '.html' or '.jpeg').
     */
    @IgorParam(sortIndex = 9, advanced = true)
    private boolean appendFiletypeSuffix;

    /**
     * The name of the key the action's results will be stored in.
     */
    @NotBlank
    @IgorParam(sortIndex = Integer.MAX_VALUE - 1, advanced = true)
    private String targetKey = DEFAULT_KEY_WEB_RESPONSE;

    /**
     * Creates a new component instance.
     */
    public HttpFileDownloadAction() {
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

        var requestUrl = CorePluginUtils.evaluateTemplate(data, url);
        var destination = CorePluginUtils.evaluateTemplate(data, targetDirectory);
        var filename = CorePluginUtils.evaluateTemplate(data, targetFilename);
        var resolvedTargetKey = CorePluginUtils.evaluateTemplate(data, targetKey);

        var httpRequestBuilder = HttpRequest.newBuilder().GET().uri(URI.create(requestUrl));
        parsedHeaders.forEach(header -> httpRequestBuilder.header(CorePluginUtils.evaluateTemplate(data, header.split(":")[0]),
            CorePluginUtils.evaluateTemplate(data, header.split(":")[1])));
        addBasicAuthHeaderIfConfigured(httpRequestBuilder);

        try {
            var workInProgressMonitor = new WorkInProgressMonitor(filename, 0);
            jobExecution.addWorkInProgress(workInProgressMonitor);

            HttpResponse<InputStream> httpResponse = httpConnector.getHttpClient().send(httpRequestBuilder.build(),
                HttpResponse.BodyHandlers.ofInputStream());

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() > 226) {
                throw new IgorException("Received HTTP error code on download file request for url '" + requestUrl + "': "
                    + httpResponse.statusCode());
            }

            var fileStreamData = new FileStreamData();
            fileStreamData.setFilenameSuffix(determineFilenameSuffix(httpResponse));
            setFilesizeAndData(httpResponse, fileStreamData, httpRequestBuilder, requestUrl);

            String targetFileWithSuffix = CorePluginUtils.appendSuffixIfRequired(filename,
                fileStreamData.getFilenameSuffix(), appendFiletypeSuffix);

            String targetFileWithPath = CorePluginUtils.combineFilePath(destination, targetFileWithSuffix);

            log.debug("Downloading file '{}' to '{}'", requestUrl, targetFileWithPath);

            String targetFileInTransfer = targetFileWithPath;
            if (appendTransferSuffix) {
                targetFileInTransfer += CorePluginUtils.FILE_IN_TRANSFER_SUFFIX;
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

                var fileContent = httpStringResponse.body();

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
