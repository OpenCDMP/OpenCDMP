package org.opencdmp.model.planreference;

import java.util.UUID;

public class PlanReferenceData {
	private UUID blueprintFieldId;
	public final static String _blueprintFieldId = "blueprintFieldId";

	public UUID getBlueprintFieldId() {
		return blueprintFieldId;
	}

	public void setBlueprintFieldId(UUID blueprintFieldId) {
		this.blueprintFieldId = blueprintFieldId;
	}
}
