package org.opencdmp.controllers.publicapi.query.definition;

import org.opencdmp.controllers.publicapi.QueryableList;

/**
 * Created by ikalyvas on 3/21/2018.
 */
public interface Collector<T> {
    QueryableList<T> collect() throws Exception;

    QueryableList<T> collect(QueryableList<T> repo) throws Exception;

}
