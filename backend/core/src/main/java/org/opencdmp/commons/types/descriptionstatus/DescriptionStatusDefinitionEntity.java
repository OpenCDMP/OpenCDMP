package org.opencdmp.commons.types.descriptionstatus;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class DescriptionStatusDefinitionEntity {

    @XmlElement(name = "authorization")
    private DescriptionStatusDefinitionAuthorizationEntity authorization;

    public DescriptionStatusDefinitionAuthorizationEntity getAuthorization() { return this.authorization; }

    public void setAuthorization(DescriptionStatusDefinitionAuthorizationEntity authorization) { this.authorization = authorization; }
}
