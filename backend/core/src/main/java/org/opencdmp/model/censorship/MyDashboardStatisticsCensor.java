package org.opencdmp.model.censorship;

import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyDashboardStatisticsCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(MyDashboardStatisticsCensor.class));

    protected final AuthorizationService authService;


    public MyDashboardStatisticsCensor(ConventionService conventionService, AuthorizationService authService) {
        super(conventionService);
        this.authService = authService;
    }

    public void censor(UUID userId) {
        this.authService.authorizeAtLeastOneForce(userId != null ? List.of(new OwnedResource(userId)) : null);
    }

}
