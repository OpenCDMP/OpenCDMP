package org.opencdmp.event;

import java.util.UUID;

public class UserTouchedEvent {
	public UserTouchedEvent() {
	}

	public UserTouchedEvent(UUID userId) {
		this.userId = userId;
	}

	private UUID userId;

	public UUID getUserId() {
		return this.userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
}
