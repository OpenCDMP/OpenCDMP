package org.opencdmp.integrationevent.outbox.annotationentitytouch;

import java.util.UUID;

public interface AnnotationEntityTouchedIntegrationEventHandler {

    void handleDescription(UUID descriptionId);
    void handlePlan(UUID planId);
}
