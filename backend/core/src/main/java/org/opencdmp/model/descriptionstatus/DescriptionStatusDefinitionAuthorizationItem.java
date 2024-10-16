package org.opencdmp.model.descriptionstatus;

import org.opencdmp.commons.enums.PlanUserRole;

import java.util.List;

public class DescriptionStatusDefinitionAuthorizationItem {

    public final static String _roles = "roles";
    private List<String> roles;

    public final static String _planRoles = "planRoles";
    private List<PlanUserRole> planRoles;

    public final static String _allowAuthenticated = "allowAuthenticated";
    private Boolean allowAuthenticated;

    public final static String _allowAnonymous = "allowAnonymous";
    private Boolean allowAnonymous;

    public List<String> getRoles() { return this.roles; }

    public void setRoles(List<String> roles) { this.roles = roles; }

    public List<PlanUserRole> getPlanRoles() { return this.planRoles; }

    public void setPlanRoles(List<PlanUserRole> planRoles) { this.planRoles = planRoles; }

    public Boolean getAllowAuthenticated() { return this.allowAuthenticated; }

    public void setAllowAuthenticated(Boolean allowAuthenticated) { this.allowAuthenticated = allowAuthenticated; }

    public Boolean getAllowAnonymous() { return this.allowAnonymous; }

    public void setAllowAnonymous(Boolean allowAnonymous) { this.allowAnonymous = allowAnonymous; }
}
