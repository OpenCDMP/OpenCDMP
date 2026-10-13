package org.opencdmp.commons.types.iconconfiguration.importexport;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.types.storagefile.importexport.StorageFileImportExport;

@XmlAccessorType(XmlAccessType.FIELD)
public class IconConfigurationImportExport {

    @XmlAttribute(name = "matIconName")
    private String matIconName;

    @XmlElement(name="storageFile")
    private StorageFileImportExport storageFile;

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public StorageFileImportExport getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFileImportExport storageFile) {
        this.storageFile = storageFile;
    }
}
