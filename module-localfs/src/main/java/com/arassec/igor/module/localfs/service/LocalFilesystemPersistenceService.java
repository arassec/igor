package com.arassec.igor.module.localfs.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.persistence.PersistenceService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Persistence-Service that uses the local filesystem of the igor server.
 */
@Slf4j
@IgorService(label = "Filesystem")
public class LocalFilesystemPersistenceService extends BaseService implements PersistenceService {

    /**
     * The target directory to store persistence files in.
     */
    @IgorParam
    private String targetDir;

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(String jobId, String taskName, String value) {
        try {
            Files.write(Paths.get(getFileName(jobId, taskName)), Arrays.asList(value), UTF_8, APPEND, CREATE);
        } catch (IOException e) {
            throw new ServiceException("Could not save value " + value + " to file: " + getFileName(jobId, taskName), e);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(String jobId, String taskName, int numEntriesToKeep) {
        Path persistenceFile = Paths.get(getFileName(jobId, taskName));
        Path tempFile = Paths.get(getFileName(jobId, taskName) + "_TEMP");
        boolean cleanedUp = false;
        try (Stream<String> streamOne = Files.lines(persistenceFile)) {
            long numLines = streamOne.count();
            if (numLines > numEntriesToKeep) {
                try (Stream<String> streamTwo = Files.lines(persistenceFile)) {
                    Files.deleteIfExists(tempFile);
                    Files.write(tempFile, streamTwo.skip(numLines - numEntriesToKeep).collect(Collectors.toList()));
                    cleanedUp = true;
                }
            }
        } catch (IOException e) {
            log.error("Could not cleanup persistence store!", e);
        }
        if (cleanedUp) {
            try {
                Files.move(tempFile, persistenceFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Could not move cleaned persistence store!", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Creates the persistence file's name based on the supplied data.
     *
     * @param jobId    The job's ID.
     * @param taskName The task's name.
     * @return The filename of the persistence file for this job and task.
     */
    private String getFileName(String jobId, String taskName) {
        String result = targetDir;
        if (!result.endsWith(File.separator)) {
            result += File.separator;
        }
        return result + clean(jobId) + "_" + clean(taskName) + ".igor.log";
    }

    /**
     * Replaces whitespaces in the provided input string with underscores.
     *
     * @param input The input to clean.
     * @return The input with underscores instead of whitespaces.
     */
    private String clean(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.replaceAll("/", "_").replaceAll("\\\\", "_").replaceAll("\\s", "_");
    }
}
