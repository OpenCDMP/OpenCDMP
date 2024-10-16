package org.opencdmp.commons.types.plan.importexport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.opencdmp.commons.enums.PlanUserRole;

import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class PlanUserImportExport {

    @XmlElement(name = "id")
    private UUID id;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "role")
    private PlanUserRole role;

    @XmlElement(name = "sectionId")
    private UUID sectionId;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlanUserRole getRole() {
        return this.role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }

    public UUID getSectionId() {
        return this.sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }
}
