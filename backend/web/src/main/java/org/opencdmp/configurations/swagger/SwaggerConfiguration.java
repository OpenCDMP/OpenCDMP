package org.opencdmp.configurations.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerConfiguration {
	private final SwaggerProperties properties;

	@Autowired
	public SwaggerConfiguration(SwaggerProperties properties) {
		this.properties = properties;
	}

	public SwaggerProperties getProperties() {
		return properties;
	}
}
