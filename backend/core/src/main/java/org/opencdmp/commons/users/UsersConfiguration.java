package org.opencdmp.commons.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UsersProperties.class)
public class UsersConfiguration {
	private final UsersProperties properties;

	@Autowired
	public UsersConfiguration(UsersProperties properties) {
		this.properties = properties;
	}

	public UsersProperties getProperties() {
		return properties;
	}
}
