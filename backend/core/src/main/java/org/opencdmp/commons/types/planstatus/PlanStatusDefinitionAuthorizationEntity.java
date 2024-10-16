package org.opencdmp.commons.types.planstatus;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "authorization")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanStatusDefinitionAuthorizationEntity {
    @XmlElement(name = "edit")
    private PlanStatusDefinitionAuthorizationItemEntity edit;

    public PlanStatusDefinitionAuthorizationItemEntity getEdit() {
        return this.edit;
    }

    public void setEdit(PlanStatusDefinitionAuthorizationItemEntity edit) {
        this.edit = edit;
    }
}
