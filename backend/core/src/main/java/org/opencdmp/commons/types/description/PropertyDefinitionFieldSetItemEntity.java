package org.opencdmp.commons.types.description;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PropertyDefinitionFieldSetItemEntity {
	private Map<String, FieldEntity> fields;
	private int ordinal;

	public Map<String, FieldEntity> getFields() {
		return this.fields;
	}

	public void setFields(Map<String, FieldEntity> fields) {
		this.fields = fields;
	}

	public int getOrdinal() {
		return this.ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
}
