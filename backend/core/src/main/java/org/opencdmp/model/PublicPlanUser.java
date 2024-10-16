package org.opencdmp.model;

import org.opencdmp.commons.enums.PlanUserRole;
import java.util.UUID;

public class PublicPlanUser {

    public static final String _id = "id";
    private UUID id;


    public static final String _plan = "plan";
    private PublicPlan plan;


    public static final String _user = "user";
    private PublicUser user;


    public static final String _role = "role";
    private PlanUserRole role;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PublicPlan getPlan() {
        return plan;
    }

    public void setPlan(PublicPlan plan) {
        this.plan = plan;
    }

    public PublicUser getUser() {
        return user;
    }

    public void setUser(PublicUser user) {
        this.user = user;
    }

    public PlanUserRole getRole() {
        return role;
    }

    public void setRole(PlanUserRole role) {
        this.role = role;
    }
}
