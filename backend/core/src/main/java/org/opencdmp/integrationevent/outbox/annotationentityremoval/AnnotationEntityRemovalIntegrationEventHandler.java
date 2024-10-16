package org.opencdmp.integrationevent.outbox.annotationentityremoval;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface AnnotationEntityRemovalIntegrationEventHandler {

    void handleDescription(UUID descriptionId) throws InvalidApplicationException;
    void handlePlan(UUID planId) throws InvalidApplicationException;
}
