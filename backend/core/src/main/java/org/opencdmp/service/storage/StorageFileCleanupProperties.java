package org.opencdmp.service.storage;


import org.opencdmp.commons.enums.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "storage.task")
public class StorageFileCleanupProperties {
	private boolean enable;
	private int intervalSeconds;

	public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getIntervalSeconds() {
		return intervalSeconds;
	}

	public void setIntervalSeconds(int intervalSeconds) {
		this.intervalSeconds = intervalSeconds;
	}
}

