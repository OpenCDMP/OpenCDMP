package org.opencdmp.model.planblueprint;

import java.util.UUID;

public class DescriptionTemplate {


	public final static String _descriptionTemplateGroupId = "descriptionTemplateGroupId";
	private UUID descriptionTemplateGroupId;

	public final static String _label = "label";
	private String label;

	public final static String _minMultiplicity = "minMultiplicity";
	private Integer minMultiplicity;

	public final static String _maxMultiplicity = "maxMultiplicity";
	private Integer maxMultiplicity;

	public UUID getDescriptionTemplateGroupId() {
		return descriptionTemplateGroupId;
	}

	public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
		this.descriptionTemplateGroupId = descriptionTemplateGroupId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
