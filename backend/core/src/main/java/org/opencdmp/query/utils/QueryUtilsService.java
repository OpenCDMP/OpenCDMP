package org.opencdmp.query.utils;

import jakarta.persistence.criteria.*;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.PlanStatus;

import java.util.UUID;

public interface QueryUtilsService {
    <Key, D> Subquery<Key> buildSubQuery(BuildSubQueryInput<D, Key> parameters);

    Subquery<UUID> buildPlanAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, Boolean usePublic);

    Subquery<UUID> buildUserDescriptionTemplateEntityAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId);

    Subquery<UUID> buildDescriptionAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, Boolean usePublic);

    Subquery<UUID> buildPublicPlanAuthZSubQuery(AbstractQuery<?> query,
                                                CriteriaBuilder criteriaBuilder,
                                                Boolean usePublic);

    Subquery<UUID> buildPlanStatusAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, PlanStatus internalStatus);

    Subquery<UUID> buildDescriptionStatusAuthZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, DescriptionStatus internalStatus);

    Subquery<UUID> buildPlanUserAuthZSubQuery(AbstractQuery<?> query,
                                              CriteriaBuilder criteriaBuilder,
                                              UUID userId);

	Predicate ilike(CriteriaBuilder criteriaBuilder, Expression<String> path, String value);
}
