package org.opencdmp.service.accounting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AccountingProperties.class)
public class AccountingConfiguration {
	private final AccountingProperties properties;

	@Autowired
	public AccountingConfiguration(AccountingProperties properties) {
		this.properties = properties;
	}

	public AccountingProperties getProperties() {
		return this.properties;
	}
}
