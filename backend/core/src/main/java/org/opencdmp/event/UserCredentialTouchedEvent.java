package org.opencdmp.event;

import java.util.UUID;

public class UserCredentialTouchedEvent {
	public UserCredentialTouchedEvent() {
	}

	public UserCredentialTouchedEvent(UUID id, String subjectId) {
		this.id = id;
		this.subjectId = subjectId;
	}

	private UUID id;

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	private String subjectId;

	public String getSubjectId() {
		return this.subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
}
