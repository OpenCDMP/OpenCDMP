package org.opencdmp.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import gr.cite.tools.exception.MyNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.*;
import org.opencdmp.model.PublicUser;
import org.opencdmp.model.user.User;
import org.opencdmp.query.utils.BuildSubQueryInput;
import org.opencdmp.query.utils.QueryUtilsService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserQuery extends QueryBase<UserEntity> {
    private String like;
    private Boolean planAssociated;
    private Collection<UUID> ids;
    private Collection<String> emails;
    private Collection<UUID> excludedIds;
    private Collection<IsActive> isActives;
    private UserRoleQuery userRoleQuery;
    private TenantUserQuery tenantUserQuery;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final UserScope userScope;
    private final AuthorizationService authService;
    private final  QueryUtilsService queryUtilsService;
    private final TenantEntityManager tenantEntityManager;
    public UserQuery(UserScope userScope, AuthorizationService authService, QueryUtilsService queryUtilsService, TenantEntityManager tenantEntityManager) {
        this.userScope = userScope;
        this.authService = authService;
        this.queryUtilsService = queryUtilsService;
	    this.tenantEntityManager = tenantEntityManager;
    }

    public UserQuery like(String value) {
        this.like = value;
        return this;
    }

    public UserQuery ids(UUID value) {
        this.ids = List.of(value);
        return this;
    }

    public UserQuery ids(UUID... value) {
        this.ids = Arrays.asList(value);
        return this;
    }

    public UserQuery ids(Collection<UUID> values) {
        this.ids = values;
        return this;
    }

    public UserQuery emails(String value) {
        this.emails = List.of(value);
        return this;
    }

    public UserQuery emails(String... value) {
        this.emails = Arrays.asList(value);
        return this;
    }

    public UserQuery emails(Collection<String> values) {
        this.emails = values;
        return this;
    }
    
    public UserQuery excludedIds(Collection<UUID> values) {
        this.excludedIds = values;
        return this;
    }

    public UserQuery excludedIds(UUID value) {
        this.excludedIds = List.of(value);
        return this;
    }

    public UserQuery excludedIds(UUID... value) {
        this.excludedIds = Arrays.asList(value);
        return this;
    }

    public UserQuery isActive(IsActive value) {
        this.isActives = List.of(value);
        return this;
    }

    public UserQuery isActive(IsActive... value) {
        this.isActives = Arrays.asList(value);
        return this;
    }

    public UserQuery isActive(Collection<IsActive> values) {
        this.isActives = values;
        return this;
    }

    public UserQuery userRoleSubQuery(UserRoleQuery userRoleSubQuery) {
        this.userRoleQuery = userRoleSubQuery;
        return this;
    }

    public UserQuery tenantUserSubQuery(TenantUserQuery tenantUserSubQuery) {
        this.tenantUserQuery = tenantUserSubQuery;
        return this;
    }

    public UserQuery planAssociated(Boolean planAssociated) {
        this.planAssociated = planAssociated;
        return this;
    }

    public UserQuery enableTracking() {
        this.noTracking = false;
        return this;
    }

    public UserQuery disableTracking() {
        this.noTracking = true;
        return this;
    }

    public UserQuery authorize(EnumSet<AuthorizationFlags> values) {
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
                        this.isEmpty(this.isActives) ||
                        this.isEmpty(this.emails) ||
                        this.isEmpty(this.excludedIds) ||
                        this.isFalseQuery(this.userRoleQuery) ||
                        this.isFalseQuery(this.tenantUserQuery);
    }

    @Override
    protected Class<UserEntity> entityClass() {
        return UserEntity.class;
    }

    @Override
    protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
        if (this.authorize.contains(AuthorizationFlags.None)) return null;
        if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseUser)) return null;
        UUID userId;
        if (this.authorize.contains(AuthorizationFlags.Owner)) userId = this.userScope.getUserIdSafe();
        if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();
        if (this.authorize.contains(AuthorizationFlags.DescriptionTemplateAssociated)) userId = this.userScope.getUserIdSafe();
        else  userId = null;

        List<Predicate> predicates = new ArrayList<>();
        boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
        if (userId != null || usePublic) {
            UUID finalUserId = userId;
            predicates.add(queryContext.CriteriaBuilder.or(
                    userId != null ?  queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(userId) : queryContext.CriteriaBuilder.or(),  //Creates a false query
                    queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(PlanUserEntity.class, UUID.class)
                            .query(queryContext.Query)
                            .criteriaBuilder(queryContext.CriteriaBuilder)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanUserEntity._userId))
                            .filterFunc((subQueryRoot, cb) ->
                                    cb.in(subQueryRoot.get(PlanUserEntity._planId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, finalUserId, usePublic))
                            )
                    ))),
                    queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(UserDescriptionTemplateEntity.class, UUID.class)
                            .query(queryContext.Query)
                            .criteriaBuilder(queryContext.CriteriaBuilder)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(UserDescriptionTemplateEntity._userId))
                            .filterFunc((subQueryRoot, cb) ->
                                    cb.in(subQueryRoot.get(UserDescriptionTemplateEntity._descriptionTemplateId)).value(this.queryUtilsService.buildUserDescriptionTemplateEntityAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, finalUserId))
                            )
                    )))
            ));
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
        if (this.like != null && !this.like.isBlank()) {
            predicates.add(this.queryUtilsService.ilike(queryContext.CriteriaBuilder, queryContext.Root.get(UserEntity._name), this.like));
        }
        if (this.ids != null) {
            CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id));
            for (UUID item : this.ids)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.excludedIds != null) {
            CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id));
            for (UUID item : this.excludedIds)
                notInClause.value(item);
            predicates.add(notInClause.not());
        }
        if (this.isActives != null) {
            CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._isActive));
            for (IsActive item : this.isActives)
                inClause.value(item);
            predicates.add(inClause);
        }
        if (this.emails != null) {
            Subquery<UUID> userContactInfoSubquery = this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(
                    new BuildSubQueryInput.Builder<>(UserContactInfoQuery.class, UUID.class, queryContext)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(UserContactInfoEntity._id))
                            .filterFunc((subQueryRoot, cb) -> {
                                        CriteriaBuilder.In<String> inClause = cb.in(subQueryRoot.get(UserContactInfoEntity._value));
                                        for (String item : this.emails)
                                            inClause.value(item);
                                        return inClause;
                                }   
                            )
            ));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(userContactInfoSubquery));
        }
        if (this.userRoleQuery != null) {
            QueryContext<UserRoleEntity, UUID> subQuery = this.applySubQuery(this.userRoleQuery, queryContext, UUID.class, userRoleEntityRoot -> userRoleEntityRoot.get(UserRoleEntity._userId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(subQuery.Query));
        }
        if (this.tenantUserQuery != null) {
            QueryContext<TenantUserEntity, UUID> subQuery = this.applySubQuery(this.tenantUserQuery, queryContext, UUID.class, tenantUserEntityRoot -> tenantUserEntityRoot.get(TenantUserEntity._userId));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(subQuery.Query));
        }
        if (this.planAssociated != null){
            UUID userId;
            if (this.userScope.isSet()) userId = this.userScope.getUserIdSafe();
            else throw new MyNotFoundException("Only user scoped allowed");

            Subquery<UUID> planUserUserQuery = this.queryUtilsService.buildSubQuery(new BuildSubQueryInput<>(
                    new BuildSubQueryInput.Builder<>(PlanUserEntity.class, UUID.class, queryContext)
                            .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanUserEntity._userId))
                            .filterFunc((subQueryRoot, cb) -> cb.and(
                                    cb.in(subQueryRoot.get(PlanUserEntity._planId)).value(this.queryUtilsService.buildPlanAuthZSubQuery(queryContext.Query, queryContext.CriteriaBuilder, userId, false)) ,
                                    cb.equal(subQueryRoot.get(PlanUserEntity._isActive), IsActive.Active)
                            ))
            ));
            predicates.add(queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id)).value(planUserUserQuery));
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
        if (item.match(User._id) || item.match(PublicUser._id)) return UserEntity._id;
        else if (item.match(User._name) || item.match(PublicUser._name)) return UserEntity._name;
        else if (item.prefix(User._additionalInfo)) return UserEntity._additionalInfo;
        else if (item.match(User._additionalInfo)) return UserEntity._additionalInfo;
        else if (item.match(User._createdAt) ) return UserEntity._createdAt;
        else if (item.match(User._updatedAt)) return UserEntity._updatedAt;
        else if (item.match(User._hash)) return UserEntity._updatedAt;
        else if (item.match(User._isActive)) return UserEntity._isActive;
        else return null;
    }

    @Override
    protected UserEntity convert(Tuple tuple, Set<String> columns) {
        UserEntity item = new UserEntity();
        item.setId(QueryBase.convertSafe(tuple, columns, UserEntity._id, UUID.class));
        item.setName(QueryBase.convertSafe(tuple, columns, UserEntity._name, String.class));
        item.setAdditionalInfo(QueryBase.convertSafe(tuple, columns, UserEntity._additionalInfo, String.class));
        item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UserEntity._createdAt, Instant.class));
        item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, UserEntity._updatedAt, Instant.class));
        item.setIsActive(QueryBase.convertSafe(tuple, columns, UserEntity._isActive, IsActive.class));
        return item;
    }

}
