package org.opencdmp.configurations.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SwaggerConfigProperties.class, SwaggerSpringDocProperties.class})
public class SwaggerConfiguration {

	private final SwaggerSpringDocProperties springDocProperties;
	private final SwaggerConfigProperties configProperties;

	@Autowired
	public SwaggerConfiguration(SwaggerSpringDocProperties springDocProperties, SwaggerConfigProperties configProperties) {
        this.springDocProperties = springDocProperties;
        this.configProperties = configProperties;
	}

	public SwaggerConfigProperties getConfigProperties() {
		return configProperties;
	}

	public SwaggerSpringDocProperties getSpringDocProperties() {
		return springDocProperties;
	}
}
