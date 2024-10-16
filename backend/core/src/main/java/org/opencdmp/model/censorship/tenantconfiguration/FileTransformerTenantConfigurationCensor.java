package org.opencdmp.model.censorship.tenantconfiguration;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.censorship.filetransformer.FileTransformerSourceCensor;
import org.opencdmp.model.tenantconfiguration.FileTransformerTenantConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FileTransformerTenantConfigurationCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FileTransformerTenantConfigurationCensor.class));

    protected final AuthorizationService authService;

    protected final CensorFactory censorFactory;


    public FileTransformerTenantConfigurationCensor(ConventionService conventionService, AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
	    this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;
        this.authService.authorizeForce(Permission.BrowseTenantConfiguration);

        FieldSet sourcesFields = fields.extractPrefixed(this.asIndexerPrefix(FileTransformerTenantConfiguration._sources));
        this.censorFactory.censor(FileTransformerSourceCensor.class).censor(sourcesFields, userId);
    }

}
