package org.opencdmp.query.utils;

import jakarta.persistence.criteria.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.data.*;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.UUID;

@Component
public class QueryUtilsServiceImpl implements QueryUtilsService {

    private final TenantScopeFactory tenantScopeFactory;

    public QueryUtilsServiceImpl(TenantScopeFactory tenantScopeFactory) {
        this.tenantScopeFactory = tenantScopeFactory;
    }

    @Override
    public <Key, D> Subquery<Key> buildSubQuery(BuildSubQueryInput<D, Key> parameters){
        Subquery<Key> subQuery = parameters.getQuery().subquery(parameters.getKeyType());
        Root<D> subQueryRoot = subQuery.from(parameters.getEntityType());
        subQuery.select(parameters.getKeyPathFunc().apply(subQueryRoot)).distinct(true);
        subQuery.where(parameters.getFilterFunc().apply(subQueryRoot, parameters.getCriteriaBuilder()));
        return subQuery;
    }

    @Override
    public Subquery<UUID> buildPlanAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, Boolean usePublic){
        return this.buildSubQuery(new BuildSubQueryInput<>(
                new BuildSubQueryInput.Builder<>(PlanEntity.class, UUID.class)
                        .query(query)
                        .criteriaBuilder(criteriaBuilder)
                        .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanEntity._id))
                        .filterFunc((subQueryRoot, cb) -> cb.or(
                                    usePublic ? cb.and(
                                            cb.equal(subQueryRoot.get(PlanEntity._accessType), PlanAccessType.Public),
                                            cb.in(subQueryRoot.get(PlanEntity._statusId)).value(this.buildPlanStatusAuthZSubQuery(query, criteriaBuilder, PlanStatus.Finalized)),
                                            cb.equal(subQueryRoot.get(PlanEntity._isActive), IsActive.Active)
                                    ): cb.or(), //Creates a false query
                                    userId != null ? cb.in(subQueryRoot.get(PlanEntity._id)).value(this.buildPlanUserAuthZSubQuery(query, criteriaBuilder, userId, IsActive.Active)) : cb.or(), //Creates a false query
                                    userId != null ? cb.and(
                                            cb.in(subQueryRoot.get(PlanEntity._id)).value(this.buildPlanUserAuthZSubQuery(query, criteriaBuilder, userId, IsActive.Inactive)),
                                            cb.equal(subQueryRoot.get(PlanEntity._isActive), IsActive.Inactive)
                                    ) : cb.or() //Creates a false query
                                )
                        )
        ));
    }


    @Override
    public Subquery<UUID> buildUserDescriptionTemplateEntityAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId){
        return this.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(UserDescriptionTemplateEntity.class, UUID.class)
                .query(query)
                .criteriaBuilder(criteriaBuilder)
                .keyPathFunc((subQueryRoot) -> subQueryRoot.get(UserDescriptionTemplateEntity._descriptionTemplateId))
                .filterFunc((subQueryRoot, cb) ->
                        userId != null ? cb.and(
                                cb.equal(subQueryRoot.get(PlanUserEntity._userId), userId),
                                cb.equal(subQueryRoot.get(PlanUserEntity._isActive), IsActive.Active)
                        ) : cb.or() //Creates a false query
                )
        ));
    }

    @Override
    public Subquery<UUID> buildDescriptionAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, Boolean usePublic) {
        return this.buildSubQuery(new BuildSubQueryInput<>(
                new BuildSubQueryInput.Builder<>(DescriptionEntity.class, UUID.class)
                        .query(query)
                        .criteriaBuilder(criteriaBuilder)
                        .keyPathFunc((subQueryRoot) -> subQueryRoot.get(DescriptionEntity._id))
                        .filterFunc((subQueryRoot, cb) -> cb.or(
                                        usePublic ? cb.and(
                                                cb.in(subQueryRoot.get(DescriptionEntity._statusId)).value(this.buildDescriptionStatusAuthZSubQuery(query, criteriaBuilder, DescriptionStatus.Finalized)),
                                                cb.equal(subQueryRoot.get(DescriptionEntity._isActive), IsActive.Active),
                                                cb.in(subQueryRoot.get(DescriptionEntity._planId)).value(this.buildPublicPlanAuthZSubQuery(query, criteriaBuilder, usePublic))
                                        ): cb.or(), //Creates a false query
                                        userId != null ?
                                                cb.or(
                                                        cb.equal(subQueryRoot.get(DescriptionEntity._createdById), userId),
                                                        cb.in(subQueryRoot.get(DescriptionEntity._planId)).value(this.buildPlanUserAuthZSubQuery(query, criteriaBuilder, userId, IsActive.Active))
                                                ) : cb.or(), //Creates a false query
                                        userId != null ?
                                                cb.and(
                                                        // give access to inactive plans users for inactive plans
                                                        cb.in(subQueryRoot.get(DescriptionEntity._planId)).value(this.buildPlanUserAuthZSubQuery(query, criteriaBuilder, userId, IsActive.Inactive)),
                                                        cb.equal(subQueryRoot.get(DescriptionEntity._isActive), IsActive.Inactive),
                                                        cb.in(subQueryRoot.get(DescriptionEntity._planId)).value(
                                                                this.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(PlanEntity.class, UUID.class)
                                                                        .query(query)
                                                                        .criteriaBuilder(criteriaBuilder)
                                                                        .keyPathFunc((subQueryRoot2) -> subQueryRoot.get(PlanEntity._id))
                                                                        .filterFunc((subQueryRoot2, cb2) ->
                                                                                cb2.equal(subQueryRoot.get(PlanEntity._isActive), IsActive.Inactive)
                                                                        )
                                                                ))
                                                        )
                                                ) : cb.or() //Creates a false query
                                )
                        )
        ));
    }

    @Override
    public Subquery<UUID> buildPublicPlanAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, Boolean usePublic){
        return this.buildSubQuery(new BuildSubQueryInput<>(
                new BuildSubQueryInput.Builder<>(PlanEntity.class, UUID.class)
                        .query(query)
                        .criteriaBuilder(criteriaBuilder)
                        .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanEntity._id))
                        .filterFunc((subQueryRoot, cb) -> 
                                usePublic ? cb.and(
                                        cb.equal(subQueryRoot.get(PlanEntity._accessType), PlanAccessType.Public),
                                        cb.in(subQueryRoot.get(PlanEntity._statusId)).value(this.buildPlanStatusAuthZSubQuery(query, criteriaBuilder, PlanStatus.Finalized)),
                                        cb.equal(subQueryRoot.get(PlanEntity._isActive), IsActive.Active)
                                ): cb.or() //Creates a false query
                        )
        ));
    }

    @Override
    public Subquery<UUID> buildPlanStatusAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, PlanStatus internalStatus){
        return this.buildSubQuery(new BuildSubQueryInput<>(
                new BuildSubQueryInput.Builder<>(PlanStatusEntity.class, UUID.class)
                        .query(query)
                        .criteriaBuilder(criteriaBuilder)
                        .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanStatusEntity._id))
                        .filterFunc((subQueryRoot, cb) ->
                                cb.and(
                                        cb.equal(subQueryRoot.get(PlanStatusEntity._internalStatus), internalStatus),
                                        cb.equal(subQueryRoot.get(PlanStatusEntity._isActive), IsActive.Active)
                                )
                        )
        ));
    }

    @Override
    public Subquery<UUID> buildDescriptionStatusAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, DescriptionStatus internalStatus){
        return this.buildSubQuery(new BuildSubQueryInput<>(
                new BuildSubQueryInput.Builder<>(DescriptionStatusEntity.class, UUID.class)
                        .query(query)
                        .criteriaBuilder(criteriaBuilder)
                        .keyPathFunc((subQueryRoot) -> subQueryRoot.get(DescriptionStatusEntity._id))
                        .filterFunc((subQueryRoot, cb) ->
                                        cb.and(
                                                cb.equal(subQueryRoot.get(DescriptionStatusEntity._internalStatus), internalStatus),
                                                cb.equal(subQueryRoot.get(DescriptionStatusEntity._isActive), IsActive.Active)
                                        )
                        )
        ));
    }

    @Override
    public Subquery<UUID> buildPlanUserAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, IsActive isActive){
        return this.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(PlanUserEntity.class, UUID.class)
                .query(query)
                .criteriaBuilder(criteriaBuilder)
                .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanUserEntity._planId))
                .filterFunc((subQueryRoot, cb) -> 
                        userId != null ? cb.and(
                                cb.equal(subQueryRoot.get(PlanUserEntity._userId), userId),
                                cb.equal(subQueryRoot.get(PlanUserEntity._isActive), isActive)
                        ) : cb.or() //Creates a false query
                )
        ));
    }

    @Override
    public Subquery<UUID> buildPlanUserAuthWithRoleZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, PlanUserRole role){
        return this.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(PlanUserEntity.class, UUID.class)
                .query(query)
                .criteriaBuilder(criteriaBuilder)
                .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanUserEntity._planId))
                .filterFunc((subQueryRoot, cb) ->
                        userId != null ? cb.and(
                                cb.equal(subQueryRoot.get(PlanUserEntity._userId), userId),
                                cb.equal(subQueryRoot.get(PlanUserEntity._role), role),
                                cb.equal(subQueryRoot.get(PlanUserEntity._isActive), IsActive.Active)
                        ) : cb.or() //Creates a false query
                )
        ));
    }


    @Override
    public Predicate ilike(CriteriaBuilder criteriaBuilder, Expression<String> path, String value){
        if (criteriaBuilder instanceof HibernateCriteriaBuilder) {
            return ((HibernateCriteriaBuilder) criteriaBuilder).ilike(path, value);
        } else {
            return criteriaBuilder.like(path, value);
        }
    }

    @Override
    public Predicate buildTenantFilter(CriteriaBuilder criteriaBuilder, Expression<String> path){
        if (!this.tenantScopeFactory.getInstance().isDefaultTenant()) {
            try {
                return criteriaBuilder.equal(path, this.tenantScopeFactory.getInstance().getTenant());
            } catch (InvalidApplicationException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

