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
import org.opencdmp.data.UserDescriptionTemplateEntity;
import org.opencdmp.model.UserDescriptionTemplate;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.user.User;
import org.opencdmp.query.DescriptionTemplateQuery;
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
public class UserDescriptionTemplateBuilder extends BaseBuilder<UserDescriptionTemplate, UserDescriptionTemplateEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final TenantScope tenantScope;
    
    @Autowired
    public UserDescriptionTemplateBuilder(
		    ConventionService conventionService, QueryFactory queryFactory, BuilderFactory builderFactory, TenantScope tenantScope) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UserDescriptionTemplateBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
	    this.tenantScope = tenantScope;
    }

    public UserDescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<UserDescriptionTemplate> build(FieldSet fields, List<UserDescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet descriptionTemplateFields = fields.extractPrefixed(this.asPrefix(UserDescriptionTemplate._descriptionTemplate));
        Map<UUID, DescriptionTemplate> descriptionTemplateMap = this.collectDescriptionTemplates(descriptionTemplateFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(UserDescriptionTemplate._user));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        List<UserDescriptionTemplate> models = new ArrayList<>();
        for (UserDescriptionTemplateEntity d : data) {
            UserDescriptionTemplate m = new UserDescriptionTemplate();
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._id)))  m.setId(d.getId());
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._role))) m.setRole(d.getRole());
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(UserDescriptionTemplate._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!descriptionTemplateFields.isEmpty() && descriptionTemplateMap != null && descriptionTemplateMap.containsKey(d.getDescriptionTemplateId())) m.setDescriptionTemplate(descriptionTemplateMap.get(d.getDescriptionTemplateId()));
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<UserDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(UserDescriptionTemplateEntity::getUserId).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(UserDescriptionTemplateEntity::getUserId).distinct().collect(Collectors.toList()));
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

    private Map<UUID, DescriptionTemplate> collectDescriptionTemplates(FieldSet fields, List<UserDescriptionTemplateEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", DescriptionTemplate.class.getSimpleName());

        Map<UUID, DescriptionTemplate> itemMap = null;
        if (!fields.hasOtherField(this.asIndexer(DescriptionTemplate._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(UserDescriptionTemplateEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()),
                    x -> {
                        DescriptionTemplate item = new DescriptionTemplate();
                        item.setId(x);
                        return item;
                    },
                    x -> x.getId());
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(DescriptionTemplate._id);
            DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(data.stream().map(UserDescriptionTemplateEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionTemplateBuilder.class).asForeignKey(q, clone, DescriptionTemplate::getId);
        }
        if (!fields.hasField(DescriptionTemplate._id)) {
            itemMap.values().stream().filter(x -> x != null).map(x -> {
                x.setId(null);
                return x;
            }).collect(Collectors.toList());
        }

        return itemMap;
    }
}
