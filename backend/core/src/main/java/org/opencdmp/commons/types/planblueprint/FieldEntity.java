package org.opencdmp.commons.types.planblueprint;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import org.opencdmp.commons.enums.PlanBlueprintFieldCategory;

import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class FieldEntity {
	@XmlAttribute(name="id")
	private UUID id;

	@XmlAttribute(name="category")
	private PlanBlueprintFieldCategory category;

	@XmlAttribute(name="label")
	private String label;

	@XmlAttribute(name="placeholder")
	private String placeholder;

	@XmlAttribute(name="description")
	private String description;

	@XmlAttribute(name="semantics")
	private List<String> semantics;

	@XmlAttribute(name="ordinal")
	private Integer ordinal;

	@XmlAttribute(name="required")
	private boolean required;

	public UUID getId() {
		return this.id;
	}
	public void setId(UUID id) {
		this.id = id;
	}

	public PlanBlueprintFieldCategory getCategory() {
		return this.category;
	}
	public void setCategory(PlanBlueprintFieldCategory category) {
		this.category = category;
	}

	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getPlaceholder() {
		return this.placeholder;
	}
	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getSemantics() {
		return this.semantics;
	}

	public void setSemantics(List<String> semantics) {
		this.semantics = semantics;
	}

	public Integer getOrdinal() {
		return this.ordinal;
	}
	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public boolean isRequired() {
		return this.required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	
}
