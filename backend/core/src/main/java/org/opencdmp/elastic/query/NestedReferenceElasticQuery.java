package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.opencdmp.elastic.data.nested.NestedReferenceElasticEntity;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedReferenceElasticQuery extends ElasticNestedQuery<NestedReferenceElasticQuery, NestedReferenceElasticEntity, UUID> {

	private Collection<UUID> ids;
	private String nestedPath;

	public NestedReferenceElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public NestedReferenceElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NestedReferenceElasticQuery ids(Collection<UUID> value) {
		this.ids = value;
		return this;
	}

	@Override
	public NestedReferenceElasticQuery nestedPath(String value) {
		this.nestedPath = value;
		return this;
	}

	public NestedReferenceElasticQuery(
			ElasticsearchTemplate elasticsearchTemplate,
			ElasticProperties elasticProperties
	) {
		super(elasticsearchTemplate, elasticProperties);
	}

	@Override
	protected Class<NestedReferenceElasticEntity> entityClass() {
		return NestedReferenceElasticEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids);
	}

	@Override
	protected Query applyAuthZ() {
		return null;
	}

	@Override
	protected Query applyFilters() {
		List<Query> predicates= new ArrayList<>();
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedReferenceElasticEntity._id), this.ids)._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public NestedReferenceElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NestedReferenceElasticEntity mocDoc = new NestedReferenceElasticEntity();
		if (columns.contains(NestedReferenceElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedReferenceElasticEntity._id), UUID.class));
		if (columns.contains(NestedReferenceElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedReferenceElasticEntity._label), String.class));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NestedReferenceElasticEntity._id)) return this.elasticFieldOf(NestedReferenceElasticEntity._id).disableInfer(true);
		else if (item.match(NestedReferenceElasticEntity._label)) return this.elasticFieldOf(NestedReferenceElasticEntity._label).disableInfer(true);
		else return null;
	}

	@Override
	protected String getNestedPath() {
		return this.nestedPath;
	}

	@Override
	protected UUID toKey(String key) {
		return UUID.fromString(key);
	}

	@Override
	protected ElasticField getKeyField() {
		return this.elasticFieldOf(NestedReferenceElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		return null;
	}
}
