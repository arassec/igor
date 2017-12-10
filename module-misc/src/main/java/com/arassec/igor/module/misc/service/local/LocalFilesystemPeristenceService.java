package com.arassec.igor.module.misc.service.local;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.module.misc.service.PersistenceService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * TODO: Document class.
 */
@IgorService(type = "com.arassec.igor.service.misc.LocalFilesystemPeristenceService")
public class LocalFilesystemPeristenceService extends BaseService implements PersistenceService {

    @IgorParam
    private String targetFile;

    @Override
    public void save(String value) {
        try {
            Files.write(Paths.get(targetFile), Arrays.asList(value.toString()), UTF_8, APPEND, CREATE);
        } catch (IOException e) {
            throw new ServiceException("Could not save value " + value + " to file: " + targetFile, e);
        }
    }

    @Override
    public List<String> loadAll() {
        try {
            if (!Files.exists(Paths.get(targetFile))) {
                Files.createFile(Paths.get(targetFile));
            }
            return Files.readAllLines(Paths.get(targetFile));
        } catch (IOException e) {
            throw new ServiceException("Could not load values from file: " + targetFile, e);
        }
    }
}
