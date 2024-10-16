package org.opencdmp.model.censorship.descriptiontemplate;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.descriptiontemplate.Field;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("descriptiontemplatedefinitionfieldcensor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FieldCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FieldCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public FieldCensor(ConventionService conventionService,
                       AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseDescriptionTemplate);

        FieldSet visibilityRulesFields = fields.extractPrefixed(this.asIndexerPrefix(Field._visibilityRules));
        this.censorFactory.censor(RuleCensor.class).censor(visibilityRulesFields, userId);

        FieldSet defaultValueFields = fields.extractPrefixed(this.asIndexerPrefix(Field._defaultValue));
        this.censorFactory.censor(DefaultValueCensor.class).censor(defaultValueFields, userId);
        
        FieldSet fieldFields = fields.extractPrefixed(this.asIndexerPrefix(Field._data));
        this.censorFactory.censor(FieldDataCensor.class).censor(fieldFields, userId);
    }

}
