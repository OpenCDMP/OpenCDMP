package org.opencdmp.integrationevent.outbox.userremoval;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface UserRemovalIntegrationEventHandler {

    void handle(UUID userId) throws InvalidApplicationException;

}
