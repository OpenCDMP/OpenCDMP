package org.opencdmp.integrationevent.outbox.annotationentitytouch;

import org.opencdmp.integrationevent.TrackedEvent;

import java.util.List;
import java.util.UUID;

public class AnnotationEntitiesTouchedIntegrationEvent extends TrackedEvent {

    private List<AnnotationEntityTouchedIntegrationEvent> events;

    public List<AnnotationEntityTouchedIntegrationEvent> getEvents() {
        return this.events;
    }

    public void setEvents(List<AnnotationEntityTouchedIntegrationEvent> events) {
        this.events = events;
    }

    public static class AnnotationEntityTouchedIntegrationEvent {

        private UUID entityId;

        private List<UUID> userIds;

        public UUID getEntityId() {
            return this.entityId;
        }

        public void setEntityId(UUID entityId) {
            this.entityId = entityId;
        }

        public List<UUID> getUserIds() {
            return this.userIds;
        }

        public void setUserIds(List<UUID> userIds) {
            this.userIds = userIds;
        }

    }
}
