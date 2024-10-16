package org.opencdmp.commons.types.notification;

public class Attachment {

    private String fileRef, fileName, mimeType;

    public Attachment(String fileRef, String fileName, String mimeType) {
        this.fileRef = fileRef;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    public String getFileRef() {
        return fileRef;
    }

    public void setFileRef(String fileRef) {
        this.fileRef = fileRef;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
