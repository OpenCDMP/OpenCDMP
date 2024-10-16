package org.opencdmp.model.censorship.reference;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.censorship.referencetype.ReferenceTypeCensor;
import org.opencdmp.model.censorship.UserCensor;
import org.opencdmp.model.censorship.planreference.PlanReferenceCensor;
import org.opencdmp.model.reference.Reference;
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
public class ReferenceCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public ReferenceCensor(ConventionService conventionService,
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

        this.authService.authorizeForce(Permission.BrowseReference, Permission.DeferredAffiliation);
        FieldSet definitionFields = fields.extractPrefixed(this.asIndexerPrefix(Reference._definition));
        this.censorFactory.censor(DefinitionCensor.class).censor(definitionFields, userId);
        FieldSet planReferencesFields = fields.extractPrefixed(this.asIndexerPrefix(Reference._planReferences));
        this.censorFactory.censor(PlanReferenceCensor.class).censor(planReferencesFields, userId);
        FieldSet createdByFields = fields.extractPrefixed(this.asIndexerPrefix(Reference._createdBy));
        this.censorFactory.censor(UserCensor.class).censor(createdByFields, userId);
        FieldSet typeFields = fields.extractPrefixed(this.asIndexerPrefix(Reference._type));
        this.censorFactory.censor(ReferenceTypeCensor.class).censor(typeFields, userId);
    }

}
