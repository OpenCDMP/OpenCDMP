package org.opencdmp.model.descriptiontemplate.fielddata;

import org.opencdmp.commons.enums.FieldType;

public abstract class BaseFieldData {

	public final static String _label = "label";
	private String label;

	public final static String _fieldType = "fieldType";
	private FieldType fieldType;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
}
