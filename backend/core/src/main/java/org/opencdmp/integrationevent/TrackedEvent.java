package org.opencdmp.integrationevent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackedEvent {
	public String trackingContextTag;

	public String getTrackingContextTag() {
		return trackingContextTag;
	}

	public void setTrackingContextTag(String trackingContextTag) {
		this.trackingContextTag = trackingContextTag;
	}


}
