package org.opencdmp.model.censorship.descriptiontemplate;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.descriptiontemplate.Section;
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

@Component("descriptiontemplatedefinitionsectioncensor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SectionCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(SectionCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public SectionCensor(ConventionService conventionService,
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

        this.authService.authorizeForce(Permission.BrowseDescriptionTemplate);
        FieldSet fieldsFields = fields.extractPrefixed(this.asIndexerPrefix(Section._fieldSets));
        this.censorFactory.censor(FieldSetCensor.class).censor(fieldsFields, userId);
    }

}
