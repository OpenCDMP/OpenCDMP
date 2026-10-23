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
import org.opencdmp.commons.enums.UserPlanBlueprintRole;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.UserPlanBlueprintEntity;
import org.opencdmp.model.UserPlanBlueprint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserPlanBlueprintQuery extends QueryBase<UserPlanBlueprintEntity> {

    private Collection<UUID> ids;
    private Collection<IsActive> isActives;
    private Collection<UserPlanBlueprintRole> roles;
    private Collection<UUID> excludedIds;
    private Collection<UUID> userIds;
    private Collection<UUID> planBlueprintIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public UserPlanBlueprintQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UserPlanBlueprintQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UserPlanBlueprintQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public UserPlanBlueprintQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public UserPlanBlueprintQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public UserPlanBlueprintQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public UserPlanBlueprintQuery planBlueprintIds(UUID value) {
        this.planBlueprintIds = List.of(value);
        return this;
    }

    public UserPlanBlueprintQuery planBlueprintIds(UUID... value) {
        this.planBlueprintIds = Arrays.asList(value);
        return this;
    }

    public UserPlanBlueprintQuery planBlueprintIds(Collection<UUID> values) {
        this.planBlueprintIds = values;
        return this;
    }

    public UserPlanBlueprintQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public UserPlanBlueprintQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public UserPlanBlueprintQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public UserPlanBlueprintQuery roles(UserPlanBlueprintRole value) {
        this.roles = List.of(value);
        return this;
    }

    public UserPlanBlueprintQuery roles(UserPlanBlueprintRole... value) {
        this.roles = Arrays.asList(value);
        return this;
    }

    public UserPlanBlueprintQuery roles(Collection<UserPlanBlueprintRole> values) {
        this.roles = values;
        return this;
    }

    public UserPlanBlueprintQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UserPlanBlueprintQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UserPlanBlueprintQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UserPlanBlueprintQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UserPlanBlueprintQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public UserPlanBlueprintQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }


    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    public UserPlanBlueprintQuery(
		    TenantEntityManagerFactory tenantEntityManagerFactory) {
	    this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    protected Class<UserPlanBlueprintEntity> entityClass() {
        return UserPlanBlueprintEntity.class;
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.userIds) || this.isEmpty(this.planBlueprintIds) ||this.isEmpty(this.isActives) ||this.isEmpty(this.roles) || this.isEmpty(this.excludedIds);
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserPlanBlueprintEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserPlanBlueprintEntity._userId));
            for (UUID item : this.userIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.planBlueprintIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserPlanBlueprintEntity._planBlueprintId));
            for (UUID item : this.planBlueprintIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserPlanBlueprintEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.roles != null) {
            CriteriaBuilder.In<UserPlanBlueprintRole> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserPlanBlueprintEntity._role));
            for (UserPlanBlueprintRole item : this.roles)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserPlanBlueprintEntity._id));
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
    protected UserPlanBlueprintEntity convert(Tuple tuple, Set<String> columns) {
        UserPlanBlueprintEntity item = new UserPlanBlueprintEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._tenantId, UUID.class));
        item.setUserId(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._userId, UUID.class));
        item.setPlanBlueprintId(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._planBlueprintId, UUID.class));
        item.setRole(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._role, UserPlanBlueprintRole.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, UserPlanBlueprintEntity._isActive, IsActive.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(UserPlanBlueprint._id)) return UserPlanBlueprintEntity._id;
        else if (item.prefix(UserPlanBlueprint._user)) return UserPlanBlueprintEntity._userId;
        else if (item.match(UserPlanBlueprint._user)) return UserPlanBlueprintEntity._userId;
        else if (item.prefix(UserPlanBlueprint._planBlueprint)) return UserPlanBlueprintEntity._planBlueprintId;
        else if (item.match(UserPlanBlueprint._planBlueprint)) return UserPlanBlueprintEntity._planBlueprintId;
        else if (item.match(UserPlanBlueprint._role)) return UserPlanBlueprintEntity._role;
        else if (item.match(UserPlanBlueprint._createdAt)) return UserPlanBlueprintEntity._createdAt;
        else if (item.match(UserPlanBlueprint._updatedAt)) return UserPlanBlueprintEntity._updatedAt;
        else if (item.match(UserPlanBlueprint._hash)) return UserPlanBlueprintEntity._updatedAt;
        else if (item.match(UserPlanBlueprint._isActive)) return UserPlanBlueprintEntity._isActive;
        else if (item.match(UserPlanBlueprint._belongsToCurrentTenant)) return UserPlanBlueprintEntity._tenantId;
        else return null;
    }

}
