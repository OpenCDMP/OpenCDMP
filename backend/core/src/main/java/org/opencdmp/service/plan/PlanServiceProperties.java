package org.opencdmp.service.plan;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plan")
public class PlanServiceProperties {

	private int maxPlanPerRequest;

	private String rdaTransformerId;

	public int getMaxPlanPerRequest() {
		return maxPlanPerRequest;
	}

	public void setMaxPlanPerRequest(int maxPlanPerRequest) {
		this.maxPlanPerRequest = maxPlanPerRequest;
	}

	public String getRdaTransformerId() {
		return rdaTransformerId;
	}

	public void setRdaTransformerId(String rdaTransformerId) {
		this.rdaTransformerId = rdaTransformerId;
	}
}
