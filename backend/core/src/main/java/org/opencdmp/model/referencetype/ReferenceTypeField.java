package org.opencdmp.model.referencetype;

import org.opencdmp.commons.enums.ReferenceFieldDataType;

public class ReferenceTypeField {

	public final static String _code = "code";
	private String code;

	public final static String _label = "label";
	private String label;

	public final static String _description = "description";
	private String description;

	public final static String _dataType = "dataType";
	private ReferenceFieldDataType dataType;


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ReferenceFieldDataType getDataType() {
		return dataType;
	}

	public void setDataType(ReferenceFieldDataType dataType) {
		this.dataType = dataType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
