package org.opencdmp.service.kpi;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kpi.task")
public class KpiTaskProperties {
	private boolean enable;
	private boolean bootstrapEnable;
	private long bootstrapDelaySeconds;

	public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean getBootstrapEnable() {
		return bootstrapEnable;
	}

	public void setBootstrapEnable(boolean bootstrapEnable) {
		this.bootstrapEnable = bootstrapEnable;
	}

	public long getBootstrapDelaySeconds() {
		return bootstrapDelaySeconds;
	}

	public void setBootstrapDelaySeconds(long bootstrapDelaySeconds) {
		this.bootstrapDelaySeconds = bootstrapDelaySeconds;
	}
}

