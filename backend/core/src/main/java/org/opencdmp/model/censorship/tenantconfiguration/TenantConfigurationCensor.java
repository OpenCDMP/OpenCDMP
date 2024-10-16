package org.opencdmp.model.censorship.tenantconfiguration;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantConfigurationCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantConfigurationCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;


    public TenantConfigurationCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
	    this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeForce(Permission.BrowseTenantConfiguration);

        FieldSet cssColorsFields = fields.extractPrefixed(this.asIndexerPrefix(TenantConfiguration._cssColors));
        this.censorFactory.censor(CssColorsTenantConfigurationCensor.class).censor(cssColorsFields, userId);

        FieldSet defaultUserLocaleFields = fields.extractPrefixed(this.asIndexerPrefix(TenantConfiguration._defaultUserLocale));
        this.censorFactory.censor(DefaultUserLocaleTenantConfigurationCensor.class).censor(defaultUserLocaleFields, userId);

        FieldSet depositPluginsFields = fields.extractPrefixed(this.asIndexerPrefix(TenantConfiguration._depositPlugins));
        this.censorFactory.censor(DepositTenantConfigurationCensor.class).censor(depositPluginsFields, userId);

        FieldSet fileTransformerPluginsFields = fields.extractPrefixed(this.asIndexerPrefix(TenantConfiguration._fileTransformerPlugins));
        this.censorFactory.censor(FileTransformerTenantConfigurationCensor.class).censor(fileTransformerPluginsFields, userId);

        FieldSet logoFields = fields.extractPrefixed(this.asIndexerPrefix(TenantConfiguration._logo));
        this.censorFactory.censor(LogoTenantConfigurationCensor.class).censor(logoFields, userId);
    }

}
