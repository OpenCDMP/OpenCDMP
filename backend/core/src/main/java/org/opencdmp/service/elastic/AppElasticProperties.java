package org.opencdmp.service.elastic;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app-elastic")
public class AppElasticProperties {
	private boolean enabled;
	private String planIndexName;
	private String descriptionIndexName;
	private int resetBatchSize;
	private boolean enableIcuAnalysisPlugin;

	public String getPlanIndexName() {
		return this.planIndexName;
	}

	public void setPlanIndexName(String planIndexName) {
		this.planIndexName = planIndexName;
	}

	public boolean isEnableIcuAnalysisPlugin() {
		return this.enableIcuAnalysisPlugin;
	}

	public void setEnableIcuAnalysisPlugin(boolean enableIcuAnalysisPlugin) {
		this.enableIcuAnalysisPlugin = enableIcuAnalysisPlugin;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDescriptionIndexName() {
		return this.descriptionIndexName;
	}

	public void setDescriptionIndexName(String descriptionIndexName) {
		this.descriptionIndexName = descriptionIndexName;
	}

	public int getResetBatchSize() {
		return this.resetBatchSize;
	}

	public void setResetBatchSize(int resetBatchSize) {
		this.resetBatchSize = resetBatchSize;
	}
}
