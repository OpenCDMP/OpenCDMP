package org.opencdmp.model.censorship.descriptionstatus;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinition;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionStatusDefinitionCensor extends BaseCensor {

    protected final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionStatusDefinitionCensor.class));
    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public DescriptionStatusDefinitionCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        this.logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseDescriptionStatus);

        FieldSet authorizationFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionStatusDefinition._authorization));
        this.censorFactory.censor(DescriptionStatusDefinitionAuthorizationCensor.class).censor(authorizationFields, userId);
    }
}
