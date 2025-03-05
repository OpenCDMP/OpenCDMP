package org.opencdmp.model.planblueprint;


public class BlueprintDescriptionTemplate {


	public final static String _descriptionTemplate = "descriptionTemplate";
	private org.opencdmp.model.descriptiontemplate.DescriptionTemplate descriptionTemplate;

	public final static String _minMultiplicity = "minMultiplicity";
	private Integer minMultiplicity;

	public final static String _maxMultiplicity = "maxMultiplicity";
	private Integer maxMultiplicity;

	public org.opencdmp.model.descriptiontemplate.DescriptionTemplate getDescriptionTemplate() {
		return descriptionTemplate;
	}

	public void setDescriptionTemplate(org.opencdmp.model.descriptiontemplate.DescriptionTemplate descriptionTemplate) {
		this.descriptionTemplate = descriptionTemplate;
	}

	public Integer getMinMultiplicity() {
		return minMultiplicity;
	}

	public void setMinMultiplicity(Integer minMultiplicity) {
		this.minMultiplicity = minMultiplicity;
	}

	public Integer getMaxMultiplicity() {
		return maxMultiplicity;
	}

	public void setMaxMultiplicity(Integer maxMultiplicity) {
		this.maxMultiplicity = maxMultiplicity;
	}
}
