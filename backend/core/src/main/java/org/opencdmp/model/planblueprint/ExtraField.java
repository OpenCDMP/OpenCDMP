package org.opencdmp.model.planblueprint;

import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;


public class ExtraField extends Field {

	public final static String _dataType = "dataType";
	private PlanBlueprintExtraFieldDataType dataType;

	public PlanBlueprintExtraFieldDataType getDataType() {
		return dataType;
	}

	public void setDataType(PlanBlueprintExtraFieldDataType dataType) {
		this.dataType = dataType;
	}
}

