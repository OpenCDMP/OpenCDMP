package org.opencdmp.model.censorship;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicTagCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PublicTagCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public PublicTagCensor(ConventionService conventionService,
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
    }

}
