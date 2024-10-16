package org.opencdmp.commons.types.reference;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefinitionEntity {
	@XmlElementWrapper(name = "fields")
	@XmlElement(name = "field")
	private List<FieldEntity> fields;

	public List<FieldEntity> getFields() {
		return fields;
	}

	public void setFields(List<FieldEntity> fields) {
		this.fields = fields;
	}
	
}
