package org.opencdmp.model.planblueprint;


import org.opencdmp.model.prefillingsource.PrefillingSource;

import java.util.List;
import java.util.UUID;

public class Section {

	public final static String _id = "id";
	private UUID id;

	public final static String _description = "description";
	private String description;

	public final static String _label = "label";
	private String label;

	public final static String _ordinal = "ordinal";
	private Integer ordinal;

	public final static String _hasTemplates = "hasTemplates";
	private Boolean hasTemplates;

	public final static String _fields = "fields";
	private List<Field> fields;

	public final static String _descriptionTemplates = "descriptionTemplates";
	private List<BlueprintDescriptionTemplate> descriptionTemplates;

	public final static String _prefillingSourcesEnabled = "prefillingSourcesEnabled";
	private Boolean prefillingSourcesEnabled;

	public final static String _canEditDescriptionTemplates = "canEditDescriptionTemplates";
	private Boolean canEditDescriptionTemplates;

	public final static String _prefillingSources = "prefillingSources";
	private List<PrefillingSource> prefillingSources;

	public final static String _canCreate = "canCreate";
	private Boolean canCreate;


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

	public Integer getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public Boolean getHasTemplates() {
		return hasTemplates;
	}

	public void setHasTemplates(Boolean hasTemplates) {
		this.hasTemplates = hasTemplates;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public List<BlueprintDescriptionTemplate> getDescriptionTemplates() {
		return descriptionTemplates;
	}

	public void setDescriptionTemplates(List<BlueprintDescriptionTemplate> descriptionTemplates) {
		this.descriptionTemplates = descriptionTemplates;
	}

	public Boolean getPrefillingSourcesEnabled() {
		return prefillingSourcesEnabled;
	}

	public void setPrefillingSourcesEnabled(Boolean prefillingSourcesEnabled) {
		this.prefillingSourcesEnabled = prefillingSourcesEnabled;
	}

	public Boolean getCanEditDescriptionTemplates() {
		return canEditDescriptionTemplates;
	}

	public void setCanEditDescriptionTemplates(Boolean canEditDescriptionTemplates) {
		this.canEditDescriptionTemplates = canEditDescriptionTemplates;
	}

	public List<PrefillingSource> getPrefillingSources() {
		return prefillingSources;
	}

	public void setPrefillingSources(List<PrefillingSource> prefillingSources) {
		this.prefillingSources = prefillingSources;
	}

	public Boolean getCanCreate() {
		return canCreate;
	}

	public void setCanCreate(Boolean canCreate) {
		this.canCreate = canCreate;
	}
}


