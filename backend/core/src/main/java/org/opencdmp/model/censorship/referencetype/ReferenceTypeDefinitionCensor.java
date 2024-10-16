package org.opencdmp.model.censorship.referencetype;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.referencetype.ReferenceTypeDefinition;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
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
public class ReferenceTypeDefinitionCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceTypeDefinitionCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public ReferenceTypeDefinitionCensor(ConventionService conventionService,
                                         AuthorizationService authService,
                                         CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseReferenceType);
        FieldSet fieldsFields = fields.extractPrefixed(this.asIndexerPrefix(ReferenceTypeDefinition._fields));
        this.censorFactory.censor(ReferenceTypeFieldCensor.class).censor(fieldsFields, userId);

        FieldSet sourcesFields = fields.extractPrefixed(this.asIndexerPrefix(ReferenceTypeDefinition._sources));
        this.censorFactory.censor(ReferenceTypeSourceBaseConfigurationCensor.class).censor(sourcesFields, userId);
    }

}
