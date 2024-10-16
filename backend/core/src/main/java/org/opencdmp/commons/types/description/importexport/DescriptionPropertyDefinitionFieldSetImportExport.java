package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionPropertyDefinitionFieldSetImportExport {

	@XmlElement(name = "fieldSetId")
	private String fieldSetId;
	@XmlElementWrapper(name = "items")
	@XmlElement(name = "item")
	private List<DescriptionPropertyDefinitionFieldSetItemImportExport> items;

	@XmlElement(name = "comment")
	private String comment;

	public List<DescriptionPropertyDefinitionFieldSetItemImportExport> getItems() {
		return this.items;
	}

	public void setItems(List<DescriptionPropertyDefinitionFieldSetItemImportExport> items) {
		this.items = items;
	}

	public String getFieldSetId() {
		return this.fieldSetId;
	}

	public void setFieldSetId(String fieldSetId) {
		this.fieldSetId = fieldSetId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
