package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTagEntity;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.TagQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTagBuilder extends BaseBuilder<DescriptionTag, DescriptionTagEntity> {

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;
    private final TenantScope tenantScope;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionTagBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionTagBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
	    this.tenantScope = tenantScope;
    }

    public DescriptionTagBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionTag> build(FieldSet fields, List<DescriptionTagEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet tagFields = fields.extractPrefixed(this.asPrefix(DescriptionTag._tag));
        Map<UUID, Tag> tagItemsMap = this.collectTags(tagFields, data);

        FieldSet descriptionFields = fields.extractPrefixed(this.asPrefix(DescriptionTag._description));
        Map<UUID, Description> descriptionItemsMap = this.collectDescriptions(descriptionFields, data);

        List<DescriptionTag> models = new ArrayList<>();

        for (DescriptionTagEntity d : data) {
            DescriptionTag m = new DescriptionTag();
            if (fields.hasField(this.asIndexer(DescriptionTag._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(DescriptionTag._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(DescriptionTag._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(DescriptionTag._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(DescriptionTag._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(DescriptionTag._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!tagFields.isEmpty() && tagItemsMap != null && tagItemsMap.containsKey(d.getTagId())) m.setTag(tagItemsMap.get(d.getTagId()));
            if (!descriptionFields.isEmpty() && descriptionItemsMap != null && descriptionItemsMap.containsKey(d.getDescriptionId()))  m.setDescription(descriptionItemsMap.get(d.getDescriptionId()));

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, Description> collectDescriptions(FieldSet fields, List<DescriptionTagEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Description.class.getSimpleName());

        Map<UUID, Description> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Tag._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionTagEntity::getDescriptionId).distinct().collect(Collectors.toList()),
                    x -> {
                        Description item = new Description();
                        item.setId(x);
                        return item;
                    },
                    Description::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Tag._id);
            DescriptionQuery q = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionTagEntity::getDescriptionId).distinct().collect(Collectors.toList()));
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

    private Map<UUID, Tag> collectTags(FieldSet fields, List<DescriptionTagEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Tag.class.getSimpleName());

        Map<UUID, Tag> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Tag._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionTagEntity::getTagId).distinct().collect(Collectors.toList()),
                    x -> {
                        Tag item = new Tag();
                        item.setId(x);
                        return item;
                    },
                    Tag::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Tag._id);
            TagQuery q = this.queryFactory.query(TagQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionTagEntity::getTagId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(TagBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Tag::getId);
        }
        if (!fields.hasField(Tag._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

}
