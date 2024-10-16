package org.opencdmp.integrationevent.outbox.notification;

import org.opencdmp.commons.enums.notification.NotificationContactType;
import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class NotifyIntegrationEvent extends TrackedEvent {

	private UUID userId;

	private UUID notificationType;

	private NotificationContactType contactTypeHint;

	private String contactHint;

	private String data;

	private String provenanceRef;

	public NotifyIntegrationEvent() {
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(UUID notificationType) {
		this.notificationType = notificationType;
	}

	public NotificationContactType getContactTypeHint() {
		return contactTypeHint;
	}

	public void setContactTypeHint(NotificationContactType contactTypeHint) {
		this.contactTypeHint = contactTypeHint;
	}

	public String getContactHint() {
		return contactHint;
	}

	public void setContactHint(String contactHint) {
		this.contactHint = contactHint;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getProvenanceRef() {
		return provenanceRef;
	}

	public void setProvenanceRef(String provenanceRef) {
		this.provenanceRef = provenanceRef;
	}
}
