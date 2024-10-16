package org.opencdmp.authorization;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("AppAuthorizationConfiguration")
@EnableConfigurationProperties(AuthorizationProperties.class)
public class AuthorizationConfiguration {
	private final AuthorizationProperties authorizationProperties;

	public AuthorizationConfiguration(AuthorizationProperties authorizationProperties) {
		this.authorizationProperties = authorizationProperties;
	}

	public AuthorizationProperties getAuthorizationProperties() {
		return this.authorizationProperties;
	}
}
