package org.opencdmp.commons.types.tenantconfiguration;

import java.util.UUID;

public class LogoTenantConfigurationEntity {
	private UUID storageFileId;

	public UUID getStorageFileId() {
		return storageFileId;
	}

	public void setStorageFileId(UUID storageFileId) {
		this.storageFileId = storageFileId;
	}
}
