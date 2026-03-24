package org.opencdmp.model.censorship;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;

import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDescriptionReferenceCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PublicDescriptionReferenceCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public PublicDescriptionReferenceCensor(ConventionService conventionService,
                                            AuthorizationService authService,
                                            CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.PublicBrowseDescription);
        FieldSet descriptionFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionReference._description));
        this.censorFactory.censor(PublicDescriptionCensor.class).censor(descriptionFields);
        FieldSet referenceFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionReference._reference));
        this.censorFactory.censor(PublicReferenceCensor.class).censor(referenceFields);
    }

}
