package org.opencdmp.controllers.publicapi.query.definition;

import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.criteria.Criteria;

/**
 * Created by ikalyvas on 3/21/2018.
 */
public interface TableCriteriaQuery<C extends Criteria<T>, T> extends CriteriaQuery<C, T> {
    QueryableList<T> applyPaging(QueryableList<T> items) ;
}
