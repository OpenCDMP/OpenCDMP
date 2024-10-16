package org.opencdmp.model.builder.externalfetcher;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherApiSourceConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.externalfetcher.ExternalFetcherApiSourceConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExternalFetcherApiSourceConfigurationBuilder extends ExternalFetcherBaseSourceConfigurationBuilder<ExternalFetcherApiSourceConfiguration, ExternalFetcherApiSourceConfigurationEntity> {

    private final BuilderFactory builderFactory;
    private final EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final AuthorizationService authorizationService;

    @Autowired
    public ExternalFetcherApiSourceConfigurationBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, AuthorizationService authorizationService, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ExternalFetcherApiSourceConfigurationBuilder.class)), builderFactory, queryFactory);
        this.builderFactory = builderFactory;
	    this.authorizationService = authorizationService;
    }


    @Override
    protected ExternalFetcherApiSourceConfiguration getInstance() {
        return new ExternalFetcherApiSourceConfiguration();
    }

    @Override
    protected ExternalFetcherApiSourceConfiguration buildChild(FieldSet fields, ExternalFetcherApiSourceConfigurationEntity d, ExternalFetcherApiSourceConfiguration m) {
        FieldSet resultsFields = fields.extractPrefixed(this.asPrefix(ExternalFetcherApiSourceConfiguration._results));
        FieldSet authFields = fields.extractPrefixed(this.asPrefix(ExternalFetcherApiSourceConfiguration._auth));
        FieldSet queriesFields = fields.extractPrefixed(this.asPrefix(ExternalFetcherApiSourceConfiguration._queries));
        if(!this.authorizationService.authorize(Permission.EditReferenceType)) return m;
        
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._url))) m.setUrl(d.getUrl());
        if (!resultsFields.isEmpty() && d.getResults() != null) {
            m.setResults(this.builderFactory.builder(ResultsConfigurationBuilder.class).authorize(this.authorize).build(resultsFields, d.getResults()));
        }
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._type))) m.setType(d.getType());
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._paginationPath))) m.setPaginationPath(d.getPaginationPath());
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._contentType))) m.setContentType(d.getContentType());
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._firstPage))) m.setFirstPage(d.getFirstPage());
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._httpMethod))) m.setHttpMethod(d.getHttpMethod());
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._requestBody))) m.setRequestBody(d.getRequestBody());
        if (fields.hasField(this.asIndexer(ExternalFetcherApiSourceConfiguration._filterType))) m.setFilterType(d.getFilterType());
        if (!authFields.isEmpty() && d.getAuth() != null) {
            m.setAuth(this.builderFactory.builder(AuthenticationConfigurationBuilder.class).authorize(this.authorize).build(authFields, d.getAuth()));
        }
        if (!queriesFields.isEmpty() && d.getQueries() != null) {
            m.setQueries(this.builderFactory.builder(QueryConfigBuilder.class).authorize(this.authorize).build(queriesFields, d.getQueries()));
        }

        return m;
    }
}
