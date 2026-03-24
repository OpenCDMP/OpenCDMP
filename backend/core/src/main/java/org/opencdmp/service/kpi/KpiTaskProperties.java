package org.opencdmp.service.kpi;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kpi.task")
public class KpiTaskProperties {
	private boolean enable;

	public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}

