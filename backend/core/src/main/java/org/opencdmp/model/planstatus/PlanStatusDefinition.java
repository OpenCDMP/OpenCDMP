package org.opencdmp.model.planstatus;

public class PlanStatusDefinition {

    public final static String _authorization = "authorization";
    private PlanStatusDefinitionAuthorization authorization;


    public PlanStatusDefinitionAuthorization getAuthorization() { return this.authorization; }

    public void setAuthorization(PlanStatusDefinitionAuthorization authorization) { this.authorization = authorization; }
}
