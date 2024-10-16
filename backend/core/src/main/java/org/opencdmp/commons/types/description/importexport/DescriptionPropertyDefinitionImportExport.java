package org.opencdmp.commons.types.description.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionPropertyDefinitionImportExport {

    @XmlElementWrapper(name = "fieldSets")
    @XmlElement(name = "fieldSet")
    private List<DescriptionPropertyDefinitionFieldSetImportExport> fieldSets;

    public List<DescriptionPropertyDefinitionFieldSetImportExport> getFieldSets() {
        return this.fieldSets;
    }

    public void setFieldSets(List<DescriptionPropertyDefinitionFieldSetImportExport> fieldSets) {
        this.fieldSets = fieldSets;
    }
}



