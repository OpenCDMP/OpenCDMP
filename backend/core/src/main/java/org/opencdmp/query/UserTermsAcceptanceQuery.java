package org.opencdmp.query;


import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.data.UserTermsAcceptanceEntity;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserTermsAcceptanceQuery extends QueryBase<UserTermsAcceptanceEntity> {

    private Collection<UUID> ids;
    private Collection<UUID> userIds;
    private Collection<UUID> tenantIds;
    private Boolean tenantIsSet;
    private Collection<UUID> excludedIds;
    private Collection<Short> termsVersions;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    private final TenantEntityManagerFactory tenantEntityManagerFactory;
	public UserTermsAcceptanceQuery(TenantEntityManagerFactory tenantEntityManagerFactory) {
		this.tenantEntityManagerFactory = tenantEntityManagerFactory;
	}

    public UserTermsAcceptanceQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UserTermsAcceptanceQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UserTermsAcceptanceQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public UserTermsAcceptanceQuery tenantIds(UUID value) {
        this.tenantIds = List.of(value);
        return this;
    }

    public UserTermsAcceptanceQuery tenantIds(UUID... value) {
        this.tenantIds = Arrays.asList(value);
        return this;
    }

    public UserTermsAcceptanceQuery tenantIds(Collection<UUID> values) {
        this.tenantIds = values;
        return this;
    }

    public UserTermsAcceptanceQuery clearTenantIds() {
        this.tenantIds = null;
        return this;
    }

    public UserTermsAcceptanceQuery tenantIsSet(Boolean values) {
        this.tenantIsSet = values;
        return this;
    }

    public UserTermsAcceptanceQuery userIds(UUID value) {
        this.userIds = List.of(value);
        return this;
    }

    public UserTermsAcceptanceQuery userIds(UUID... value) {
        this.userIds = Arrays.asList(value);
        return this;
    }

    public UserTermsAcceptanceQuery userIds(Collection<UUID> values) {
        this.userIds = values;
        return this;
    }

    public UserTermsAcceptanceQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UserTermsAcceptanceQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UserTermsAcceptanceQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UserTermsAcceptanceQuery termsVersions(Short value) {
        this.termsVersions = List.of(value);
        return this;
    }

    public UserTermsAcceptanceQuery termsVersions(Short... value) {
        this.termsVersions = Arrays.asList(value);
        return this;
    }

    public UserTermsAcceptanceQuery termsVersions(Collection<Short> values) {
        this.termsVersions = values;
        return this;
    }

    public UserTermsAcceptanceQuery authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public UserTermsAcceptanceQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UserTermsAcceptanceQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    @Override
    protected EntityManager entityManager(){
        return this.tenantEntityManagerFactory.getInstance().getEntityManager();
    }

    @Override
    protected Boolean isFalseQuery() {
        return this.isEmpty(this.ids) || this.isEmpty(this.tenantIds);
    }

    @Override
    protected Class<UserTermsAcceptanceEntity> entityClass() {
        return UserTermsAcceptanceEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
        List<Predicate> predicates = new ArrayList<>();
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserTermsAcceptanceEntity._id));
            for (UUID item : this.ids) inClause.value(item);
            predicates.add(inClause);
        }
        
        if (this.tenantIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserTermsAcceptanceEntity._tenantId));
            for (UUID item : this.tenantIds) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.userIds != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserTermsAcceptanceEntity._userId));
            for (UUID item : this.userIds) inClause.value(item);
            predicates.add(inClause);
        }

        if (this.tenantIsSet != null) {
            if (this.tenantIsSet) predicates.add(queryContext.CriteriaBuilder.isNotNull(queryContext.Root.get(UserTermsAcceptanceEntity._tenantId)));
            else predicates.add(queryContext.CriteriaBuilder.isNull(queryContext.Root.get(UserTermsAcceptanceEntity._tenantId)));
        }

        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserTermsAcceptanceEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }

        if (this.termsVersions != null) {
            CriteriaBuilder.In<Short> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserTermsAcceptanceEntity._termsVersion));
            for (Short item : this.termsVersions) inClause.value(item);
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
    protected UserTermsAcceptanceEntity convert(Tuple tuple, Set<String> columns) {
        UserTermsAcceptanceEntity item = new UserTermsAcceptanceEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UserTermsAcceptanceEntity._id, UUID.class));
        item.setUserId(QueryBase.convertSafe(tuple, columns, UserTermsAcceptanceEntity._userId, UUID.class));
        item.setTenantId(QueryBase.convertSafe(tuple, columns, UserTermsAcceptanceEntity._tenantId, UUID.class));
        item.setAcceptedAt(QueryBase.convertSafe(tuple, columns, UserTermsAcceptanceEntity._acceptedAt, Instant.class));
        item.setClientIp(QueryBase.convertSafe(tuple, columns, UserTermsAcceptanceEntity._clientIp, String.class));
        item.setTermsVersion(QueryBase.convertSafe(tuple, columns, UserTermsAcceptanceEntity._termsVersion, Short.class));
        return item;
    }

    @Override
    protected String fieldNameOf(FieldResolver item) {
        if (item.match(UserTermsAcceptanceEntity._id)) return UserTermsAcceptanceEntity._id;
        else if (item.match(UserTermsAcceptanceEntity._userId)) return UserTermsAcceptanceEntity._userId;
        else if (item.prefix(UserTermsAcceptanceEntity._tenantId)) return UserTermsAcceptanceEntity._tenantId;
        else if (item.match(UserTermsAcceptanceEntity._acceptedAt)) return UserTermsAcceptanceEntity._acceptedAt;
        else if (item.match(UserTermsAcceptanceEntity._clientIp)) return UserTermsAcceptanceEntity._clientIp;
        else if (item.match(UserTermsAcceptanceEntity._tenantId)) return UserTermsAcceptanceEntity._tenantId;
        else if (item.match(UserTermsAcceptanceEntity._termsVersion)) return UserTermsAcceptanceEntity._termsVersion;
        else return null;
    }

}
