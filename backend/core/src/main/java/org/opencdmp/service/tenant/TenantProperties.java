package org.opencdmp.service.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

	private String configEncryptionAesKey;
	private String configEncryptionAesIv;

	public String getConfigEncryptionAesKey() {
		return configEncryptionAesKey;
	}

	public void setConfigEncryptionAesKey(String configEncryptionAesKey) {
		this.configEncryptionAesKey = configEncryptionAesKey;
	}

	public String getConfigEncryptionAesIv() {
		return configEncryptionAesIv;
	}

	public void setConfigEncryptionAesIv(String configEncryptionAesIv) {
		this.configEncryptionAesIv = configEncryptionAesIv;
	}
}
