package org.opencdmp.commons.types.pluginconfiguration.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.types.storagefile.importexport.StorageFileImportExport;

@XmlAccessorType(XmlAccessType.FIELD)
public class PluginConfigurationFieldImportExport {

    @XmlAttribute(name="code")
    private String code;

    @XmlElement(name="storageFile")
    private StorageFileImportExport storageFile;

    @XmlAttribute(name="textValue")
    private String textValue;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public StorageFileImportExport getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFileImportExport storageFile) {
        this.storageFile = storageFile;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }
}
