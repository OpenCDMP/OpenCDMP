package org.opencdmp.query.utils;

import gr.cite.tools.data.query.QueryContext;
import jakarta.persistence.criteria.*;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BuildSubQueryInput<Entity, Key> {
	private final AbstractQuery<?> query;
	private final CriteriaBuilder criteriaBuilder;
	private final Class<Entity> entityType;
	private final Class<Key> keyType;
	private final Function<Root<Entity>, Expression<Key>> keyPathFunc;
	private final BiFunction<Root<Entity>, CriteriaBuilder, Predicate> filterFunc;

	public BuildSubQueryInput(Builder<Entity, Key> builder) {
		query = builder.query;
		criteriaBuilder = builder.criteriaBuilder;
		entityType = builder.entityType;
		keyType = builder.keyType;
		keyPathFunc = builder.keyPathFunc;
		filterFunc = builder.filterFunc;
	}

	public AbstractQuery<?> getQuery() {
		return query;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return criteriaBuilder;
	}

	public Class<Entity> getEntityType() {
		return entityType;
	}

	public Class<Key> getKeyType() {
		return keyType;
	}

	public Function<Root<Entity>, Expression<Key>> getKeyPathFunc() {
		return keyPathFunc;
	}

	public BiFunction<Root<Entity>, CriteriaBuilder, Predicate> getFilterFunc() {
		return filterFunc;
	}

	public static class Builder<Entity, Key> {
		private final Class<Entity> entityType;
		private final Class<Key> keyType;
		private AbstractQuery<?> query;
		private CriteriaBuilder criteriaBuilder;
		private Function<Root<Entity>, Expression<Key>> keyPathFunc;
		private BiFunction<Root<Entity>, CriteriaBuilder, Predicate> filterFunc;

		public Builder(Class<Entity> entityType, Class<Key> keyType) {
			this.entityType = entityType;
			this.keyType = keyType;
		}

		public Builder(Class<Entity> entityType, Class<Key> keyType, QueryContext<?, ?> queryContext) {
			this.entityType = entityType;
			this.keyType = keyType;
			this.query = queryContext.Query;
			this.criteriaBuilder = queryContext.CriteriaBuilder;
		}

		public Builder<Entity, Key> query(AbstractQuery<?> query) {
			this.query = query;
			return this;
		}

		public Builder<Entity, Key> criteriaBuilder(CriteriaBuilder criteriaBuilder) {
			this.criteriaBuilder = criteriaBuilder;
			return this;
		}

		public Builder<Entity, Key> keyPathFunc(Function<Root<Entity>, Expression<Key>> keyPathFunc) {
			this.keyPathFunc = keyPathFunc;
			return this;
		}

		public Builder<Entity, Key> filterFunc(BiFunction<Root<Entity>, CriteriaBuilder, Predicate> filterFunc) {
			this.filterFunc = filterFunc;
			return this;
		}
	}
}
