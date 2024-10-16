package org.opencdmp.model.descriptiontemplate;

import java.util.List;

public class FieldSet {

	public final static String _id = "id";
	private String id;

	public final static String _ordinal = "ordinal";
	private Integer ordinal;

	public final static String _title = "title";
	private String title;

	public final static String _description = "description";
	private String description;

	public final static String _extendedDescription = "extendedDescription";
	private String extendedDescription;

	public final static String _additionalInformation = "additionalInformation";
	private String additionalInformation;

	public final static String _multiplicity = "multiplicity";
	private Multiplicity multiplicity;

	private Boolean hasMultiplicity;
	public static final String _hasMultiplicity = "hasMultiplicity";

	public final static String _hasCommentField = "hasCommentField";
	private Boolean hasCommentField;

	public final static String _fields = "fields";
	private List<Field> fields;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getOrdinal() {
		return this.ordinal;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExtendedDescription() {
		return this.extendedDescription;
	}

	public void setExtendedDescription(String extendedDescription) {
		this.extendedDescription = extendedDescription;
	}

	public String getAdditionalInformation() {
		return this.additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public Multiplicity getMultiplicity() {
		return this.multiplicity;
	}

	public void setMultiplicity(Multiplicity multiplicity) {
		this.multiplicity = multiplicity;
	}

	public Boolean getHasMultiplicity() {
		return this.hasMultiplicity;
	}

	public void setHasMultiplicity(Boolean hasMultiplicity) {
		this.hasMultiplicity = hasMultiplicity;
	}

	public Boolean getHasCommentField() {
		return this.hasCommentField;
	}

	public void setHasCommentField(Boolean hasCommentField) {
		this.hasCommentField = hasCommentField;
	}

	public List<Field> getFields() {
		return this.fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

}
