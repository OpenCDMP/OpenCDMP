package org.opencdmp.service.kpi;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kpi.task")
public class KpiTaskProperties {
	private boolean enable;
	private int intervalSeconds;
	private int startTimeSeconds;

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

	public int getStartTimeSeconds() {
		return startTimeSeconds;
	}

	public void setStartTimeSeconds(int startTimeSeconds) {
		this.startTimeSeconds = startTimeSeconds;
	}
}

