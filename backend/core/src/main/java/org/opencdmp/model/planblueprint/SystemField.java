package org.opencdmp.model.planblueprint;

import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;

public class SystemField extends Field {

	public final static String _systemFieldType = "systemFieldType";
	private PlanBlueprintSystemFieldType systemFieldType;

	public PlanBlueprintSystemFieldType getSystemFieldType() {
		return systemFieldType;
	}

	public void setSystemFieldType(PlanBlueprintSystemFieldType systemFieldType) {
		this.systemFieldType = systemFieldType;
	}
}
