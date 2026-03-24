package org.opencdmp.integrationevent.outbox.plantouch;

import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.integrationevent.TrackedEvent;

import java.util.List;


public class PlanTouchedIntegrationEvent extends TrackedEvent {

    private List<PlanModel> plans;

    public List<PlanModel> getPlans() {
        return plans;
    }

    public void setPlans(List<PlanModel> plans) {
        this.plans = plans;
    }
}
