package com.arassec.igor.plugin.core.file.connector.localfs;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.file.connector.BaseFileConnector;
import com.arassec.igor.plugin.core.file.connector.FileInfo;
import com.arassec.igor.plugin.core.file.connector.FileStreamData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File-Connector to access files in the local file system.
 */
@Slf4j
@IgorComponent
public class LocalFilesystemFileConnector extends BaseFileConnector {

    /**
     * Creates a new component instance.
     */
    public LocalFilesystemFileConnector() {
        super("localfs-file-connector");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, String fileEnding) {
        try {
            final PathMatcher matcher;
            if (!StringUtils.hasLength(fileEnding)) {
                matcher = FileSystems.getDefault().getPathMatcher("glob:**/*");
            } else {
                matcher = FileSystems.getDefault().getPathMatcher("glob:**/*." + fileEnding);
            }
            try (Stream<Path> files = Files.list(Paths.get(directory))) {
                return files.filter(matcher::matches).filter(path -> !Files.isDirectory(path)).map(path -> {
                    FileTime lastModifiedTime = null;
                    try {
                        lastModifiedTime = Files.getLastModifiedTime(path);
                    } catch (IOException e) {
                        log.warn("Could not get last modified time from path: {},", path);
                    }
                    return new FileInfo(path.getFileName().toString(), lastModifiedTime != null ? formatInstant(
                            lastModifiedTime.toInstant().truncatedTo(ChronoUnit.SECONDS)) : null);
                }).collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new IgorException("Could not list files in local directory: " + directory, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            throw new IgorException("Could not read file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file) {
        try {
            FileStreamData result = new FileStreamData();
            result.setFileSize(Files.size(Paths.get(file)));
            result.setData(new FileInputStream(file));
            return result;
        } catch (IOException e) {
            throw new IgorException("Could not read file stream: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, WorkInProgressMonitor workInProgressMonitor,
                            JobExecution jobExecution) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            copyStream(fileStreamData.getData(), fileOutputStream, fileStreamData.getFileSize(), workInProgressMonitor, jobExecution);
        } catch (IOException e) {
            throw new IgorException("Could not write file (stream): " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file) {
        try {
            Files.delete(Paths.get(file));
        } catch (IOException e) {
            throw new IgorException("Could not delete local file " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target) {
        try {
            Files.move(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IgorException("Could not move local file '" + source + "' to '" + target + "'!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        // Nothing to test here...
    }

}
