package org.opencdmp.model.descriptiontemplate;


import java.util.List;

public class Page {

	public final static String _id = "id";
	private String id;

	public final static String _ordinal = "ordinal";
	private Integer ordinal;

	public final static String _title = "title";
	private String title;

	public final static String _sections = "sections";
	private List<Section> sections;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

}


