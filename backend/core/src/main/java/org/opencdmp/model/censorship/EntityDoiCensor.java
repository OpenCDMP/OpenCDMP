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
public class EntityDoiCensor extends BaseCensor{

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EntityDoiCensor.class));

    protected final AuthorizationService authService;

    public EntityDoiCensor(ConventionService conventionService, AuthorizationService authService) {
        super(conventionService);
        this.authService = authService;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseEntityDoi, Permission.DeferredAffiliation);
    }
}
