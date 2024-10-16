package org.opencdmp.model.reference;

import org.opencdmp.commons.enums.ReferenceFieldDataType;

public class Field {

	public final static String _code = "code";
	private String code;

	public final static String _dataType = "dataType";
	private ReferenceFieldDataType dataType;

	public final static String _value = "value";
	private String value;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
