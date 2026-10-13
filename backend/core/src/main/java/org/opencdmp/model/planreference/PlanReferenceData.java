package org.opencdmp.model.planreference;

public class PlanReferenceData {
	private String blueprintFieldId;
	public final static String _blueprintFieldId = "blueprintFieldId";

	private Integer ordinal;
	public final static String _ordinal = "ordinal";

	public String getBlueprintFieldId() {
		return blueprintFieldId;
	}

	public void setBlueprintFieldId(String blueprintFieldId) {
		this.blueprintFieldId = blueprintFieldId;
	}

	public Integer getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
}
