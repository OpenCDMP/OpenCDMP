package org.opencdmp.integrationevent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackedEvent {
	@JsonProperty(value = "TrackingContextTag")
	public String trackingContextTag;

	public String getTrackingContextTag() {
		return trackingContextTag;
	}

	public void setTrackingContextTag(String trackingContextTag) {
		this.trackingContextTag = trackingContextTag;
	}


}
