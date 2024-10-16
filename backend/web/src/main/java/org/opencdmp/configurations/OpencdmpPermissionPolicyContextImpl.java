package org.opencdmp.configurations;

import gr.cite.commons.web.authz.configuration.AuthorizationConfiguration;
import gr.cite.commons.web.authz.configuration.PermissionPolicyContextImpl;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class OpencdmpPermissionPolicyContextImpl extends PermissionPolicyContextImpl {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(OpencdmpPermissionPolicyContextImpl.class));

    public OpencdmpPermissionPolicyContextImpl(AuthorizationConfiguration authorizationConfiguration) {
        super(authorizationConfiguration);
    }


    @Override
    public void refresh(boolean force) {
        if (!force && this.policies != null) return;
        this.policies = this.authorizationConfiguration.getRawPolicies();
        this.extendedClaims = this.authorizationConfiguration.getRawExtendedClaims();
        logger.info("Authorization policies found: {}", this.policies.size());
        this.reload();
    }

}
