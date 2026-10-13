package org.opencdmp.model.referencetype;

import org.opencdmp.model.externalfetcher.ExternalFetcherBaseSourceConfiguration;

import java.util.List;

public class ReferenceTypeDefinition {

	public final static String _fields = "fields";
	private List<ReferenceTypeField> fields;

	public final static String _sources = "sources";
	private List<ExternalFetcherBaseSourceConfiguration> sources;

	// Builds a manual entry's name from its fields, e.g. "{firstName} {lastName}".
	public final static String _labelTemplate = "labelTemplate";
	private String labelTemplate;

	public List<ReferenceTypeField> getFields() {
		return fields;
	}

	public void setFields(List<ReferenceTypeField> fields) {
		this.fields = fields;
	}

	public List<ExternalFetcherBaseSourceConfiguration> getSources() {
		return sources;
	}

	public void setSources(List<ExternalFetcherBaseSourceConfiguration> sources) {
		this.sources = sources;
	}

	public String getLabelTemplate() {
		return labelTemplate;
	}

	public void setLabelTemplate(String labelTemplate) {
		this.labelTemplate = labelTemplate;
	}
}
