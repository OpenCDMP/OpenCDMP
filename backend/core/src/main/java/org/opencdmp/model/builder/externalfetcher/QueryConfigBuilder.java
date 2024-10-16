package org.opencdmp.model.builder.externalfetcher;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.QueryConfigEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.externalfetcher.QueryConfig;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QueryConfigBuilder extends BaseBuilder<QueryConfig, QueryConfigEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public QueryConfigBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(QueryConfigBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public QueryConfigBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<QueryConfig> build(FieldSet fields, List<QueryConfigEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet casesFields = fields.extractPrefixed(this.asPrefix(QueryConfig._cases));

        List<QueryConfig> models = new ArrayList<>();
        for (QueryConfigEntity d : data) {
            QueryConfig m = new QueryConfig();
            if (fields.hasField(this.asIndexer(QueryConfig._defaultValue))) m.setDefaultValue(d.getDefaultValue());
            if (fields.hasField(this.asIndexer(QueryConfig._name))) m.setName(d.getName());
            if (!casesFields.isEmpty() && !this.conventionService.isListNullOrEmpty(d.getCases())){
                m.setCases(this.builderFactory.builder(QueryCaseConfigBuilder.class).authorize(this.authorize).build(casesFields, d.getCases()));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

}
