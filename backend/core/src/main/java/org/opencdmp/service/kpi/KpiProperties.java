package org.opencdmp.service.kpi;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "kpi")
public class KpiProperties {

	private int maxIndicatorPointsPerRequest;
	private KpiTaskProperties task;
	private IndicatorProperties indicator;

	public void setMaxIndicatorPointsPerRequest(int maxIndicatorPointsPerRequest) {
		this.maxIndicatorPointsPerRequest = maxIndicatorPointsPerRequest;
	}

	public Integer getMaxIndicatorPointsPerRequest() {
		return maxIndicatorPointsPerRequest;
	}

	public void setMaxIndicatorPointsPerRequest(Integer maxIndicatorPointsPerRequest) {
		this.maxIndicatorPointsPerRequest = maxIndicatorPointsPerRequest;
	}

	public KpiTaskProperties getTask() {
		return task;
	}

	public void setTask(KpiTaskProperties task) {
		this.task = task;
	}

	public IndicatorProperties getIndicator() {
		return indicator;
	}

	public void setIndicator(IndicatorProperties indicator) {
		this.indicator = indicator;
	}
}
