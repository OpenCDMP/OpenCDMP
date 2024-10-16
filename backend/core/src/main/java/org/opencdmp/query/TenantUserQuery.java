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
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.data.TenantUserEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.model.TenantUser;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TenantUserQuery extends QueryBase<TenantUserEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> userIds;
	private Collection<UUID> tenantIds;
	private Collection<IsActive> isActives;
	private UserQuery userQuery;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	private final UserScope userScope;
	private final AuthorizationService authService;
	private final TenantEntityManager tenantEntityManager;

	public TenantUserQuery(
			UserScope userScope,
			AuthorizationService authService, TenantEntityManager tenantEntityManager
	) {
		this.userScope = userScope;
		this.authService = authService;
		this.tenantEntityManager = tenantEntityManager;
	}

	public TenantUserQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public TenantUserQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public TenantUserQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public TenantUserQuery userIds(UUID value) {
		this.userIds = List.of(value);
		return this;
	}

	public TenantUserQuery userIds(UUID... value) {
		this.userIds = Arrays.asList(value);
		return this;
	}

	public TenantUserQuery userIds(Collection<UUID> values) {
		this.userIds = values;
		return this;
	}

	public TenantUserQuery tenantIds(UUID value) {
		this.tenantIds = List.of(value);
		return this;
	}

	public TenantUserQuery tenantIds(UUID... value) {
		this.tenantIds = Arrays.asList(value);
		return this;
	}

	public TenantUserQuery tenantIds(Collection<UUID> values) {
		this.tenantIds = values;
		return this;
	}

	public TenantUserQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public TenantUserQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public TenantUserQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public TenantUserQuery userSubQuery(UserQuery subQuery) {
		this.userQuery = subQuery;
		return this;
	}

	public TenantUserQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	public TenantUserQuery enableTracking() {
		this.noTracking = false;
		return this;
	}

	public TenantUserQuery disableTracking() {
		this.noTracking = true;
		return this;
	}

	@Override
	protected EntityManager entityManager(){
		return this.tenantEntityManager.getEntityManager();
	}

	@Override
	protected Class<TenantUserEntity> entityClass() {
		return TenantUserEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.userIds) || this.isEmpty(this.tenantIds) || this.isEmpty(this.isActives) || this.isFalseQuery(this.userQuery);
	}


	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseTenantUser)) return null;
		UUID ownerId = null;
		if (this.authorize.contains(AuthorizationFlags.Owner)) ownerId = this.userScope.getUserIdSafe();

		List<Predicate> predicates = new ArrayList<>();
		if (ownerId != null) {
			predicates.add(queryContext.CriteriaBuilder.equal(queryContext.Root.get(TenantUserEntity._userId), ownerId));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantUserEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.userIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantUserEntity._userId));
			for (UUID item : this.userIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.tenantIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantUserEntity._tenantId));
			for (UUID item : this.tenantIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantUserEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.userQuery != null) {
			QueryContext<UserEntity, UUID> subQuery = this.applySubQuery(this.userQuery, queryContext, UUID.class, root -> root.get(TenantUserEntity._userId));
			predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(TenantUserEntity._userId)).value(subQuery.Query));
		}
		if (predicates.size() > 0) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected TenantUserEntity convert(Tuple tuple, Set<String> columns) {
		TenantUserEntity item = new TenantUserEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, TenantUserEntity._id, UUID.class));
		item.setTenantId(QueryBase.convertSafe(tuple, columns, TenantUserEntity._tenantId, UUID.class));
		item.setUserId(QueryBase.convertSafe(tuple, columns, TenantUserEntity._userId, UUID.class));
		item.setTenantId(QueryBase.convertSafe(tuple, columns, TenantUserEntity._tenantId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, TenantUserEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, TenantUserEntity._updatedAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, TenantUserEntity._isActive, IsActive.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(TenantUser._id)) return TenantUserEntity._id;
		else if (item.match(TenantUser._tenant)) return TenantUserEntity._tenantId;
		else if (item.prefix(TenantUser._tenant)) return TenantUserEntity._tenantId;
		else if (item.match(TenantUser._isActive)) return TenantUserEntity._isActive;
		else if (item.match(TenantUser._createdAt)) return TenantUserEntity._createdAt;
		else if (item.match(TenantUser._updatedAt)) return TenantUserEntity._updatedAt;
		else if (item.match(TenantUser._hash)) return TenantUserEntity._updatedAt;
		else if (item.match(TenantUser._user, UserEntity._id)) return TenantUserEntity._userId;
		else if (item.prefix(TenantUser._user)) return TenantUserEntity._userId;
		else if (item.match(TenantUser._belongsToCurrentTenant)) return TenantUserEntity._tenantId;
		else return null;
	}
}
