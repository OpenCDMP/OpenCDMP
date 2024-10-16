package org.opencdmp.controllers.publicapi.jpa.predicates;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

/**
 * Created by ikalyvas on 10/10/2018.
 */
public interface EntitySelectPredicate<T> {
    Path applyPredicate(Root<T> root);

}
