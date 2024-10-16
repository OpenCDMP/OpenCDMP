package org.opencdmp.commons.types.planstatus;

import jakarta.xml.bind.annotation.*;
import org.opencdmp.commons.enums.PlanUserRole;

import java.util.List;

@XmlRootElement(name = "edit")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanStatusDefinitionAuthorizationItemEntity {

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "roles")
    public List<String> roles;

    @XmlElementWrapper(name = "plan_roles")
    @XmlElement(name = "plan_roles")
    public List<PlanUserRole> planRoles;

    @XmlElement(name = "allowAuthenticated")
    public Boolean allowAuthenticated;

    @XmlElement(name = "allowAnonymous")
    public Boolean allowAnonymous;

    public List<String> getRoles() { return this.roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<PlanUserRole> getPlanRoles() { return this.planRoles; }
    public void setPlanRoles(List<PlanUserRole> planRoles) { this.planRoles = planRoles; }

    public Boolean getAllowAuthenticated() { return this.allowAuthenticated; }
    public void setAllowAuthenticated(Boolean allowAuthenticated) { this.allowAuthenticated = allowAuthenticated; }

    public Boolean getAllowAnonymous() { return this.allowAnonymous; }
    public void setAllowAnonymous(Boolean allowAnonymous) { this.allowAnonymous = allowAnonymous; }
}
