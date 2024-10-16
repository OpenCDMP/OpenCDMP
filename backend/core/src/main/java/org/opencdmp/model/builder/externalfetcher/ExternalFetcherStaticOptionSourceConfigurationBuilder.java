package org.opencdmp.model.builder.externalfetcher;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherStaticOptionSourceConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.externalfetcher.ExternalFetcherStaticOptionSourceConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExternalFetcherStaticOptionSourceConfigurationBuilder extends ExternalFetcherBaseSourceConfigurationBuilder<ExternalFetcherStaticOptionSourceConfiguration, ExternalFetcherStaticOptionSourceConfigurationEntity> {

    private final BuilderFactory builderFactory;
    private final EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final AuthorizationService authorizationService;

    @Autowired
    public ExternalFetcherStaticOptionSourceConfigurationBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, AuthorizationService authorizationService, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ExternalFetcherStaticOptionSourceConfigurationBuilder.class)), builderFactory, queryFactory);
        this.builderFactory = builderFactory;
	    this.authorizationService = authorizationService;
    }


    @Override
    protected ExternalFetcherStaticOptionSourceConfiguration getInstance() {
        return new ExternalFetcherStaticOptionSourceConfiguration();
    }

    @Override
    protected ExternalFetcherStaticOptionSourceConfiguration buildChild(FieldSet fields, ExternalFetcherStaticOptionSourceConfigurationEntity d, ExternalFetcherStaticOptionSourceConfiguration m) {
        FieldSet itemsFields = fields.extractPrefixed(this.asPrefix(ExternalFetcherStaticOptionSourceConfiguration._items));

        if(!this.authorizationService.authorize(Permission.EditReferenceType)) return m;
        
        if (!itemsFields.isEmpty() && d.getItems() != null) {
            m.setItems(this.builderFactory.builder(StaticBuilder.class).authorize(this.authorize).build(itemsFields, d.getItems()));
        }

        return m;
    }

}
