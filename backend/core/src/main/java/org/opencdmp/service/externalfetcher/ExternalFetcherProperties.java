package org.opencdmp.service.externalfetcher;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "externalapifetcher")
public class ExternalFetcherProperties {

	private boolean disableSSLCertificateValidation;
	private Integer maxInMemoryMb;

	public boolean isDisableSSLCertificateValidation() {
		return disableSSLCertificateValidation;
	}

	public void setDisableSSLCertificateValidation(boolean disableSSLCertificateValidation) {
		this.disableSSLCertificateValidation = disableSSLCertificateValidation;
	}

	public Integer getMaxInMemoryMb() {
		return maxInMemoryMb;
	}

	public void setMaxInMemoryMb(Integer maxInMemoryMb) {
		this.maxInMemoryMb = maxInMemoryMb;
	}
}
