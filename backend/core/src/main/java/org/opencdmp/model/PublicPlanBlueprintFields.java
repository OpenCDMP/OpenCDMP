package org.opencdmp.model;


import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;

import java.util.UUID;

public class PublicPlanBlueprintFields {

	public final static String _id = "id";
	private UUID id;

	public final static String _label = "label";
	private String label;

	public final static String _description = "description";
	private String description;

	public final static String _category = "category";
	private PlanBlueprintFieldCategory category;

	public final static String _placeholder = "placeholder";
	private String placeholder;

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

	public PlanBlueprintFieldCategory getCategory() {
		return category;
	}

	public void setCategory(PlanBlueprintFieldCategory category) {
		this.category = category;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}
}


