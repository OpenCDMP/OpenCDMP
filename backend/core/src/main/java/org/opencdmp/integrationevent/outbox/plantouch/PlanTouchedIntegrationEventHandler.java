package org.opencdmp.integrationevent.outbox.plantouch;

import java.util.List;
import java.util.UUID;

public interface PlanTouchedIntegrationEventHandler {

    void handlePlan(List<UUID> planIds);
}
