package org.opencdmp.event;

import java.util.UUID;

public class TagTouchedEvent {

	public TagTouchedEvent() {
	}

	public TagTouchedEvent(UUID id) {
		this.id = id;
	}

	private UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

}