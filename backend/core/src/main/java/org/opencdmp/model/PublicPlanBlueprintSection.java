package org.opencdmp.model;


import java.util.List;
import java.util.UUID;

public class PublicPlanBlueprintSection {

	public final static String _id = "id";
	private UUID id;

	public final static String _label = "label";
	private String label;

	public final static String _description = "description";
	private String description;

	public final static String _hasTemplates = "hasTemplates";
	private Boolean hasTemplates;

	public final static String _fields = "fields";
	private List<PublicPlanBlueprintFields> fields;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public Boolean getHasTemplates() {
		return hasTemplates;
	}

	public void setHasTemplates(Boolean hasTemplates) {
		this.hasTemplates = hasTemplates;
	}

	public List<PublicPlanBlueprintFields> getFields() {
		return fields;
	}

	public void setFields(List<PublicPlanBlueprintFields> fields) {
		this.fields = fields;
	}
}


