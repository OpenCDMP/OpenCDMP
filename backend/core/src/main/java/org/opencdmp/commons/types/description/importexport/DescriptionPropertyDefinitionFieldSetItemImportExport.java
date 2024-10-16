package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionPropertyDefinitionFieldSetItemImportExport {


	@XmlElementWrapper(name = "fields")
	@XmlElement(name = "field")
	private List<DescriptionFieldImportExport> fields;
	@XmlElement(name = "ordinal")
	private int ordinal;

	public List<DescriptionFieldImportExport> getFields() {
		return this.fields;
	}

	public void setFields(List<DescriptionFieldImportExport> fields) {
		this.fields = fields;
	}

	public int getOrdinal() {
		return this.ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
}
