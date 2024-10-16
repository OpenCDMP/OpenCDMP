package org.opencdmp.model.referencetype;

import org.opencdmp.model.externalfetcher.ExternalFetcherBaseSourceConfiguration;

import java.util.List;

public class ReferenceTypeDefinition {

	public final static String _fields = "fields";
	private List<ReferenceTypeField> fields;

	public final static String _sources = "sources";
	private List<ExternalFetcherBaseSourceConfiguration> sources;

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
}
