package org.opencdmp.controllers.publicapi;

import org.opencdmp.controllers.publicapi.jpa.predicates.*;
import org.opencdmp.controllers.publicapi.types.SelectionField;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;

import javax.management.InvalidApplicationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface QueryableList<T> {
    QueryableList<T> where(SinglePredicate<T> predicate);

    <R> List<R> select(SelectPredicate<T, R> predicate) throws InvalidApplicationException;

    <R> CompletableFuture<List<R>> selectAsync(SelectPredicate<T, R> predicate) throws InvalidApplicationException;

    List<T> toList() throws InvalidApplicationException;

    <V> void update(EntitySelectPredicate<T> selectPredicate, V value) throws InvalidApplicationException;

    QueryableList<T> withFields(List<String> fields);

    List<Map> toListWithFields();

    CompletableFuture<List<T>> toListAsync() throws InvalidApplicationException;

    T getSingle() throws InvalidApplicationException;

    CompletableFuture<T> getSingleAsync() throws InvalidApplicationException;

    T getSingleOrDefault() throws InvalidApplicationException;

    CompletableFuture<T> getSingleOrDefaultAsync() throws InvalidApplicationException;

    QueryableList<T> skip(Integer offset);

    QueryableList<T> take(Integer length);

    QueryableList<T> distinct();

    QueryableList<T> orderBy(OrderByPredicate<T> predicate);

    QueryableList<T> groupBy(GroupByPredicate<T> predicate);

    QueryableList<T> withHint(String hint);

    Long count() throws InvalidApplicationException;

    QueryableList<T> where(NestedQuerySinglePredicate<T> predicate);

    CompletableFuture<Long> countAsync() throws InvalidApplicationException;

    Subquery<T> query(List<SelectionField> fields) throws InvalidApplicationException;

    Subquery<T> subQuery(SinglePredicate<T> predicate, List<SelectionField> fields) throws InvalidApplicationException;

    Subquery<T> subQuery(NestedQuerySinglePredicate<T> predicate, List<SelectionField> fields);

    Subquery<Long> subQueryCount(NestedQuerySinglePredicate<T> predicate, List<SelectionField> fields);

    Subquery<Long> subQueryCount(SinglePredicate<T> predicate, List<SelectionField> fields) throws InvalidApplicationException;

    <U> QueryableList<T> initSubQuery(Class<U> uClass);

    <U extends Comparable> Subquery<U> subQueryMax(SinglePredicate<T> predicate, List<SelectionField> fields, Class<U> uClass) throws InvalidApplicationException;

    <U extends Comparable> Subquery<U> subQueryMax(NestedQuerySinglePredicate<T> predicate, List<SelectionField> fields, Class<U> uClass);

    Join getJoin(String field, JoinType type);


}
