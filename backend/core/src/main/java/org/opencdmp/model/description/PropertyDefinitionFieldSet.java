package org.opencdmp.model.description;

import java.util.List;

public class PropertyDefinitionFieldSet {

	public final static String _items = "items";
	private List<PropertyDefinitionFieldSetItem> items;

	public final static String _comment = "comment";
	private String comment;

	public List<PropertyDefinitionFieldSetItem> getItems() {
		return items;
	}

	public void setItems(List<PropertyDefinitionFieldSetItem> items) {
		this.items = items;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
