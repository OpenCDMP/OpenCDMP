package org.opencdmp.model.censorship.referencetype;

import org.opencdmp.authorization.Permission;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.censorship.BaseCensor;
import org.opencdmp.model.externalfetcher.ExternalFetcherBaseSourceConfiguration;
import org.opencdmp.model.externalfetcher.ExternalFetcherApiSourceConfiguration;
import org.opencdmp.model.externalfetcher.ExternalFetcherStaticOptionSourceConfiguration;
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
public class ReferenceTypeSourceBaseConfigurationCensor extends BaseCensor {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceTypeSourceBaseConfigurationCensor.class));

    protected final AuthorizationService authService;
    protected final CensorFactory censorFactory;

    public ReferenceTypeSourceBaseConfigurationCensor(ConventionService conventionService,
                                                      AuthorizationService authService, CensorFactory censorFactory) {
        super(conventionService);
        this.authService = authService;
        this.censorFactory = censorFactory;
    }

    public void censor(FieldSet fields, UUID userId) {
        logger.debug(new DataLogEntry("censoring fields", fields));
        if (fields == null || fields.isEmpty())
            return;

        this.authService.authorizeForce(Permission.BrowseReferenceType);
        FieldSet referenceTypeDependencyFields = fields.extractPrefixed(this.asIndexerPrefix(ExternalFetcherBaseSourceConfiguration._referenceTypeDependencies));
        this.censorFactory.censor(ReferenceTypeCensor.class).censor(referenceTypeDependencyFields, userId);

        FieldSet optionsFields = fields.extractPrefixed(this.asIndexerPrefix(ExternalFetcherStaticOptionSourceConfiguration._items));
        this.censorFactory.censor(ReferenceTypeStaticOptionCensor.class).censor(optionsFields, userId);

        FieldSet authFields = fields.extractPrefixed(this.asIndexerPrefix(ExternalFetcherApiSourceConfiguration._auth));
        this.censorFactory.censor(AuthenticationConfigurationCensor.class).censor(authFields, userId);

        FieldSet resultsFields = fields.extractPrefixed(this.asIndexerPrefix(ExternalFetcherApiSourceConfiguration._results));
        this.censorFactory.censor(ResultsConfigurationCensor.class).censor(resultsFields, userId);

        FieldSet queriesFields = fields.extractPrefixed(this.asIndexerPrefix(ExternalFetcherApiSourceConfiguration._queries));
        this.censorFactory.censor(QueryConfigCensor.class).censor(queriesFields, userId);
    }

}


