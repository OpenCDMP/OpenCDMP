package org.opencdmp.model.builder.externalfetcher;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.QueryCaseConfigEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.externalfetcher.QueryCaseConfig;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.ReferenceTypeQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QueryCaseConfigBuilder extends BaseBuilder<QueryCaseConfig, QueryCaseConfigEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public QueryCaseConfigBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(QueryCaseConfigBuilder.class)));
        this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public QueryCaseConfigBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<QueryCaseConfig> build(FieldSet fields, List<QueryCaseConfigEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet referenceTypeFields = fields.extractPrefixed(this.asPrefix(QueryCaseConfig._referenceType));
        Map<UUID, ReferenceType> referenceTypeItemsMap = this.collectReferenceTypes(referenceTypeFields, data);

        List<QueryCaseConfig> models = new ArrayList<>();
        for (QueryCaseConfigEntity d : data) {
            QueryCaseConfig m = new QueryCaseConfig();
            if (fields.hasField(this.asIndexer(QueryCaseConfig._separator))) m.setSeparator(d.getSeparator());
            if (fields.hasField(this.asIndexer(QueryCaseConfig._value))) m.setValue(d.getValue());
            if (fields.hasField(this.asIndexer(QueryCaseConfig._likePattern))) m.setLikePattern(d.getLikePattern());
            if (fields.hasField(this.asIndexer(QueryCaseConfig._referenceTypeSourceKey))) m.setReferenceTypeSourceKey(d.getReferenceTypeSourceKey());
            if (!referenceTypeFields.isEmpty() && referenceTypeItemsMap != null && referenceTypeItemsMap.containsKey(d.getReferenceTypeId())) m.setReferenceType(referenceTypeItemsMap.get(d.getReferenceTypeId()));

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, ReferenceType> collectReferenceTypes(FieldSet fields, List<QueryCaseConfigEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", ReferenceType.class.getSimpleName());

        Map<UUID, ReferenceType> itemMap;
        if (!fields.hasOtherField(this.asIndexer(ReferenceType._id))) {
            itemMap = this.asEmpty(
                    data.stream().filter(x-> x.getReferenceTypeId() != null).map(QueryCaseConfigEntity::getReferenceTypeId).distinct().collect(Collectors.toList()),
                    x -> {
                        ReferenceType item = new ReferenceType();
                        item.setId(x);
                        return item;
                    },
                    ReferenceType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(ReferenceType._id);
            ReferenceTypeQuery q = this.queryFactory.query(ReferenceTypeQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().filter(x-> x.getReferenceTypeId() != null).map(QueryCaseConfigEntity::getReferenceTypeId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, ReferenceType::getId);
        }
        if (!fields.hasField(ReferenceType._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }
}
