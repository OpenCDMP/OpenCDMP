package org.opencdmp.integrationevent.outbox.planremoval;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.UUID;

public interface PlanRemovalIntegrationEventHandler {

    void handlePlan(List<UUID> planIds) throws InvalidApplicationException;
}
