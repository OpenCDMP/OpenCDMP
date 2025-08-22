package org.opencdmp.model;


import java.util.List;

public class PublicPlanBlueprintDefinition {

	public final static String _sections = "sections";
	private List<PublicPlanBlueprintSection> sections;

	public List<PublicPlanBlueprintSection> getSections() {
		return sections;
	}

	public void setSections(List<PublicPlanBlueprintSection> sections) {
		this.sections = sections;
	}
}
