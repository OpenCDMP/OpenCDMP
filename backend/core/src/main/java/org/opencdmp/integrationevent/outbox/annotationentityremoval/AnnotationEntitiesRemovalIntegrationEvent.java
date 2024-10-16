package org.opencdmp.integrationevent.outbox.annotationentityremoval;

import org.opencdmp.integrationevent.TrackedEvent;

import java.util.List;
import java.util.UUID;

public class AnnotationEntitiesRemovalIntegrationEvent extends TrackedEvent {

    private List<UUID> entityIds;

    public List<UUID> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(List<UUID> entityIds) {
        this.entityIds = entityIds;
    }
}
