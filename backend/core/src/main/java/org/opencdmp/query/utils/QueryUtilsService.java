package org.opencdmp.query.utils;

import jakarta.persistence.criteria.*;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanUserRole;

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
                                              UUID userId,
                                              IsActive isActive);

    Subquery<UUID> buildPlanUserAuthWithRoleZSubQuery(AbstractQuery<?> query, CriteriaBuilder criteriaBuilder, UUID userId, PlanUserRole role);

    Predicate ilike(CriteriaBuilder criteriaBuilder, Expression<String> path, String value);

    Predicate buildTenantFilter(CriteriaBuilder criteriaBuilder, Expression<String> path);
}
