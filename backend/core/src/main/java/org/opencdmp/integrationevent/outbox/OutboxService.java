package org.opencdmp.integrationevent.outbox;

public interface OutboxService {
	void publish(OutboxIntegrationEvent event);
}
