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
import org.opencdmp.data.UserCredentialEntity;
import org.opencdmp.model.usercredential.UserCredential;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserCredentialQuery extends QueryBase<UserCredentialEntity> {
    private Collection<UUID> ids;
    private Collection<UUID> excludedIds;
    private Collection<UUID> userIds;
    private Collection<String> externalIds;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;
    private final AuthorizationService authService;
    private final TenantEntityManager tenantEntityManager;
    public UserCredentialQuery(UserScope userScope, AuthorizationService authService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public UserCredentialQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UserCredentialQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UserCredentialQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }
    
    public UserCredentialQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UserCredentialQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UserCredentialQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UserCredentialQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public UserCredentialQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public UserCredentialQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public UserCredentialQuery externalIds(String value) {
        this.externalIds = List.of(value);
        return this;
    }

    public UserCredentialQuery externalIds(String... value) {
        this.externalIds = Arrays.asList(value);
        return this;
    }

    public UserCredentialQuery externalIds(Collection<String> values) {
        this.externalIds = values;
        return this;
    }

    public UserCredentialQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UserCredentialQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public UserCredentialQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
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
                        this.isEmpty(this.externalIds) ||
                        this.isEmpty(this.excludedIds);
    }

    @Override
    protected Class<UserCredentialEntity> entityClass() {
        return UserCredentialEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseUser)) return null;
        UUID userId;
        if (this.authorize.contains(AuthorizationFlags.Owner)) userId = this.userScope.getUserIdSafe();
        else  userId = null;

        List<Predicate> predicates = new ArrayList<>();
        if (userId != null) {
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(UserCredentialEntity._userId)).value(userId));
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
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserCredentialEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserCredentialEntity._userId));
            for (UUID item : this.userIds)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserCredentialEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.externalIds != null) {
            CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserCredentialEntity._externalId));
            for (String item : this.externalIds)
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
        if (item.match(UserCredential._id)) return UserCredentialEntity._id;
        else if (item.match(UserCredential._externalId)) return UserCredentialEntity._externalId;
        else if (item.prefix(UserCredential._user)) return UserCredentialEntity._userId;
        else if (item.match(UserCredential._user)) return UserCredentialEntity._userId;
        else if (item.match(UserCredential._createdAt) ) return UserCredentialEntity._createdAt;
        else if (item.match(UserCredential._data) ) return UserCredentialEntity._data;
        else if (item.prefix(UserCredential._data) ) return UserCredentialEntity._data;
        else return null;
    }

    @Override
    protected UserCredentialEntity convert(Tuple tuple, Set<String> columns) {
        UserCredentialEntity item = new UserCredentialEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UserCredentialEntity._id, UUID.class));
        item.setExternalId(QueryBase.convertSafe(tuple, columns, UserCredentialEntity._externalId, String.class));
        item.setUserId(QueryBase.convertSafe(tuple, columns, UserCredentialEntity._userId, UUID.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UserCredentialEntity._createdAt, Instant.class));
        item.setData(QueryBase.convertSafe(tuple, columns, UserCredentialEntity._data, String.class));
        return item;
    }

}
