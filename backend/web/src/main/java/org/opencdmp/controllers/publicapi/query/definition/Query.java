package org.opencdmp.controllers.publicapi.query.definition;

import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.criteria.Criteria;
import io.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Query<C extends Criteria<T>, T> implements CriteriaQuery<C, T> {
    private static final Logger logger = LoggerFactory.getLogger(Query.class);
    private C criteria;
    @ApiModelProperty(value = "query", name = "query", dataType = "String", hidden = true)
    private QueryableList<T> query;

    public static class QueryBuilder<C extends Criteria<T>, T, Q extends Query<C, T>> {
        private C criteria;
        private QueryableList<T> query;
        private Class<Q> tClass;

        public QueryBuilder(Class<Q> tClass) {
            this.tClass = tClass;
        }

        public QueryBuilder<C, T, Q> criteria(C criteria) {
            this.criteria = criteria;
            return this;
        }

        public QueryBuilder<C, T, Q> query(QueryableList<T> query) {
            this.query = query;
            return this;
        }

        public Q build() {
            try {
                Q q  = tClass.newInstance();
                q.setCriteria(criteria);
                q.setQuery(query);
                return q;
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error (e.getMessage(), e);
            }
            return null;
        }
    }

    public QueryableList<T> getQuery() {
        return query;
    }

    public void setQuery(QueryableList<T> query) {
        this.query = query;
    }

    public C getCriteria() {
        return criteria;
    }

    public void setCriteria(C criteria) {
        this.criteria = criteria;
    }

    @Override
    public QueryableList<T> collect() {
        return this.applyCriteria();
    }

    @Override
    public QueryableList<T> collect(QueryableList<T> repo) {
        this.query = repo;
        return this.applyCriteria();
    }
}
