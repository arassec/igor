package com.arassec.igor.module.file.provider;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.provider.BaseProvider;
import com.arassec.igor.core.model.provider.IgorData;
import com.arassec.igor.module.file.service.FileService;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides files from a specified directory.
 */
@IgorProvider(type = "com.arassec.igor.provider.file.ListFilesProvider")
public class ListFilesProvider extends BaseProvider {

    /**
     * Defines the directory to list files in.
     */
    @IgorParam
    private String directory;

    /**
     * The service to use for file listing.
     */
    @IgorParam
    private FileService sourceService;

    /**
     * The key the files are listed under.
     */
    @IgorParam
    private String dataKey = "data";

    /**
     * The files in the configured directory.
     */
    private List<String> files = new LinkedList<>();

    /**
     * The index into the listed files, pointing to the next file to serve.
     */
    private int currentFile = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        files.clear();
        files.addAll(sourceService.listFiles(directory));
        currentFile = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        if (currentFile < files.size()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IgorData next() {
        IgorData igorData = new IgorData();
        igorData.put(dataKey, files.get(currentFile));
        currentFile++;
        return igorData;
    }

}
