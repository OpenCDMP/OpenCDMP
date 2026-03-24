package org.opencdmp.service.websocket;

import org.opencdmp.convention.ConventionService;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.UUID;

@Component
public class StompEndpointHelper {
	public static final String SubscriptionGetPlanUsers = "/topic/plan/{id}/users";
	private final PathMatcher pathMatcher;
	private final ConventionService conventionService;

	public StompEndpointHelper(ConventionService conventionService) {
		this.conventionService = conventionService;
		pathMatcher = new AntPathMatcher();
	}
	
	public boolean isSubscriptionGetPlanUsers(String destination){
		return pathMatcher.match(SubscriptionGetPlanUsers, destination);
	}
	
	public String buildSubscriptionGetPlanUsers(UUID planId){
		return SubscriptionGetPlanUsers.replace("{id}", planId.toString());
	}

	public UUID extractSubscriptionGetPlanUsersPlanId(String destination){
		String id = pathMatcher.extractUriTemplateVariables(SubscriptionGetPlanUsers, destination).getOrDefault("id", "");
		
		return conventionService.isNullOrEmpty(id) ? null : UUID.fromString(id);
	}
}
