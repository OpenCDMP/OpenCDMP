package org.opencdmp.query.utils;

import jakarta.persistence.criteria.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.data.UserDescriptionTemplateEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QueryUtilsServiceImpl implements QueryUtilsService {
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
                                            cb.equal(subQueryRoot.get(PlanEntity._status),  PlanStatus.Finalized),
                                            cb.equal(subQueryRoot.get(PlanEntity._isActive), IsActive.Active)
                                    ): cb.or(), //Creates a false query
                                    userId != null ? cb.in(subQueryRoot.get(PlanEntity._id)).value(this.buildPlanUserAuthZSubQuery(query, criteriaBuilder, userId)) : cb.or() //Creates a false query
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
                                                cb.equal(subQueryRoot.get(DescriptionEntity._status), DescriptionStatus.Finalized),
                                                cb.equal(subQueryRoot.get(DescriptionEntity._isActive), IsActive.Active),
                                                cb.in(subQueryRoot.get(DescriptionEntity._planId)).value(this.buildPublicPlanAuthZSubQuery(query, criteriaBuilder, usePublic))
                                        ): cb.or(), //Creates a false query
                                        userId != null ?
                                                cb.or(
                                                        cb.equal(subQueryRoot.get(DescriptionEntity._createdById), userId),
                                                        cb.in(subQueryRoot.get(DescriptionEntity._planId)).value(this.buildPlanUserAuthZSubQuery(query, criteriaBuilder, userId))
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
                                        cb.equal(subQueryRoot.get(PlanEntity._status),  PlanStatus.Finalized),
                                        cb.equal(subQueryRoot.get(PlanEntity._isActive), IsActive.Active)
                                ): cb.or() //Creates a false query
                        )
        ));
    }

    @Override
    public Subquery<UUID> buildPlanUserAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId){
        return this.buildSubQuery(new BuildSubQueryInput<>(new BuildSubQueryInput.Builder<>(PlanUserEntity.class, UUID.class)
                .query(query)
                .criteriaBuilder(criteriaBuilder)
                .keyPathFunc((subQueryRoot) -> subQueryRoot.get(PlanUserEntity._planId))
                .filterFunc((subQueryRoot, cb) -> 
                        userId != null ? cb.and(
                            cb.equal(subQueryRoot.get(PlanUserEntity._userId), userId),
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
}

