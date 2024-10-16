package org.opencdmp.commons.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationConfiguration {
	private final NotificationProperties properties;

	@Autowired
	public NotificationConfiguration(NotificationProperties properties) {
		this.properties = properties;
	}

	public NotificationProperties getProperties() {
		return properties;
	}
}
