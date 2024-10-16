package org.opencdmp.integrationevent.outbox.usertouched;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface UserTouchedIntegrationEventHandler {

    void handle(UUID userId) throws InvalidApplicationException;

}
