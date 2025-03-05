package org.opencdmp.commons.types.storagefile.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class StorageFileImportExport {

    @XmlAttribute(name="fileName")
    private String fileName;

    @XmlAttribute(name="fileValue")
    private String fileValue;

    @XmlAttribute(name="extension")
    private String extension;

    @XmlAttribute(name="mimeType")
    private String mimeType;

    public String getFileValue() {
        return fileValue;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileValue(String fileValue) {
        this.fileValue = fileValue;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}