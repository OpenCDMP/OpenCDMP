package org.opencdmp.query;

import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.UserDescriptionTemplateEntity;
import org.opencdmp.model.UserDescriptionTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDescriptionTemplateQuery extends QueryBase<UserDescriptionTemplateEntity> {

    private Collection<UUID> ids;
    private Collection<IsActive> isActives;
    private Collection<UserDescriptionTemplateRole> roles;
    private Collection<UUID> excludedIds;
    private Collection<UUID> userIds;
    private Collection<UUID> descriptionTemplateIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public UserDescriptionTemplateQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UserDescriptionTemplateQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UserDescriptionTemplateQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public UserDescriptionTemplateQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public UserDescriptionTemplateQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public UserDescriptionTemplateQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public UserDescriptionTemplateQuery descriptionTemplateIds(UUID value) {
        this.descriptionTemplateIds = List.of(value);
        return this;
    }

    public UserDescriptionTemplateQuery descriptionTemplateIds(UUID... value) {
        this.descriptionTemplateIds = Arrays.asList(value);
        return this;
    }

    public UserDescriptionTemplateQuery descriptionTemplateIds(Collection<UUID> values) {
        this.descriptionTemplateIds = values;
        return this;
    }

    public UserDescriptionTemplateQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public UserDescriptionTemplateQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public UserDescriptionTemplateQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public UserDescriptionTemplateQuery roles(UserDescriptionTemplateRole value) {
        this.roles = List.of(value);
        return this;
    }

    public UserDescriptionTemplateQuery roles(UserDescriptionTemplateRole... value) {
        this.roles = Arrays.asList(value);
        return this;
    }

    public UserDescriptionTemplateQuery roles(Collection<UserDescriptionTemplateRole> values) {
        this.roles = values;
        return this;
    }

    public UserDescriptionTemplateQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UserDescriptionTemplateQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UserDescriptionTemplateQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UserDescriptionTemplateQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UserDescriptionTemplateQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public UserDescriptionTemplateQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }


    private final TenantEntityManager tenantEntityManager;
    public UserDescriptionTemplateQuery(
		    TenantEntityManager tenantEntityManager) {
	    this.tenantEntityManager = tenantEntityManager;
    }

    @Override
    protected Class<UserDescriptionTemplateEntity> entityClass() {
        return UserDescriptionTemplateEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.userIds) || this.isEmpty(this.descriptionTemplateIds) ||this.isEmpty(this.isActives) ||this.isEmpty(this.roles) || this.isEmpty(this.excludedIds);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserDescriptionTemplateEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserDescriptionTemplateEntity._userId));
            for (UUID item : this.userIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.descriptionTemplateIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserDescriptionTemplateEntity._descriptionTemplateId));
            for (UUID item : this.descriptionTemplateIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserDescriptionTemplateEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.roles != null) {
            CriteriaBuilder.In<UserDescriptionTemplateRole> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserDescriptionTemplateEntity._role));
            for (UserDescriptionTemplateRole item : this.roles)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserDescriptionTemplateEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected UserDescriptionTemplateEntity convert(Tuple tuple, Set<String> columns) {
        UserDescriptionTemplateEntity item = new UserDescriptionTemplateEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._tenantId, UUID.class));
        item.setUserId(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._userId, UUID.class));
        item.setDescriptionTemplateId(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._descriptionTemplateId, UUID.class));
        item.setRole(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._role, UserDescriptionTemplateRole.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, UserDescriptionTemplateEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(UserDescriptionTemplate._id)) return UserDescriptionTemplateEntity._id;
        else if (item.prefix(UserDescriptionTemplate._user)) return UserDescriptionTemplateEntity._userId;
        else if (item.match(UserDescriptionTemplate._user)) return UserDescriptionTemplateEntity._userId;
        else if (item.prefix(UserDescriptionTemplate._descriptionTemplate)) return UserDescriptionTemplateEntity._descriptionTemplateId;
        else if (item.match(UserDescriptionTemplate._descriptionTemplate)) return UserDescriptionTemplateEntity._descriptionTemplateId;
        else if (item.match(UserDescriptionTemplate._role)) return UserDescriptionTemplateEntity._role;
        else if (item.match(UserDescriptionTemplate._createdAt)) return UserDescriptionTemplateEntity._createdAt;
        else if (item.match(UserDescriptionTemplate._updatedAt)) return UserDescriptionTemplateEntity._updatedAt;
        else if (item.match(UserDescriptionTemplate._hash)) return UserDescriptionTemplateEntity._updatedAt;
        else if (item.match(UserDescriptionTemplate._isActive)) return UserDescriptionTemplateEntity._isActive;
        else if (item.match(UserDescriptionTemplate._belongsToCurrentTenant)) return UserDescriptionTemplateEntity._tenantId;
        else return null;
    }

}
