package org.opencdmp.model.tenantconfiguration;

import org.opencdmp.model.StorageFile;

public class LogoTenantConfiguration {
	private StorageFile storageFile;
	public static final String _storageFile = "storageFile";

	public StorageFile getStorageFile() {
		return storageFile;
	}

	public void setStorageFile(StorageFile storageFile) {
		this.storageFile = storageFile;
	}
}
