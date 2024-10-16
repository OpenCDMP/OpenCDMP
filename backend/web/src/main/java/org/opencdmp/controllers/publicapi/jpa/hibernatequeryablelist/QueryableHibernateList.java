package org.opencdmp.controllers.publicapi.jpa.hibernatequeryablelist;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.collector.Collector;
import org.opencdmp.controllers.publicapi.exceptions.NotSingleResultException;
import org.opencdmp.controllers.publicapi.jpa.predicates.*;
import org.opencdmp.controllers.publicapi.types.FieldSelectionType;
import org.opencdmp.controllers.publicapi.types.SelectionField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import javax.management.InvalidApplicationException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QueryableHibernateList<T> implements QueryableList<T> {

    private static final Logger logger = LoggerFactory.getLogger(QueryableHibernateList.class);

    private Collector collector = new Collector();

    private EntityManager manager;

    private CriteriaQuery query;

    private Class<T> tClass;

    private Root<T> root;

    private Root<T> nestedQueryRoot;

    private Subquery subquery;

    private List<SinglePredicate<T>> singlePredicates = new LinkedList<>();

    private List<NestedQuerySinglePredicate<T>> nestedPredicates = new LinkedList<>();

    private boolean distinct = false;

    private List<OrderByPredicate<T>> orderings = new LinkedList<>();

    private List<GroupByPredicate<T>> groupings = new LinkedList<>();

    private List<String> fields = new LinkedList<>();

    private Integer length;

    private Integer offset;

    private Set<String> hints;

    private String hint;

    private Map<String, Join> joinsMap = new HashMap<>();

    public QueryableHibernateList(EntityManager manager, Class<T> tClass) {
        this.manager = manager;
        this.tClass = tClass;
    }

    public QueryableHibernateList<T> setManager(EntityManager manager) {
        this.manager = manager;
        return this;
    }

    public QueryableList<T> withHint(String hint) {
        this.hint = hint;
        return this;
    }

    @Override
    public QueryableList<T> withFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    private QueryableList<T> selectFields() {
        List<Selection> rootFields = fields.stream().map(this::convertFieldToPath).filter(Objects::nonNull).collect(Collectors.toList());
        this.query.select(this.manager.getCriteriaBuilder().tuple(rootFields.toArray(new Selection[rootFields.size()])));
        return this;
    }

    private Path convertFieldToPath(String field) {
        if (!field.contains(".")) {
            Path path = this.root.get(field);
            path.alias(field);
            return path;
        } else {
            String[] fields = field.split("\\.");
            Path path = this.root.get(fields[0]);
            Join join = null;
            path.alias(fields[0]);
            for (int i = 1; i < fields.length; i++) {
                join = join != null ? join.join(fields[i - 1], JoinType.LEFT) : this.getJoin(fields[i - 1], JoinType.LEFT);
                path = join.get(fields[i]);
                path.alias(String.join(".", Arrays.asList(fields).subList(0, i + 1)));
            }
            return path;
        }
    }

    private Join getJoin(String field, String path, Join joined, JoinType type) {
        if (this.joinsMap.containsKey(path))
            return this.joinsMap.get(path);
        Join join = joined.join(path, type);
        this.joinsMap.put(path, join);
        return join;
    }

    public Join getJoin(String field, JoinType type) {
        if (this.joinsMap.containsKey(field))
            return this.joinsMap.get(field);
        Join join = this.root.join(field, type);
        this.joinsMap.put(field, join);
        return join;
    }

    public QueryableHibernateList<T> setEntity(Class<T> type) {

        return this;
    }

    public void initiateQueryableList(Class<T> type) {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        this.query = builder.createQuery(type);
    }

    @Override
    public QueryableList<T> skip(Integer offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public QueryableList<T> take(Integer length) {
        this.length = length;
        return this;
    }

    public QueryableList<T> where(SinglePredicate<T> predicate) {
        this.singlePredicates.add(predicate);
        return this;
    }

    public QueryableList<T> where(NestedQuerySinglePredicate<T> predicate) {
        this.nestedPredicates.add(predicate);
        return this;
    }

    public <R> List<R> select(SelectPredicate<T, R> predicate) throws InvalidApplicationException {
        return this.toList().stream().map(predicate::applySelection).collect(Collectors.toList());
    }

    public <R> CompletableFuture<List<R>> selectAsync(SelectPredicate<T, R> predicate) throws InvalidApplicationException {
        return this.toListAsync().thenApplyAsync(items -> items.stream().map(predicate::applySelection).collect(Collectors.toList()));
    }

    public QueryableList<T> distinct() {
        this.distinct = true;
        return this;
    }

    public QueryableList<T> orderBy(OrderByPredicate<T> predicate) {
        this.orderings.add(predicate);
        return this;
    }

    public QueryableList<T> groupBy(GroupByPredicate<T> predicate) {
        this.groupings.add(predicate);
        return this;
    }

    public Long count() throws InvalidApplicationException {
        CriteriaBuilder criteriaBuilder = this.manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        this.root = criteriaQuery.from(tClass);
        if (distinct)
            criteriaQuery.select(criteriaBuilder.countDistinct(this.root.get("id")));
        else
            criteriaQuery.select(criteriaBuilder.count(this.root));
        criteriaQuery.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.groupings.isEmpty())
            criteriaQuery.groupBy(this.generateGroupPredicates(this.groupings, this.root));
        //if (distinct) criteriaQuery.distinct(true);

        //GK: Group By special case. When group by is used, since it will result in a list of how many elements have the grouped field common
        // then it will instead return the number of the distinct values of the grouped field
        if (this.groupings.isEmpty()) {
            return this.manager.createQuery(criteriaQuery).getSingleResult();
        } else {
            return (long) this.manager.createQuery(criteriaQuery).getResultList().size();
        }
    }

    @Async
    public CompletableFuture<Long> countAsync() throws InvalidApplicationException {
        CriteriaBuilder criteriaBuilder = this.manager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        this.root = criteriaQuery.from(tClass);
        if (distinct)
            criteriaQuery.select(criteriaBuilder.countDistinct(this.root.get("id")));
        else
            criteriaQuery.select(criteriaBuilder.count(this.root));
        criteriaQuery.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.groupings.isEmpty())
            criteriaQuery.groupBy(this.generateGroupPredicates(this.groupings, this.root));
        //if (distinct) criteriaQuery.distinct(true);
        return CompletableFuture.supplyAsync(() -> {
            if (this.groupings.isEmpty()) {
                return this.manager.createQuery(criteriaQuery).getSingleResult();
            } else {
                return (long) this.manager.createQuery(criteriaQuery).getResultList().size();
            }
        });
    }

    private Predicate[] generateWherePredicates(List<SinglePredicate<T>> singlePredicates, Root<T> root, List<NestedQuerySinglePredicate<T>> nestedPredicates, Root<T> nestedQueryRoot) throws InvalidApplicationException {
        List<Predicate> predicates = new LinkedList<>();
        predicates.addAll(Arrays.asList(this.generateSingleWherePredicates(singlePredicates, root)));
        predicates.addAll(Arrays.asList(this.generateNestedWherePredicates(nestedPredicates, root, nestedQueryRoot)));
        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private Predicate[] generateSingleWherePredicates(List<SinglePredicate<T>> singlePredicates, Root<T> root) throws InvalidApplicationException {
        List<Predicate> predicates = new LinkedList<>();
        for (SinglePredicate<T> singlePredicate : singlePredicates) {
            predicates.add(singlePredicate.applyPredicate(this.manager.getCriteriaBuilder(), root));
        }
        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private Predicate[] generateNestedWherePredicates(List<NestedQuerySinglePredicate<T>> nestedPredicates, Root<T> root, Root<T> nestedQueryRoot) {
        List<Predicate> predicates = new LinkedList<>();
        for (NestedQuerySinglePredicate<T> singlePredicate : nestedPredicates) {
            predicates.add(singlePredicate.applyPredicate(this.manager.getCriteriaBuilder(), root, nestedQueryRoot));
        }
        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private Order[] generateOrderPredicates(List<OrderByPredicate<T>> orderByPredicates, Root<T> root) {
        List<Order> predicates = new LinkedList<>();
        for (OrderByPredicate<T> orderPredicate : orderByPredicates) {
            predicates.add(orderPredicate.applyPredicate(this.manager.getCriteriaBuilder(), root));
        }
        return predicates.toArray(new Order[predicates.size()]);
    }

    private Expression[] generateGroupPredicates(List<GroupByPredicate<T>> groupByPredicates, Root<T> root) {
        List<Expression> predicates = new LinkedList<>();
        for (GroupByPredicate<T> groupPredicate : groupByPredicates) {
            predicates.add(groupPredicate.applyPredicate(this.manager.getCriteriaBuilder(), root));
        }
        return predicates.toArray(new Expression[predicates.size()]);
    }

    public List<T> toList() throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        if (!this.fields.isEmpty())
            this.query = builder.createTupleQuery();
        else
            this.query = builder.createQuery(this.tClass);
        this.root = this.query.from(this.tClass);
        this.query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.orderings.isEmpty())
            this.query.orderBy(this.generateOrderPredicates(this.orderings, this.root));
        if (!this.groupings.isEmpty())
            this.query.groupBy(this.generateGroupPredicates(this.groupings, this.root));
        if (!this.fields.isEmpty())
            this.selectFields();
        if (distinct)
            this.query.distinct(true);
        //if (!this.fields.isEmpty()) this.query.multiselect(this.parseFields(this.fields));
        ObjectMapper mapper = new ObjectMapper();
        if (!this.fields.isEmpty())
            return this.toListWithFields().stream().map(m -> mapper.convertValue(m, this.tClass)).collect(Collectors.toList());
        return this.toListWithOutFields();
    }

    public List<Map> toListWithFields() {
        TypedQuery<Tuple> typedQuery = this.manager.createQuery(this.query);
        if (this.offset != null)
            typedQuery.setFirstResult(this.offset);
        if (this.length != null)
            typedQuery.setMaxResults(this.length);
        List<Tuple> results = typedQuery.getResultList();
        Map<Object, List<Tuple>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(x -> x.get("id")));
        return this.collector.buildFromTuple(results, groupedResults, this.fields, "id");
    }

    private List<T> toListWithOutFields() {
        TypedQuery<T> typedQuery = this.manager.createQuery(this.query);
        if (this.offset != null)
            typedQuery.setFirstResult(this.offset);
        if (this.length != null)
            typedQuery.setMaxResults(this.length);
//        if (this.hint != null) {
//            List ids = typedQuery.getResultList().stream().map(item -> item.getKeys()).collect(Collectors.toList());
//            if (ids != null && !ids.isEmpty())
//                typedQuery = queryWithHint(ids);
//        }
        return typedQuery.getResultList();
    }

    @Async
    public CompletableFuture<List<T>> toListAsync() throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        if (!this.fields.isEmpty())
            this.query = builder.createTupleQuery();
        else
            this.query = builder.createQuery(this.tClass);
        this.root = this.query.from(this.tClass);
        this.query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.orderings.isEmpty())
            this.query.orderBy(this.generateOrderPredicates(this.orderings, this.root));
        if (!this.groupings.isEmpty())
            this.query.groupBy(this.generateGroupPredicates(this.groupings, this.root));
        if (!this.fields.isEmpty())
            this.selectFields();
        if (distinct)
            this.query.distinct(true);
        if (!this.fields.isEmpty())
            return this.toListAsyncWithFields();
        else
            return this.toListAsyncWithOutFields();
    }

    @Async
    private CompletableFuture<List<T>> toListAsyncWithFields() {
        List<Tuple> results = this.manager.createQuery(query).getResultList();
        Map<Object, List<Tuple>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(x -> x.get("id")));
        return CompletableFuture.supplyAsync(() -> results.stream().map(x -> {
            try {
//                return (T) this.tClass.newInstance().buildFromTuple(groupedResults.get(x.get("id")), this.fields, "");
                return (T) this.tClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }).collect(Collectors.toList()));
    }

    @Async
    private CompletableFuture<List<T>> toListAsyncWithOutFields() {
        TypedQuery<T> typedQuery = this.manager.createQuery(this.query);
        if (this.offset != null)
            typedQuery.setFirstResult(this.offset);
        if (this.length != null)
            typedQuery.setMaxResults(this.length);
        return CompletableFuture.supplyAsync(() -> {
            if (this.hint != null) {
//                List ids = typedQuery.getResultList().stream().map(item -> item.getKeys()).collect(Collectors.toList());
//                if (ids != null && !ids.isEmpty())
//                    return queryWithHint(ids).getResultList();
            }
            return typedQuery.getResultList();
        });
    }

    public T getSingle() throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        if (!this.fields.isEmpty())
            this.query = builder.createTupleQuery();
        else
            this.query = builder.createQuery(this.tClass);
        this.root = this.query.from(this.tClass);
        this.query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.fields.isEmpty())
            this.selectFields();
        TypedQuery<T> typedQuery = this.manager.createQuery(this.query);
        if (this.hint != null)
            typedQuery.setHint("jakarta.persistence.fetchgraph", this.manager.getEntityGraph(this.hint));
        return typedQuery.getSingleResult();
    }

    @Async
    public CompletableFuture<T> getSingleAsync() throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        if (!this.fields.isEmpty())
            this.query = builder.createTupleQuery();
        else
            this.query = builder.createQuery(this.tClass);
        this.root = this.query.from(this.tClass);
        this.query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.fields.isEmpty())
            this.selectFields();
        TypedQuery<T> typedQuery = this.manager.createQuery(this.query);
        if (this.hint != null)
            typedQuery.setHint("jakarta.persistence.fetchgraph", this.manager.getEntityGraph(this.hint));
        return CompletableFuture.supplyAsync(() -> typedQuery.getSingleResult());
    }

    public T getSingleOrDefault() throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        if (!this.fields.isEmpty())
            this.query = builder.createTupleQuery();
        else
            this.query = builder.createQuery(this.tClass);
        this.root = this.query.from(this.tClass);
        this.query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.fields.isEmpty())
            this.selectFields();
        TypedQuery<T> typedQuery = this.manager.createQuery(this.query);
        if (this.hint != null)
            typedQuery.setHint("jakarta.persistence.fetchgraph", this.manager.getEntityGraph(this.hint));
        List<T> results = typedQuery.getResultList();
        if (results.size() == 0)
            return null;
        if (results.size() == 1)
            return results.get(0);
        else
            throw new NotSingleResultException("Query returned more than one items");
    }

    @Async
    public CompletableFuture<T> getSingleOrDefaultAsync() throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        if (!this.fields.isEmpty())
            this.query = builder.createTupleQuery();
        else
            this.query = builder.createQuery(this.tClass);
        this.root = this.query.from(this.tClass);
        this.query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (!this.fields.isEmpty())
            this.selectFields();
        TypedQuery<T> typedQuery = this.manager.createQuery(this.query);
        if (this.hint != null)
            typedQuery.setHint("jakarta.persistence.fetchgraph", this.manager.getEntityGraph(this.hint));
        return CompletableFuture.supplyAsync(() -> typedQuery.getResultList()).thenApply(list -> {
            if (list.size() == 0)
                return null;
            if (list.size() == 1)
                return list.get(0);
            else
                throw new NotSingleResultException("Query returned more than one items");
        });
    }

    private TypedQuery<T> queryWithHint(List ids) {
        CriteriaBuilder criteriaBuilder = this.manager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(tClass);
        Root<T> criteriaRoot = criteriaQuery.from(this.tClass);
        criteriaQuery.where(criteriaRoot.get("id").in(ids));
        if (!this.orderings.isEmpty())
            criteriaQuery.orderBy(this.generateOrderPredicates(this.orderings, criteriaRoot));

        if (!this.groupings.isEmpty())
            criteriaQuery.groupBy(this.generateGroupPredicates(this.groupings, this.root));

        TypedQuery<T> typedQuery = this.manager.createQuery(criteriaQuery);
        typedQuery.setHint("jakarta.persistence.fetchgraph", this.manager.getEntityGraph(this.hint));
        return typedQuery;
    }

    @Override
    public Subquery<T> subQuery(SinglePredicate<T> predicate, List<SelectionField> fields) throws InvalidApplicationException {
        Subquery<T> subquery = this.manager.getCriteriaBuilder().createQuery().subquery(this.tClass);
        this.nestedQueryRoot = subquery.from(this.tClass);
        subquery.where(predicate.applyPredicate(this.manager.getCriteriaBuilder(), this.nestedQueryRoot));
        if (fields.get(0).getType() == FieldSelectionType.FIELD)
            subquery.select(this.nestedQueryRoot.get(fields.get(0).getField()));
        else if (fields.get(0).getType() == FieldSelectionType.COMPOSITE_FIELD) {
            subquery.select(this.nestedQueryRoot.join(fields.get(0).getField().split(":")[0]).get(fields.get(0).getField().split(":")[1]));
        }
        return subquery;
    }

    @Override
    public Subquery<T> subQuery(NestedQuerySinglePredicate<T> predicate, List<SelectionField> fields) {
        Subquery<T> subquery = this.manager.getCriteriaBuilder().createQuery().subquery(this.tClass);
        this.nestedQueryRoot = subquery.from(this.tClass);
        subquery.where(predicate.applyPredicate(this.manager.getCriteriaBuilder(), this.root, this.nestedQueryRoot));
        if (fields.get(0).getType() == FieldSelectionType.FIELD)
            subquery.select(this.nestedQueryRoot.get(fields.get(0).getField()));
        else if (fields.get(0).getType() == FieldSelectionType.COMPOSITE_FIELD) {
            subquery.select(this.nestedQueryRoot.get(fields.get(0).getField().split(":")[0]).get(fields.get(0).getField().split(":")[1]));
        }
        return subquery;
    }

    @Override
    public Subquery<Long> subQueryCount(SinglePredicate<T> predicate, List<SelectionField> fields) throws InvalidApplicationException {
        Subquery<Long> subquery = this.manager.getCriteriaBuilder().createQuery().subquery(Long.class);
        this.nestedQueryRoot = subquery.from(this.tClass);
        subquery.where(predicate.applyPredicate(this.manager.getCriteriaBuilder(), this.nestedQueryRoot));
        subquery.select(this.manager.getCriteriaBuilder().count(this.nestedQueryRoot.get(fields.get(0).getField())));
        return subquery;
    }

    @Override
    public Subquery<Long> subQueryCount(NestedQuerySinglePredicate<T> predicate, List<SelectionField> fields) {
        Subquery<Long> subquery = this.manager.getCriteriaBuilder().createQuery().subquery(Long.class);
        this.nestedQueryRoot = subquery.from(this.tClass);
        subquery.where(predicate.applyPredicate(this.manager.getCriteriaBuilder(), this.root, this.nestedQueryRoot));
        subquery.select(this.manager.getCriteriaBuilder().count(this.nestedQueryRoot.get(fields.get(0).getField())));
        return subquery;
    }

    @Override
    public <U extends Comparable> Subquery<U> subQueryMax(SinglePredicate<T> predicate, List<SelectionField> fields, Class<U> uClass) throws InvalidApplicationException {
        Subquery<U> subquery = this.manager.getCriteriaBuilder().createQuery().subquery(uClass);
        this.nestedQueryRoot = subquery.from(this.tClass);
        subquery.where(predicate.applyPredicate(this.manager.getCriteriaBuilder(), this.nestedQueryRoot));
        if (fields.get(0).getType() == FieldSelectionType.FIELD)
            subquery.select(this.manager.getCriteriaBuilder().greatest(this.nestedQueryRoot.<U>get(fields.get(0).getField())));
        else if (fields.get(0).getType() == FieldSelectionType.COMPOSITE_FIELD) {
            subquery.select(this.manager.getCriteriaBuilder().greatest(this.nestedQueryRoot.get(fields.get(0).getField().split(":")[0]).<U>get(fields.get(0).getField().split(":")[1])));
        }
        return subquery;
    }

    @Override
    public <U extends
            Comparable> Subquery<U> subQueryMax(NestedQuerySinglePredicate<T> predicate, List<SelectionField> fields, Class<U> uClass) {
        Subquery<U> subquery = this.manager.getCriteriaBuilder().createQuery().subquery(uClass);
        this.nestedQueryRoot = subquery.from(this.tClass);
        subquery.where(predicate.applyPredicate(this.manager.getCriteriaBuilder(), this.root, this.nestedQueryRoot));
        if (fields.get(0).getType() == FieldSelectionType.FIELD)
            subquery.select(this.manager.getCriteriaBuilder().greatest(this.nestedQueryRoot.<U>get(fields.get(0).getField())));
        else if (fields.get(0).getType() == FieldSelectionType.COMPOSITE_FIELD) {
            subquery.select(this.manager.getCriteriaBuilder().greatest(this.nestedQueryRoot.get(fields.get(0).getField().split(":")[0]).<U>get(fields.get(0).getField().split(":")[1])));
        }
        return subquery;
    }

    public <U> QueryableList<T> initSubQuery(Class<U> uClass) {
        this.subquery = this.manager.getCriteriaBuilder().createQuery().subquery(uClass);
        this.nestedQueryRoot = subquery.from(this.tClass);
        return this;
    }

    @Override
    public Subquery<T> query(List<SelectionField> fields) throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager.getCriteriaBuilder();
        Subquery<T> query = builder.createQuery().subquery(this.tClass);
        this.root = query.from(this.tClass);
        query.where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        if (fields.get(0).getType() == FieldSelectionType.FIELD)
            query.select(this.root.get(fields.get(0).getField()));
        else if (fields.get(0).getType() == FieldSelectionType.COMPOSITE_FIELD) {
            query.select(this.root.get(fields.get(0).getField().split(":")[0]).get(fields.get(0).getField().split(":")[1]));
        }
        if (distinct)
            query.distinct(true);
        return query;
    }

    @Override
    public <V> void update(EntitySelectPredicate<T> selectPredicate, V value) throws InvalidApplicationException {
        CriteriaBuilder builder = this.manager
                .getCriteriaBuilder();
        CriteriaUpdate<T> update = builder
                .createCriteriaUpdate(tClass);
        this.root = update.from(tClass);
        update.set(selectPredicate.applyPredicate(root), value)
                .where(this.generateWherePredicates(this.singlePredicates, this.root, this.nestedPredicates, this.nestedQueryRoot));
        this.manager
                .createQuery(update)
                .executeUpdate();
    }

    private Path[] parseFields(List<String> selectedFields) {
        List<Path> paths = new ArrayList<>();
        selectedFields.forEach(s -> paths.add(root.get(s)));

        return paths.toArray(new Path[0]);
    }
}
