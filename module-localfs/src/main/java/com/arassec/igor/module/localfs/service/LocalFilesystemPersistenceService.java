package com.arassec.igor.module.localfs.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.persistence.PersistenceService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * TODO: Document class.
 */
@Slf4j
@IgorService(label = "Filesystem")
public class LocalFilesystemPersistenceService extends BaseService implements PersistenceService {

    @IgorParam
    private String targetDir;

    @Override
    public void save(String jobId, String taskName, String value) {
        try {
            Files.write(Paths.get(getFileName(jobId, taskName)), Arrays.asList(value), UTF_8, APPEND, CREATE);
        } catch (IOException e) {
            throw new ServiceException("Could not save value " + value + " to file: " + getFileName(jobId, taskName), e);
        }
    }

    @Override
    public List<String> loadAll(String jobId, String taskName) {
        try {
            if (!Files.exists(Paths.get(getFileName(jobId, taskName)))) {
                Files.createFile(Paths.get(getFileName(jobId, taskName)));
            }
            return Files.readAllLines(Paths.get(getFileName(jobId, taskName)));
        } catch (IOException e) {
            throw new ServiceException("Could not load values from file: " + getFileName(jobId, taskName), e);
        }
    }

    @Override
    public boolean isPersisted(String jobId, String taskName, String value) {
        try (Stream<String> stream = Files.lines(Paths.get(getFileName(jobId, taskName)))) {
            return !stream.noneMatch(line -> value.equals(line));
        } catch (NoSuchFileException e) {
            return false;
        } catch (IOException e) {
            throw new ServiceException("Could not read persisted values: " + getFileName(jobId, taskName), e);
        }
    }

    @Override
    public void testConfiguration() throws ServiceException {
        Path targetPath = Paths.get(targetDir);
        if (!Files.exists(targetPath)) {
            try {
                Files.createDirectory(targetPath);
            } catch (IOException e) {
                throw new ServiceException("Target directory does not exist and could not be created.");
            }
        } else if (!Files.isDirectory(targetPath)) {
            throw new ServiceException("Target is not a directory.");
        }
        if (!Files.isWritable(targetPath)) {
            throw new ServiceException("Target directory is not writable.");
        }
    }

    private String getFileName(String jobId, String taskName) {
        String result = targetDir;
        if (!result.endsWith(File.separator)) {
            result += File.separator;
        }
        return result + clean(jobId) + "_" + clean(taskName) + ".igor.log";
    }

    private String clean(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.replaceAll("/", "_").replaceAll("\\\\", "_").replaceAll("\\s", "_");
    }
}
