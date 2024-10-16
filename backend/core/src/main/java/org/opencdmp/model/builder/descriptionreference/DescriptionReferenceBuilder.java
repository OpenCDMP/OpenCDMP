package org.opencdmp.model.builder.descriptionreference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.descriptionreference.DescriptionReferenceDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionReferenceEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.builder.reference.ReferenceBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.ReferenceQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionReferenceBuilder extends BaseBuilder<DescriptionReference, DescriptionReferenceEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final JsonHandlingService jsonHandlingService;
    private final TenantScope tenantScope;

    @Autowired
    public DescriptionReferenceBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, JsonHandlingService jsonHandlingService, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionReferenceBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
	    this.jsonHandlingService = jsonHandlingService;
	    this.tenantScope = tenantScope;
    }

    public DescriptionReferenceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionReference> build(FieldSet fields, List<DescriptionReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet referenceFields = fields.extractPrefixed(this.asPrefix(DescriptionReference._reference));
        Map<UUID, Reference> referenceItemsMap = this.collectReferences(referenceFields, data);

        FieldSet descriptionFields = fields.extractPrefixed(this.asPrefix(DescriptionReference._description));
        Map<UUID, Description> descriptionItemsMap = this.collectDescriptions(descriptionFields, data);
        
        FieldSet dataFields = fields.extractPrefixed(this.asPrefix(DescriptionReference._data));

        List<DescriptionReference> models = new ArrayList<>();
        for (DescriptionReferenceEntity d : data) {
            DescriptionReference m = new DescriptionReference();
            if (fields.hasField(this.asIndexer(DescriptionReference._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(DescriptionReference._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(DescriptionReference._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(DescriptionReference._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionReference._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(DescriptionReference._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!referenceFields.isEmpty() && referenceItemsMap != null && referenceItemsMap.containsKey(d.getReferenceId())) m.setReference(referenceItemsMap.get(d.getReferenceId()));
            if (!descriptionFields.isEmpty() && descriptionItemsMap != null && descriptionItemsMap.containsKey(d.getDescriptionId())) m.setDescription(descriptionItemsMap.get(d.getDescriptionId()));
            if (!dataFields.isEmpty() && d.getData() != null){
                DescriptionReferenceDataEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(DescriptionReferenceDataEntity.class, d.getData());
                m.setData(this.builderFactory.builder(DescriptionReferenceDataBuilder.class).authorize(this.authorize).build(dataFields, propertyDefinition));
            }
            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, Description> collectDescriptions(FieldSet fields, List<DescriptionReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Description.class.getSimpleName());

        Map<UUID, Description> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Reference._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionReferenceEntity::getDescriptionId).distinct().collect(Collectors.toList()),
                    x -> {
                        Description item = new Description();
                        item.setId(x);
                        return item;
                    },
                    Description::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Reference._id);
            DescriptionQuery q = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionReferenceEntity::getDescriptionId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Description::getId);
        }
        if (!fields.hasField(Description._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, Reference> collectReferences(FieldSet fields, List<DescriptionReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Reference.class.getSimpleName());

        Map<UUID, Reference> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Reference._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()),
                    x -> {
                        Reference item = new Reference();
                        item.setId(x);
                        return item;
                    },
                    Reference::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Reference._id);
            ReferenceQuery q = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionReferenceEntity::getReferenceId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(ReferenceBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Reference::getId);
        }
        if (!fields.hasField(Reference._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

}
