package org.opencdmp.controllers.publicapi.jpa.predicates;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public interface GroupByPredicate<T> {
    Expression<T> applyPredicate(CriteriaBuilder builder, Root<T> root);

}
