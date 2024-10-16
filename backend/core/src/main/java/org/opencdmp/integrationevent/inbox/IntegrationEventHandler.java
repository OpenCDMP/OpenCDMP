package org.opencdmp.integrationevent.inbox;

import javax.management.InvalidApplicationException;

public interface IntegrationEventHandler {

    EventProcessingStatus handle(IntegrationEventProperties properties, String message) throws InvalidApplicationException;

}
