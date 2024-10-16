package org.opencdmp.model.planblueprint;

import java.util.List;

public class Definition {

	public final static String _sections = "sections";
	private List<Section> sections;

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}
}
