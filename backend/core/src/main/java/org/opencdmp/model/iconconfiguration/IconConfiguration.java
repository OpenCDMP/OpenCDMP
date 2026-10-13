package org.opencdmp.model.iconconfiguration;

import org.opencdmp.model.StorageFile;

public class IconConfiguration {

    public final static String _matIconName = "matIconName";
    private String matIconName;

    public final static String _storageFile = "storageFile";
    private StorageFile storageFile;

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public StorageFile getStorageFile() {
        return storageFile;
    }

    public void setStorageFile(StorageFile storageFile) {
        this.storageFile = storageFile;
    }
}
