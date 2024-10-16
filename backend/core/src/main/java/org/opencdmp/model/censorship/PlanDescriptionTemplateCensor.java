package org.opencdmp.model.censorship;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.PlanDescriptionTemplate;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.censorship.descriptiontemplate.DescriptionTemplateCensor;
import org.opencdmp.model.censorship.plan.PlanCensor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanDescriptionTemplateCensor extends BaseCensor{

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanDescriptionTemplateCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public PlanDescriptionTemplateCensor(ConventionService conventionService,
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

        this.authService.authorizeForce(Permission.BrowseDescription, Permission.DeferredAffiliation);
        FieldSet planFields = fields.extractPrefixed(this.asIndexerPrefix(PlanDescriptionTemplate._plan));
        this.censorFactory.censor(PlanCensor.class).censor(planFields, userId);
        FieldSet descriptionTemplatesFields = fields.extractPrefixed(this.asIndexerPrefix(PlanDescriptionTemplate._descriptionTemplates));
        this.censorFactory.censor(DescriptionTemplateCensor.class).censor(descriptionTemplatesFields, userId);
        FieldSet currentDescriptionTemplateFields = fields.extractPrefixed(this.asIndexerPrefix(PlanDescriptionTemplate._currentDescriptionTemplate));
        this.censorFactory.censor(DescriptionTemplateCensor.class).censor(currentDescriptionTemplateFields, userId);
    }

}
