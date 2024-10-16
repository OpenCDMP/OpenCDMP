package org.opencdmp.commons.types.planblueprint.importexport;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlueprintDefinitionImportExport {

    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    private List<BlueprintSectionImportExport> sections;

    public List<BlueprintSectionImportExport> getSections() {
        return this.sections;
    }

    public void setSections(List<BlueprintSectionImportExport> sections) {
        this.sections = sections;
    }
}