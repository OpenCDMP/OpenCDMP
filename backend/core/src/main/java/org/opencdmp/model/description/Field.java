package org.opencdmp.model.description;


import org.opencdmp.model.Tag;
import org.opencdmp.model.reference.Reference;

import java.time.Instant;
import java.util.List;

public class Field {

	private String textValue;
	public static final String _textValue = "textValue";

	private List<String> textListValue;
	public static final String _textListValue = "textListValue";

	private Instant dateValue;
	public static final String _dateValue = "dateValue";

	private Boolean booleanValue;
	public static final String _booleanValue = "booleanValue";

	private List<Reference> references;
	public static final String _references = "references";

	private List<Tag> tags;
	public static final String _tags = "tags";

	private ExternalIdentifier externalIdentifier;
	public static final String _externalIdentifier = "externalIdentifier";

	public String getTextValue() {
		return this.textValue;
	}

	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	public List<String> getTextListValue() {
		return this.textListValue;
	}

	public void setTextListValue(List<String> textListValue) {
		this.textListValue = textListValue;
	}

	public Instant getDateValue() {
		return this.dateValue;
	}

	public void setDateValue(Instant dateValue) {
		this.dateValue = dateValue;
	}

	public Boolean getBooleanValue() {
		return this.booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public List<Reference> getReferences() {
		return this.references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}

	public List<Tag> getTags() {
		return this.tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public ExternalIdentifier getExternalIdentifier() {
		return this.externalIdentifier;
	}

	public void setExternalIdentifier(ExternalIdentifier externalIdentifier) {
		this.externalIdentifier = externalIdentifier;
	}
}

