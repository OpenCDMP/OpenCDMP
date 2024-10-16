package org.opencdmp.integrationevent.outbox.userremoval;

import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class UserRemovalIntegrationEvent extends TrackedEvent {

    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

}
