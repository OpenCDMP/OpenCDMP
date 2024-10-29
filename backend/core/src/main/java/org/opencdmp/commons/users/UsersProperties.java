package org.opencdmp.commons.users;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "users")
public class UsersProperties {

	EmailExpirationTimeSeconds emailExpirationTimeSeconds;

	public EmailExpirationTimeSeconds getEmailExpirationTimeSeconds() {
		return emailExpirationTimeSeconds;
	}

	public void setEmailExpirationTimeSeconds(EmailExpirationTimeSeconds emailExpirationTimeSeconds) {
		this.emailExpirationTimeSeconds = emailExpirationTimeSeconds;
	}
}
