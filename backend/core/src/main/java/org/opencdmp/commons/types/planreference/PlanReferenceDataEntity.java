package org.opencdmp.commons.types.planreference;


public class PlanReferenceDataEntity {
	private String blueprintFieldId;

	//ordinal is added after blueprint description templates fields merge
	private int ordinal;

	public String getBlueprintFieldId() {
		return this.blueprintFieldId;
	}

	public void setBlueprintFieldId(String blueprintFieldId) {
		this.blueprintFieldId = blueprintFieldId;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
}
