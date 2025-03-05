package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.elastic.data.nested.NestedDescriptionElasticEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedDescriptionElasticQuery extends ElasticNestedQuery<NestedDescriptionElasticQuery, NestedDescriptionElasticEntity, UUID> {

	private String nestedPath;
	private InnerObjectDescriptionTemplateElasticQuery descriptionTemplateSubQuery;



	public NestedDescriptionElasticQuery descriptionTemplateSubQuery(InnerObjectDescriptionTemplateElasticQuery subQuery) {
		this.descriptionTemplateSubQuery = subQuery;
		return this;
	}

	@Override
	public NestedDescriptionElasticQuery nestedPath(String value) {
		this.nestedPath = value;
		return this;
	}

	private final QueryFactory queryFactory;
	private final ConventionService conventionService;
	public NestedDescriptionElasticQuery(
			ElasticsearchTemplate elasticsearchRestTemplate,
			ElasticProperties elasticProperties,
			QueryFactory queryFactory, ConventionService conventionService) {
		super(elasticsearchRestTemplate, elasticProperties);
		this.queryFactory = queryFactory;
		this.conventionService = conventionService;
	}

	@Override
	protected Class<NestedDescriptionElasticEntity> entityClass() {
		return NestedDescriptionElasticEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return false;
	}

	@Override
	protected Query applyAuthZ() {
		return null;
	}

	@Override
	protected Query applyFilters() {
		List<Query> predicates = new ArrayList<>();

		if (this.descriptionTemplateSubQuery != null) {
			predicates.add(this.descriptionTemplateSubQuery.innerPath(this.conventionService.asIndexer(this.getNestedPath(), NestedDescriptionElasticEntity._descriptionTemplate)).applyFilters());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public NestedDescriptionElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NestedDescriptionElasticEntity mocDoc = new NestedDescriptionElasticEntity();
		if (columns.contains(NestedDescriptionElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionElasticEntity._id), UUID.class));
		if (columns.contains(NestedDescriptionElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionElasticEntity._label), String.class));
		if (columns.contains(NestedDescriptionElasticEntity._planId)) mocDoc.setPlanId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionElasticEntity._planId), UUID.class));
		if (columns.contains(NestedDescriptionElasticEntity._description)) mocDoc.setDescription(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionElasticEntity._description), String.class));
		if (columns.contains(NestedDescriptionElasticEntity._statusId)) mocDoc.setStatusId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionElasticEntity._statusId), UUID.class));
		if (columns.contains(NestedDescriptionElasticEntity._finalizedAt)) mocDoc.setFinalizedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionElasticEntity._finalizedAt), Date.class));
		mocDoc.setReferences(this.convertNested(rawData, columns, this.queryFactory.query(NestedReferenceElasticQuery.class), NestedDescriptionElasticEntity._references, this.getNestedPath()));
		mocDoc.setTags(this.convertNested(rawData, columns, this.queryFactory.query(NestedTagElasticQuery.class), NestedDescriptionElasticEntity._tags, this.getNestedPath()));
		mocDoc.setDescriptionTemplate(this.convertInnerObject(rawData, columns, this.queryFactory.query(InnerObjectDescriptionTemplateElasticQuery.class), NestedDescriptionElasticEntity._descriptionTemplate, null));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NestedDescriptionElasticEntity._id)) return this.elasticFieldOf(NestedDescriptionElasticEntity._id);
		else if (item.match(NestedDescriptionElasticEntity._label)) return this.elasticFieldOf(NestedDescriptionElasticEntity._label);
		else if (item.match(NestedDescriptionElasticEntity._planId)) return this.elasticFieldOf(NestedDescriptionElasticEntity._planId);
		else if (item.match(NestedDescriptionElasticEntity._description)) return this.elasticFieldOf(NestedDescriptionElasticEntity._description);
		else if (item.match(NestedDescriptionElasticEntity._statusId)) return this.elasticFieldOf(NestedDescriptionElasticEntity._statusId);
		else if (item.match(NestedDescriptionElasticEntity._finalizedAt)) return this.elasticFieldOf(NestedDescriptionElasticEntity._finalizedAt);
		else if (item.prefix(NestedDescriptionElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(this.conventionService.asIndexer(this.getNestedPath(), NestedDescriptionElasticEntity._references)).fieldNameOf(this.extractPrefixed(item, NestedDescriptionElasticEntity._references));
		else if (item.prefix(NestedDescriptionElasticEntity._tags)) return this.queryFactory.query(NestedTagElasticQuery.class).nestedPath(this.conventionService.asIndexer(this.getNestedPath(), NestedDescriptionElasticEntity._tags)).fieldNameOf(this.extractPrefixed(item, NestedDescriptionElasticEntity._tags));
		else if (item.prefix(NestedDescriptionElasticEntity._descriptionTemplate)) return this.queryFactory.query(InnerObjectDescriptionTemplateElasticQuery.class).innerPath(this.conventionService.asIndexer(this.getNestedPath(), NestedDescriptionElasticEntity._descriptionTemplate)).fieldNameOf(this.extractPrefixed(item, NestedDescriptionElasticEntity._description));
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
		return this.elasticFieldOf(NestedDescriptionElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		if (item.prefix(NestedDescriptionElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(this.conventionService.asIndexer(this.getNestedPath(), NestedDescriptionElasticEntity._references));
		else if (item.prefix(NestedDescriptionElasticEntity._tags)) return this.queryFactory.query(NestedTagElasticQuery.class).nestedPath(this.conventionService.asIndexer(this.getNestedPath(), NestedDescriptionElasticEntity._tags));
		else return null;
	}
}
