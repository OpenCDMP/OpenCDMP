package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.UserRoleEntity;
import org.opencdmp.model.UserRole;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserRoleQuery extends QueryBase<UserRoleEntity> {
    private Collection<UUID> ids;
    private Collection<UUID> excludedIds;
    private Collection<UUID> userIds;
    private Collection<String> roles;
    private Collection<UUID> tenantIds;
    private Boolean tenantIsSet;
    
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;
    private final AuthorizationService authService;
    private final TenantEntityManager tenantEntityManager;
    public UserRoleQuery(UserScope userScope, AuthorizationService authService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public UserRoleQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UserRoleQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UserRoleQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }
    
    public UserRoleQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UserRoleQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UserRoleQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UserRoleQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public UserRoleQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public UserRoleQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public UserRoleQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public UserRoleQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public UserRoleQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public UserRoleQuery tenantIsSet(Boolean value) {
        this.tenantIsSet = value;
        return this;
    }

    public UserRoleQuery roles(String value) {
        this.roles = List.of(value);
        return this;
    }

    public UserRoleQuery roles(String... value) {
        this.roles = Arrays.asList(value);
        return this;
    }

    public UserRoleQuery roles(Collection<String> values) {
        this.roles = values;
        return this;
    }

    public UserRoleQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public UserRoleQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UserRoleQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManager.getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return
                this.isEmpty(this.ids) ||
                        this.isEmpty(this.userIds) ||
                        this.isEmpty(this.roles) ||
                        this.isEmpty(this.tenantIds) ||
                        this.isEmpty(this.excludedIds);
    }

    @Override
    protected Class<UserRoleEntity> entityClass() {
        return UserRoleEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseUser)) return null;
        UUID userId = null;
        if (this.authorize.contains(AuthorizationFlags.Owner)) userId = this.userScope.getUserIdSafe();

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(UserRoleEntity._userId)).value(userId));
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return queryContext.CriteriaBuilder.or(); //Creates a false query
        }
    }
    
    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserRoleEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserRoleEntity._tenantId));
            for (UUID item : this.tenantIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(UserRoleEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(UserRoleEntity._tenantId)));
        }
        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserRoleEntity._userId));
            for (UUID item : this.userIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserRoleEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.roles != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserRoleEntity._role));
            for (String item : this.roles)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (!predicates.isEmpty()) {
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            return queryContext.CriteriaBuilder.and(predicatesArray);
        } else {
            return null;
        }
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(UserRole._id)) return UserRoleEntity._id;
        else if (item.match(UserRole._role)) return UserRoleEntity._role;
        else if (item.prefix(UserRole._user)) return UserRoleEntity._userId;
        else if (item.match(UserRole._user)) return UserRoleEntity._userId;
        else if (item.match(UserRole._createdAt) ) return UserRoleEntity._createdAt;
        else if (item.match(UserRoleEntity._tenantId) ) return UserRoleEntity._tenantId;
        else if (item.match(UserRole._belongsToCurrentTenant) ) return UserRoleEntity._tenantId;
        else return null;
    }

    @Override
    protected UserRoleEntity convert(Tuple tuple, Set<String> columns) {
        UserRoleEntity item = new UserRoleEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UserRoleEntity._id, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, UserRoleEntity._tenantId, UUID.class));
        item.setRole(QueryBase.convertSafe(tuple, columns, UserRoleEntity._role, String.class));
        item.setUserId(QueryBase.convertSafe(tuple, columns, UserRoleEntity._userId, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UserRoleEntity._createdAt, Instant.class));
        return item;
    }

}
