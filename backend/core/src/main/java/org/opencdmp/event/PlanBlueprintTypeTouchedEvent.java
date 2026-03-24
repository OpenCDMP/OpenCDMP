package org.opencdmp.event;

import java.util.UUID;

public class PlanBlueprintTypeTouchedEvent {

	public PlanBlueprintTypeTouchedEvent() {
	}

	public PlanBlueprintTypeTouchedEvent(UUID id) {
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
