package org.opencdmp.commons.locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LocaleProperties.class)
public class LocaleConfiguration {
	private final LocaleProperties properties;

	@Autowired
	public LocaleConfiguration(LocaleProperties properties) {
		this.properties = properties;
	}

	public LocaleProperties getProperties() {
		return this.properties;
	}
}
