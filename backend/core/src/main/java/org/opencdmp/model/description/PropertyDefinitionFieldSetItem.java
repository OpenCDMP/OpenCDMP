package org.opencdmp.model.description;

import java.util.Map;

public class PropertyDefinitionFieldSetItem {

	public final static String _fields = "fields";
	private Map<String, Field> fields;

	public final static String _ordinal = "ordinal";
	private Integer ordinal;

	public Map<String, Field> getFields() {
		return this.fields;
	}

	public void setFields(Map<String, Field> fields) {
		this.fields = fields;
	}

	public Integer getOrdinal() {
		return this.ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
}
