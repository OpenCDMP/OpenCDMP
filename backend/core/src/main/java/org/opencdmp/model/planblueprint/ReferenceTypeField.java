package org.opencdmp.model.planblueprint;

import org.opencdmp.model.referencetype.ReferenceType;

public class ReferenceTypeField extends Field {

	public final static String _referenceType = "referenceType";
	private ReferenceType referenceType;

	public final static String _multipleSelect = "multipleSelect";
	private Boolean multipleSelect;

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public Boolean getMultipleSelect() {
		return multipleSelect;
	}

	public void setMultipleSelect(Boolean multipleSelect) {
		this.multipleSelect = multipleSelect;
	}
}
