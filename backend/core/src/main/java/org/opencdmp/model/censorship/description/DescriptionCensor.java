package org.opencdmp.model.censorship.description;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.*;
import org.opencdmp.model.censorship.descriptionreference.DescriptionReferenceCensor;
import org.opencdmp.model.censorship.descriptiontemplate.DescriptionTemplateCensor;
import org.opencdmp.model.censorship.plan.PlanCensor;
import org.opencdmp.model.description.Description;
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
public class DescriptionCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public DescriptionCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseDescription, Permission.DeferredAffiliation);

        FieldSet descriptionReferenceFields = fields.extractPrefixed(this.asIndexerPrefix(Description._descriptionReferences));
        this.censorFactory.censor(DescriptionReferenceCensor.class).censor(descriptionReferenceFields, userId);

        FieldSet descriptionTagsFields = fields.extractPrefixed(this.asIndexerPrefix(Description._descriptionTags));
        this.censorFactory.censor(DescriptionTagCensor.class).censor(descriptionTagsFields, userId);

        FieldSet planDescriptionTemplateFields = fields.extractPrefixed(this.asIndexerPrefix(Description._planDescriptionTemplate));
        this.censorFactory.censor(PlanDescriptionTemplateCensor.class).censor(planDescriptionTemplateFields, userId);

        FieldSet descriptionTemplateFields = fields.extractPrefixed(this.asIndexerPrefix(Description._descriptionTemplate));
        this.censorFactory.censor(DescriptionTemplateCensor.class).censor(descriptionTemplateFields, userId);

        FieldSet planFields = fields.extractPrefixed(this.asIndexerPrefix(Description._plan));
        this.censorFactory.censor(PlanCensor.class).censor(planFields, userId);

        FieldSet propertiesFields = fields.extractPrefixed(this.asIndexerPrefix(Description._properties));
        this.censorFactory.censor(PropertyDefinitionCensor.class).censor(propertiesFields, userId);

        FieldSet createdByFields = fields.extractPrefixed(this.asIndexerPrefix(Description._createdBy));
        this.censorFactory.censor(UserCensor.class).censor(createdByFields, userId);
    }

}
