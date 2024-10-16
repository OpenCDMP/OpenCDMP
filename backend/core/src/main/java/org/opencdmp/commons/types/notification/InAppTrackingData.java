package org.opencdmp.commons.types.notification;

import java.util.List;
import java.util.UUID;

public class InAppTrackingData {

    private UUID inAppNotificationId;
    private List<TrackingTrace> traces;

    public InAppTrackingData(UUID inAppNotificationId, List<TrackingTrace> traces) {
        this.inAppNotificationId = inAppNotificationId;
        this.traces = traces;
    }

    public UUID getInAppNotificationId() {
        return inAppNotificationId;
    }

    public void setInAppNotificationId(UUID inAppNotificationId) {
        this.inAppNotificationId = inAppNotificationId;
    }

    public List<TrackingTrace> getTraces() {
        return traces;
    }

    public void setTraces(List<TrackingTrace> traces) {
        this.traces = traces;
    }
}
