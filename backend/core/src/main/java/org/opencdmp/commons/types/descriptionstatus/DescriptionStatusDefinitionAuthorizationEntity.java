package org.opencdmp.commons.types.descriptionstatus;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "authorization")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionStatusDefinitionAuthorizationEntity {

    @XmlElement(name = "edit")
    private DescriptionStatusDefinitionAuthorizationItemEntity edit;

    public DescriptionStatusDefinitionAuthorizationItemEntity getEdit() { return this.edit; }

    public void setEdit(DescriptionStatusDefinitionAuthorizationItemEntity edit) { this.edit = edit; }
}
