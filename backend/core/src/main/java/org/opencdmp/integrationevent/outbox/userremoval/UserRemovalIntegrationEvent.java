package org.opencdmp.integrationevent.outbox.userremoval;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opencdmp.integrationevent.TrackedEvent;

import java.util.UUID;

public class UserRemovalIntegrationEvent extends TrackedEvent {

    @JsonProperty(value = "UserId")
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

}
