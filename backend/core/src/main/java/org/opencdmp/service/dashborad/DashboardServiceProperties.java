package org.opencdmp.service.dashborad;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

@ConfigurationProperties(prefix = "dashboard")
public class DashboardServiceProperties {
	private List<UUID> referenceTypeCounters;

	public List<UUID> getReferenceTypeCounters() {
		return referenceTypeCounters;
	}

	public void setReferenceTypeCounters(List<UUID> referenceTypeCounters) {
		this.referenceTypeCounters = referenceTypeCounters;
	}
}

