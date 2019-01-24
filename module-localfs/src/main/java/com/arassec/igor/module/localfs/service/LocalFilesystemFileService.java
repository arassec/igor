package com.arassec.igor.module.localfs.service;

import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.file.BaseFileService;
import com.arassec.igor.core.model.service.file.FileStreamData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * {@link com.arassec.igor.core.model.service.file.FileService} to access files in the local file system.
 */
@IgorService(label = "Filesystem")
public class LocalFilesystemFileService extends BaseFileService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listFiles(String directory) {
        try {
            return Files.list(Paths.get(directory)).map(path -> path.toString()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new ServiceException("Could not list files in local directory: " + directory, e);
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
            throw new ServiceException("Could not read file: " + file, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(String file, String data) {
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
    public FileStreamData readStream(String file) {
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
    public void writeStream(String file, FileStreamData fileStreamData) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            copyStream(fileStreamData.getData(), fileOutputStream, fileStreamData.getFileSize());
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
    public void delete(String file) {
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
    public void move(String source, String target) {
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
