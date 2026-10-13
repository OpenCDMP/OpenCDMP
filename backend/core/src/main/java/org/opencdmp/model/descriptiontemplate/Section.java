package org.opencdmp.model.descriptiontemplate;


import java.util.List;

public class Section {

	public final static String _id = "id";
	private String id;

	public final static String _ordinal = "ordinal";
	private Integer ordinal;

	public final static String _title = "title";
	private String title;

	public final static String _description = "description";
	private String description;

	public final static String _extendedDescription = "extendedDescription";
	private String extendedDescription; //TODO maybe remove

	public final static String _sections = "sections";
	private List<Section> sections;

	public final static String _fieldSets = "fieldSets";
	private List<FieldSet> fieldSets;

	public final static String _semantics = "semantics";
	private List<String> semantics;

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

	public List<Section> getSections() {
		return this.sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public List<FieldSet> getFieldSets() {
		return this.fieldSets;
	}

	public void setFieldSets(List<FieldSet> fieldSets) {
		this.fieldSets = fieldSets;
	}

	public List<String> getSemantics() {
		return this.semantics;
	}

	public void setSemantics(List<String> semantics) {
		this.semantics = semantics;
	}
}


