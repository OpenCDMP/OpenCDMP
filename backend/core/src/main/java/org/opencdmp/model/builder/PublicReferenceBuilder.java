package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.model.PublicReference;
import org.opencdmp.model.PublicReferenceType;
import org.opencdmp.model.reference.Reference;
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
public class PublicReferenceBuilder extends BaseBuilder<PublicReference, ReferenceEntity>{
    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PublicReferenceBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicReferenceBuilder.class)));
	    this.builderFactory = builderFactory;
	    this.queryFactory = queryFactory;
    }

    public PublicReferenceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicReference> build(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();


        FieldSet typeFields = fields.extractPrefixed(this.asPrefix(PublicReference._type));
        Map<UUID, PublicReferenceType> typeItemsMap = this.collectReferenceTypes(typeFields, data);
        
        List<PublicReference> models = new ArrayList<>();
        for (ReferenceEntity d : data) {
            PublicReference m = new PublicReference();
            if (fields.hasField(this.asIndexer(PublicReference._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicReference._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(PublicReference._reference))) m.setReference(d.getReference());
            if (fields.hasField(this.asIndexer(PublicReference._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(PublicReference._source))) m.setSource(d.getSource());

            if (!typeFields.isEmpty() && typeItemsMap != null && typeItemsMap.containsKey(d.getTypeId())) m.setType(typeItemsMap.get(d.getTypeId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }



    private Map<UUID, PublicReferenceType> collectReferenceTypes(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PublicReferenceType.class.getSimpleName());

        Map<UUID, PublicReferenceType> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PublicReferenceType._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()),
                    x -> {
                        PublicReferenceType item = new PublicReferenceType();
                        item.setId(x);
                        return item;
                    },
                    PublicReferenceType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PublicReferenceType._id);
            ReferenceTypeQuery q = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().authorize(this.authorize).isActive(IsActive.Active).ids(data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PublicReferenceTypeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PublicReferenceType::getId);
        }
        if (!fields.hasField(PublicReferenceType._id)) {
            itemMap.values().stream().filter(Objects::nonNull).forEach(x -> x.setId(null));
        }

        return itemMap;
    }
}
