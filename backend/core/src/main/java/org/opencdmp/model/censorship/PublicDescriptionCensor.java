package org.opencdmp.model.censorship;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.description.Description;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDescriptionCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PublicDescriptionCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    public PublicDescriptionCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.PublicBrowseDescription);

        FieldSet planDescriptionTemplateFields = fields.extractPrefixed(this.asIndexerPrefix(Description._planDescriptionTemplate));
        this.censorFactory.censor(PublicPlanDescriptionTemplateCensor.class).censor(planDescriptionTemplateFields);

        FieldSet descriptionTemplateFields = fields.extractPrefixed(this.asIndexerPrefix(Description._descriptionTemplate));
        this.censorFactory.censor(PublicDescriptionTemplateCensor.class).censor(descriptionTemplateFields);

        FieldSet descriptionReferenceFields = fields.extractPrefixed(this.asIndexerPrefix(Description._descriptionReferences));
        this.censorFactory.censor(PublicDescriptionReferenceCensor.class).censor(descriptionReferenceFields);

        FieldSet tagFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionTag._tag));
        this.censorFactory.censor(PublicTagCensor.class).censor(tagFields);

        FieldSet planFields = fields.extractPrefixed(this.asIndexerPrefix(Description._plan));
        this.censorFactory.censor(PublicPlanCensor.class).censor(planFields);
    }

}
