package org.opencdmp.service.plan;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PlanServiceProperties.class})
public class PlanServiceConfiguration {
	private final PlanServiceProperties planServiceProperties;

	public PlanServiceConfiguration(PlanServiceProperties planServiceProperties) {
		this.planServiceProperties = planServiceProperties;
	}

	public PlanServiceProperties getPlanServiceProperties() {
		return planServiceProperties;
	}
}
