package org.opencdmp.commons.types.description;

import java.util.List;

public class PropertyDefinitionFieldSetEntity {
	private List<PropertyDefinitionFieldSetItemEntity> items;

	private String comment;

	public List<PropertyDefinitionFieldSetItemEntity> getItems() {
		return this.items;
	}

	public void setItems(List<PropertyDefinitionFieldSetItemEntity> items) {
		this.items = items;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
