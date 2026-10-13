package org.opencdmp.integrationevent.outbox.plantouch;

import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.integrationevent.TrackedEvent;

import java.util.List;
import java.util.UUID;


public class PlanTouchedIntegrationEvent extends TrackedEvent {

    private List<PlanTouched> plans;

    public List<PlanTouched> getPlans() {
        return plans;
    }

    public void setPlans(List<PlanTouched> plans) {
        this.plans = plans;
    }


    public static class PlanTouched {
        private PlanModel plan;
        private UUID tenantId;

        public PlanModel getPlan() {
            return plan;
        }

        public void setPlan(PlanModel plan) {
            this.plan = plan;
        }

        public UUID getTenantId() {
            return tenantId;
        }

        public void setTenantId(UUID tenantId) {
            this.tenantId = tenantId;
        }
    }
}
