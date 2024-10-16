package org.opencdmp.model.censorship.prefillingsource;

import org.opencdmp.authorization.OwnedResource;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.censorship.BaseCensor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrefillingCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PrefillingCensor.class));

    protected final AuthorizationService authService;


    public PrefillingCensor(ConventionService conventionService, AuthorizationService authService) {
        super(conventionService);
        this.authService = authService;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeAtLeastOneForce(userId != null ? List.of(new OwnedResource(userId)) : null, Permission.BrowsePrefilling);
    }

}
