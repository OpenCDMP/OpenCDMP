package org.opencdmp.service.externalfetcher;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "externalapifetcher")
public class ExternalFetcherProperties {

	private boolean disableSSLCertificateValidation;

	public boolean isDisableSSLCertificateValidation() {
		return disableSSLCertificateValidation;
	}

	public void setDisableSSLCertificateValidation(boolean disableSSLCertificateValidation) {
		this.disableSSLCertificateValidation = disableSSLCertificateValidation;
	}
}
