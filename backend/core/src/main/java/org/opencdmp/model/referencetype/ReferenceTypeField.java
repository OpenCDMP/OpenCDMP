package org.opencdmp.model.referencetype;

import org.opencdmp.commons.enums.ReferenceFieldDataType;

import java.util.List;

public class ReferenceTypeField {

	public final static String _code = "code";
	private String code;

	public final static String _label = "label";
	private String label;

	public final static String _description = "description";
	private String description;

	public final static String _dataType = "dataType";
	private ReferenceFieldDataType dataType;

	public final static String _semantics = "semantics";
	private List<String> semantics;

	public final static String _required = "required";
	private boolean required;

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

	public List<String> getSemantics() {
		return semantics;
	}

	public void setSemantics(List<String> semantics) {
		this.semantics = semantics;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
