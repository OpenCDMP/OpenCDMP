package org.opencdmp.model.censorship;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.DescriptionTag;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDescriptionTagCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PublicDescriptionTagCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;

    @Autowired
    public PublicDescriptionTagCensor(ConventionService conventionService,
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
        FieldSet descriptionFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionTag._description));
        this.censorFactory.censor(PublicDescriptionCensor.class).censor(descriptionFields);
        FieldSet tagFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionTag._tag));
        this.censorFactory.censor(PublicTagCensor.class).censor(tagFields);
    }

}
