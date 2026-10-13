package org.opencdmp.model.planblueprint;

import org.opencdmp.model.prefillingsource.PrefillingSource;

import java.util.List;

public class RootSection extends org.opencdmp.model.descriptiontemplate.Section {

	public final static String _hasTemplates = "hasTemplates";
	private Boolean hasTemplates;

	public final static String _descriptionTemplates = "descriptionTemplates";
	private List<BlueprintDescriptionTemplate> descriptionTemplates;

	public final static String _prefillingSourcesEnabled = "prefillingSourcesEnabled";
	private Boolean prefillingSourcesEnabled;

	public final static String _canEditDescriptionTemplates = "canEditDescriptionTemplates";
	private Boolean canEditDescriptionTemplates;

	public final static String _prefillingSources = "prefillingSources";
	private List<PrefillingSource> prefillingSources;

	public Boolean getHasTemplates() {
		return hasTemplates;
	}

	public void setHasTemplates(Boolean hasTemplates) {
		this.hasTemplates = hasTemplates;
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
}
