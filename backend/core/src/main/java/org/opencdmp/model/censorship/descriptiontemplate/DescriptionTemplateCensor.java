package org.opencdmp.model.censorship.descriptiontemplate;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.censorship.DescriptionTemplateTypeCensor;
import org.opencdmp.model.censorship.UserDescriptionTemplateCensor;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
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
public class DescriptionTemplateCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public DescriptionTemplateCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseDescriptionTemplate);
        FieldSet definitionFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionTemplate._definition));
        this.censorFactory.censor(DescriptionTemplateTypeCensor.class).censor(definitionFields, userId);
        FieldSet typeFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionTemplate._type));
        this.censorFactory.censor(DefinitionCensor.class).censor(typeFields, userId);
        FieldSet _usersFields = fields.extractPrefixed(this.asIndexerPrefix(DescriptionTemplate._users));
        this.censorFactory.censor(UserDescriptionTemplateCensor.class).censor(_usersFields, userId);
    }

}
