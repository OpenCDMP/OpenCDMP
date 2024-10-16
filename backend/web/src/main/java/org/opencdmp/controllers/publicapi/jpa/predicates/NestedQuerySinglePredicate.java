package org.opencdmp.controllers.publicapi.jpa.predicates;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Created by ikalyvas on 2/7/2018.
 */
public interface NestedQuerySinglePredicate<T> {
    Predicate applyPredicate(CriteriaBuilder builder, Root<T> root, Root<T> nestedQueryRoot);

}
