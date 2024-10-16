package org.opencdmp.service.fieldsetexpander;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FieldSetExpanderServiceProperties.class)
public class FieldSetExpanderServiceConfiguration {
	private final FieldSetExpanderServiceProperties expanderServiceProperties;

	public FieldSetExpanderServiceConfiguration(FieldSetExpanderServiceProperties expanderServiceProperties) {
		this.expanderServiceProperties = expanderServiceProperties;
	}

	public FieldSetExpanderServiceProperties getExpanderServiceProperties() {
		return this.expanderServiceProperties;
	}
}
