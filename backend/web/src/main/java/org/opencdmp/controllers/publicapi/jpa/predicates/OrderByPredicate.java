package org.opencdmp.controllers.publicapi.jpa.predicates;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

public interface OrderByPredicate<T> {
    Order applyPredicate(CriteriaBuilder builder, Root<T> root);

}
