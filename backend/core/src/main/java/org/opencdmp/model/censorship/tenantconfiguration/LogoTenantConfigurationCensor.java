package org.opencdmp.model.censorship.tenantconfiguration;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LogoTenantConfigurationCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(LogoTenantConfigurationCensor.class));

    protected final AuthorizationService authService;


    public LogoTenantConfigurationCensor(ConventionService conventionService, AuthorizationService authService) {
        super(conventionService);
        this.authService = authService;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeForce(Permission.BrowseTenantConfiguration);
    }

}
