package org.opencdmp.service.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lock")
public class LockProperties {

	private Integer lockInterval;

	public Integer getLockInterval() {
		return lockInterval;
	}

	public void setLockInterval(Integer lockInterval) {
		this.lockInterval = lockInterval;
	}
}
