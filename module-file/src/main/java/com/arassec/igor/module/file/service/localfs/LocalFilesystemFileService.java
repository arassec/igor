package com.arassec.igor.module.file.service.localfs;

import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.file.service.BaseFileService;
import com.arassec.igor.module.file.service.FileInfo;
import com.arassec.igor.module.file.service.FileService;
import com.arassec.igor.module.file.service.FileStreamData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * {@link FileService} to access files in the local file system.
 */
@Slf4j
@IgorService(label = "Filesystem")
public class LocalFilesystemFileService extends BaseFileService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileInfo> listFiles(String directory, JobExecution jobExecution) {
        try {
            return Files.list(Paths.get(directory)).map(path -> {
                FileTime lastModifiedTime = null;
                try {
                    lastModifiedTime = Files.getLastModifiedTime(path);
                } catch (IOException e) {
                    log.warn("Could not get last modified time from path: {},", path);
                }
                return new FileInfo(path.getFileName().toString(), lastModifiedTime != null ?
                        formatInstant(lastModifiedTime.toInstant().truncatedTo(ChronoUnit.SECONDS)) : null);
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new ServiceException("Could not list files in local directory: " + directory, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(String file, JobExecution jobExecution) {
        try {
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            throw new ServiceException("Could not read file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String file, String data, JobExecution jobExecution) {
        try {
            Files.write(Paths.get(file), data.getBytes());
        } catch (IOException e) {
            throw new ServiceException("Could not write file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStreamData readStream(String file, JobExecution jobExecution) {
        try {
            FileStreamData result = new FileStreamData();
            result.setFileSize(Files.size(Paths.get(file)));
            result.setData(new FileInputStream(file));
            return result;
        } catch (IOException e) {
            throw new ServiceException("Could not read file stream: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStream(String file, FileStreamData fileStreamData, JobExecution jobExecution) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            copyStream(fileStreamData.getData(), fileOutputStream, fileStreamData.getFileSize(), jobExecution);
        } catch (IOException e) {
            throw new ServiceException("Could not write file (stream): " + file, e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new ServiceException("Could not close output stream!", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String file, JobExecution jobExecution) {
        try {
            Files.delete(Paths.get(file));
        } catch (IOException e) {
            throw new ServiceException("Could not delete local file " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(String source, String target, JobExecution jobExecution) {
        try {
            Files.move(Paths.get(source), Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ServiceException("Could not move local file '" + source + "' to '" + target + "'!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() throws ServiceException {
        // Nothing to test here...
    }

    /**
     * Opens the supplied file for writing.
     *
     * @param file The file to open.
     * @return A {@link BufferedWriter} to the file.
     */
    public BufferedWriter openFileForWriting(String file) {
        try {
            return Files.newBufferedWriter(Paths.get(file), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ServiceException("Could not open file for writing: " + file, e);
        }
    }

}
