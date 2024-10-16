package org.opencdmp.controllers.publicapi.jpa.predicates;

public interface SelectPredicate<T, R> {
    R applySelection(T item);
}
