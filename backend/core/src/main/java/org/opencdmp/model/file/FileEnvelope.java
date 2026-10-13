package org.opencdmp.model.file;

import java.io.File;

/**
 * Created by ikalyvas on 3/6/2018.
 */
public class FileEnvelope {
    private String filename;
    private byte[] file;
    private String mimeType;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
