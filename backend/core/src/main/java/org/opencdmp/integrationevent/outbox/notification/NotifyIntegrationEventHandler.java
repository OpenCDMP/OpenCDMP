package org.opencdmp.integrationevent.outbox.notification;

import javax.management.InvalidApplicationException;

public interface NotifyIntegrationEventHandler {
	void handle(NotifyIntegrationEvent event) throws InvalidApplicationException;
}
