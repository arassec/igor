package com.arassec.igor.module.misc.service.persistence;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
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
    public void save(Long jobId, String taskId, String value) {
        try {
            Files.write(Paths.get(getFileName(jobId, taskId)), Arrays.asList(value), UTF_8, APPEND, CREATE);
        } catch (IOException e) {
            throw new ServiceException("Could not save value " + value + " to file: " + getFileName(jobId, taskId), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersisted(Long jobId, String taskId, String value) {
        try (Stream<String> stream = Files.lines(Paths.get(getFileName(jobId, taskId)))) {
            return !stream.noneMatch(line -> value.equals(line));
        } catch (NoSuchFileException e) {
            return false;
        } catch (IOException e) {
            throw new ServiceException("Could not read persisted values: " + getFileName(jobId, taskId), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(Long jobId, String taskId, int numEntriesToKeep) {
        Path persistenceFile = Paths.get(getFileName(jobId, taskId));
        Path tempFile = Paths.get(getFileName(jobId, taskId) + "_TEMP");
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
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     * @return The filename of the persistence file for this job and task.
     */
    private String getFileName(Long jobId, String taskId) {
        String result = targetDir;
        if (!result.endsWith(File.separator)) {
            result += File.separator;
        }
        return result + String.valueOf(jobId) + "_" + taskId + ".igor.log";
    }

}
