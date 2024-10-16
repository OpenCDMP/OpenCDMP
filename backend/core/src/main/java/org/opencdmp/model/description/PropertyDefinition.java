package org.opencdmp.model.description;


import java.util.Map;

public class PropertyDefinition {

	public final static String _fieldSets = "fieldSets";

	private Map<String, PropertyDefinitionFieldSet> fieldSets;

	public Map<String, PropertyDefinitionFieldSet> getFieldSets() {
		return fieldSets;
	}

	public void setFieldSets(Map<String, PropertyDefinitionFieldSet> fieldSets) {
		this.fieldSets = fieldSets;
	}
}
