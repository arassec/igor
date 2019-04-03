package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base for file services.
 */
public abstract class BaseFileService extends BaseService implements FileService {

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        // Nothing to do here by default...
    }

    /**
     * Copies the content of the input stream into the output stream.
     *
     * @param in
     *         The input stream.
     * @param out
     *         The output stream.
     * @param fileSize
     *         The max number of bytes to copy.
     */
    protected void copyStream(InputStream in, OutputStream out, long fileSize) {
        try {
            byte[] buf = new byte[1024 * 1024];
            int foo;
            while (true) {
                if (buf.length < fileSize) {
                    foo = buf.length;
                } else {
                    foo = (int) fileSize;
                }
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    break; // error
                }
                out.write(buf, 0, foo);
                fileSize -= foo;
                if (fileSize == 0L) {
                    break;
                }
            }
            out.flush();
        } catch (IOException e) {
            throw new ServiceException("Could not copy data via streams!", e);
        }
    }

}
