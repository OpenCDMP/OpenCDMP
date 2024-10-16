package org.opencdmp.model.builder.externalfetcher;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherBaseSourceConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.externalfetcher.ExternalFetcherBaseSourceConfiguration;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.ReferenceTypeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class ExternalFetcherBaseSourceConfigurationBuilder<Model extends ExternalFetcherBaseSourceConfiguration, Entity extends ExternalFetcherBaseSourceConfigurationEntity> extends BaseBuilder<Model, Entity> {

    protected EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    protected final BuilderFactory builderFactory;
    protected final QueryFactory queryFactory;

    @Autowired
    public ExternalFetcherBaseSourceConfigurationBuilder(
		    ConventionService conventionService,
		    LoggerService logger, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, logger);
        this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public ExternalFetcherBaseSourceConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }
    
    protected abstract Model getInstance();

    protected abstract Model buildChild(FieldSet fields, Entity data, Model model);
    @Override
    public List<Model> build(FieldSet fields, List<Entity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet referenceTypeDependenciesFields = fields.extractPrefixed(this.asPrefix(Model._referenceTypeDependencies));
        Map<UUID, ReferenceType> referenceTypeItemsMap = this.collectReferenceTypes(referenceTypeDependenciesFields, data);
        
        List<Model> models = new ArrayList<>();
        for (Entity d : data) {
            Model m = this.getInstance();
            if (fields.hasField(this.asIndexer(Model._key))) m.setKey(d.getKey());
            if (fields.hasField(this.asIndexer(Model._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Model._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Model._type))) m.setType(d.getType());
            if (!referenceTypeDependenciesFields.isEmpty() && referenceTypeItemsMap != null && d.getReferenceTypeDependencyIds() != null) {
                List<ReferenceType> referenceTypes = new ArrayList<>();
                for (UUID referenceTypeId : d.getReferenceTypeDependencyIds()){
                    if (referenceTypeItemsMap.containsKey(referenceTypeId)) referenceTypes.add(referenceTypeItemsMap.get(referenceTypeId));
                }
                if (!referenceTypes.isEmpty()) m.setReferenceTypeDependencies(referenceTypes);
            }

            this.buildChild(fields, d, m);
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, ReferenceType> collectReferenceTypes(FieldSet fields, List<Entity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", ReferenceType.class.getSimpleName());

        Map<UUID, ReferenceType> itemMap;
        if (!fields.hasOtherField(this.asIndexer(ReferenceType._id))) {
            itemMap = this.asEmpty(
                    data.stream().filter(x-> x.getReferenceTypeDependencyIds() != null).map(ExternalFetcherBaseSourceConfigurationEntity::getReferenceTypeDependencyIds).flatMap(List::stream).distinct().collect(Collectors.toList()),
                    x -> {
                        ReferenceType item = new ReferenceType();
                        item.setId(x);
                        return item;
                    },
                    ReferenceType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(ReferenceType._id);
            ReferenceTypeQuery q = this.queryFactory.query(ReferenceTypeQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().filter(x-> x.getReferenceTypeDependencyIds() != null).map(ExternalFetcherBaseSourceConfigurationEntity::getReferenceTypeDependencyIds).flatMap(List::stream).distinct().collect(Collectors.toList()));
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

