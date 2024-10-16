package org.opencdmp.model.censorship;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDashboardStatisticsCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PublicDashboardStatisticsCensor.class));

    protected final AuthorizationService authService;


    public PublicDashboardStatisticsCensor(ConventionService conventionService, AuthorizationService authService) {
        super(conventionService);
        this.authService = authService;
    }

    public void censor(UUID userId) {
        this.authService.authorizeForce(Permission.PublicBrowseDashboardStatistics);
    }

}
