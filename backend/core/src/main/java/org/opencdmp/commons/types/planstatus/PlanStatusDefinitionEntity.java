package org.opencdmp.commons.types.planstatus;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanStatusDefinitionEntity {

    @XmlElement(name = "authorization")
    private PlanStatusDefinitionAuthorizationEntity authorization;

    public PlanStatusDefinitionAuthorizationEntity getAuthorization() {
        return this.authorization;
    }

    public void setAuthorization(PlanStatusDefinitionAuthorizationEntity authorization) { this.authorization = authorization; }
}
