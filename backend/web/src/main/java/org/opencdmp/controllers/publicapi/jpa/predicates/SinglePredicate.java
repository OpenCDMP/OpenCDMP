package org.opencdmp.controllers.publicapi.jpa.predicates;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import javax.management.InvalidApplicationException;

public interface SinglePredicate<T> {
    Predicate applyPredicate(CriteriaBuilder builder, Root<T> root) throws InvalidApplicationException;
}
