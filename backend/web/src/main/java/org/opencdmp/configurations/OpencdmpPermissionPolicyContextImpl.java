package org.opencdmp.configurations;

import gr.cite.commons.web.authz.configuration.AuthorizationConfiguration;
import gr.cite.commons.web.authz.configuration.PermissionPolicyContextImpl;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.event.DescriptionStatusTouchedEvent;
import org.opencdmp.event.PlanStatusTouchedEvent;
import org.opencdmp.service.custompolicy.CustomPolicyCacheService;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Service
public class OpencdmpPermissionPolicyContextImpl extends PermissionPolicyContextImpl {

	private final CustomPolicyService customPolicyService;
	private final CustomPolicyCacheService customPolicyCacheService;
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(OpencdmpPermissionPolicyContextImpl.class));

	public OpencdmpPermissionPolicyContextImpl(AuthorizationConfiguration authorizationConfiguration, CustomPolicyService customPolicyService, CustomPolicyCacheService customPolicyCacheService) {
		super(authorizationConfiguration);
        this.customPolicyService = customPolicyService;
        this.customPolicyCacheService = customPolicyCacheService;
    }

	@EventListener
	public void handlePlanTouchedEvent(PlanStatusTouchedEvent event) {
		this.customPolicyCacheService.clearCache(event);
		this.refresh(true);
	}

	@EventListener
	public void handleDescriptionStatusTouchedEvent(DescriptionStatusTouchedEvent event) {
		this.customPolicyCacheService.clearCache(event);
		this.refresh(true);
	}

	@Override
	public void refresh(boolean force) {
		if (!force && this.policies != null) return;
		this.policies = this.authorizationConfiguration.getRawPolicies();
		this.extendedClaims = this.authorizationConfiguration.getRawExtendedClaims();
		this.policies.putAll(this.customPolicyService.buildPlanStatusPolicies());
		this.policies.putAll(this.customPolicyService.buildDescriptionStatusPolicies());
		logger.info("Authorization policies found: {}", this.policies.size());
		this.reload();
	}
}
