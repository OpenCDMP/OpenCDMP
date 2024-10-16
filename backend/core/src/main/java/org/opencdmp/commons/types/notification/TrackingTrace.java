package org.opencdmp.commons.types.notification;

import java.time.Instant;

public class TrackingTrace {

    private Instant at;
    private String data;

    public TrackingTrace(Instant at, String data) {
        this.at = at;
        this.data = data;
    }

    public Instant getAt() {
        return at;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
