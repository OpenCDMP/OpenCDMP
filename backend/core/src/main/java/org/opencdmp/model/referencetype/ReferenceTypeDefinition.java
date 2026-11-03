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

	// Semantics of the system fields every reference has, e.g. what the export reads a picked reference's id or label as.
	public final static String _referenceIdSemantics = "referenceIdSemantics";
	private List<String> referenceIdSemantics;

	public final static String _labelSemantics = "labelSemantics";
	private List<String> labelSemantics;

	public final static String _descriptionSemantics = "descriptionSemantics";
	private List<String> descriptionSemantics;

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

	public List<String> getReferenceIdSemantics() {
		return referenceIdSemantics;
	}

	public void setReferenceIdSemantics(List<String> referenceIdSemantics) {
		this.referenceIdSemantics = referenceIdSemantics;
	}

	public List<String> getLabelSemantics() {
		return labelSemantics;
	}

	public void setLabelSemantics(List<String> labelSemantics) {
		this.labelSemantics = labelSemantics;
	}

	public List<String> getDescriptionSemantics() {
		return descriptionSemantics;
	}

	public void setDescriptionSemantics(List<String> descriptionSemantics) {
		this.descriptionSemantics = descriptionSemantics;
	}
}
