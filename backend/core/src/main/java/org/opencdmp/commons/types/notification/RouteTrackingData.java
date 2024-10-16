package org.opencdmp.commons.types.notification;

import java.util.List;

public class RouteTrackingData {

    private String trackingId;
    private List<TrackingTrace> traces;

    public RouteTrackingData(String trackingId, List<TrackingTrace> traces) {
        this.trackingId = trackingId;
        this.traces = traces;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public List<TrackingTrace> getTraces() {
        return traces;
    }

    public void setTraces(List<TrackingTrace> traces) {
        this.traces = traces;
    }
}
