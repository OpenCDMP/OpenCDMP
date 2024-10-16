package org.opencdmp.model.descriptiontemplate.fielddata;

import org.opencdmp.model.referencetype.ReferenceType;

public class ReferenceTypeData extends BaseFieldData {
	private Boolean multipleSelect;
	public final static String _multipleSelect = "multipleSelect";

	private ReferenceType referenceType;
	public final static String _referenceType = "referenceType";
	public Boolean getMultipleSelect() {
		return multipleSelect;
	}
	public void setMultipleSelect(Boolean multipleSelect) {
		this.multipleSelect = multipleSelect;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}
}
