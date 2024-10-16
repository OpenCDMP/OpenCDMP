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
import org.opencdmp.data.TagEntity;
import org.opencdmp.model.Tag;
import org.opencdmp.model.user.User;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TagBuilder extends BaseBuilder<Tag, TagEntity>{

    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public TagBuilder(
		    ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(TagBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
	    this.tenantScope = tenantScope;
    }

    public TagBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Tag> build(FieldSet fields, List<TagEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(Tag._createdBy));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        List<Tag> models = new ArrayList<>();
        for (TagEntity d : data) {
            Tag m = new Tag();
            if (fields.hasField(this.asIndexer(Tag._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Tag._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Tag._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(Tag._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Tag._isActive))) m.setIsActive(d.getIsActive());
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getCreatedById())) m.setCreatedBy(userItemsMap.get(d.getCreatedById()));
            if (fields.hasField(this.asIndexer(Tag._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<TagEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(TagEntity::getCreatedById).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(TagEntity::getCreatedById).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, User::getId);
        }
        if (!fields.hasField(User._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }
}
