package org.opencdmp.model.planblueprint;

import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;

import java.util.List;
import java.util.UUID;

public abstract class Field {

	public final static String _id = "id";
	private UUID id;

	public final static String _category = "category";
	private PlanBlueprintFieldCategory category;

	public final static String _label = "label";
	private String label;

	public final static String _placeholder = "placeholder";
	private String placeholder;

	public final static String _description = "description";
	private String description;

	public final static String _ordinal = "ordinal";

	private List<String> semantics;
	public final static String _semantics  = "semantics";
	private Integer ordinal;

	public final static String _required = "required";
	private Boolean required;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public PlanBlueprintFieldCategory getCategory() {
		return category;
	}

	public void setCategory(PlanBlueprintFieldCategory category) {
		this.category = category;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
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

	public Integer getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public Boolean isRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
}
