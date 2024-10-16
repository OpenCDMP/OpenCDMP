package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.elastic.data.nested.NestedPlanDescriptionTemplateElasticEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedPlanDescriptionTemplateElasticQuery extends ElasticNestedQuery<NestedPlanDescriptionTemplateElasticQuery, NestedPlanDescriptionTemplateElasticEntity, UUID> {
	private Collection<UUID> ids;
	private Collection<UUID> excludedIds;
	private Collection<UUID> descriptionTemplateGroupIds;
	private Collection<UUID> planIds;
	private Collection<UUID> sectionIds;
	
	private String nestedPath;

	public NestedPlanDescriptionTemplateElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery planIds(UUID value) {
		this.planIds = List.of(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery planIds(UUID... value) {
		this.planIds = Arrays.asList(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery planIds(Collection<UUID> values) {
		this.planIds = values;
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery descriptionTemplateGroupIds(UUID value) {
		this.descriptionTemplateGroupIds = List.of(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery descriptionTemplateGroupIds(UUID... value) {
		this.descriptionTemplateGroupIds = Arrays.asList(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery descriptionTemplateGroupIds(Collection<UUID> values) {
		this.descriptionTemplateGroupIds = values;
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery sectionIds(UUID value) {
		this.sectionIds = List.of(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery sectionIds(UUID... value) {
		this.sectionIds = Arrays.asList(value);
		return this;
	}

	public NestedPlanDescriptionTemplateElasticQuery sectionIds(Collection<UUID> values) {
		this.sectionIds = values;
		return this;
	}

	@Override
	public NestedPlanDescriptionTemplateElasticQuery nestedPath(String value) {
		this.nestedPath = value;
		return this;
	}


	public NestedPlanDescriptionTemplateElasticQuery(
			ElasticsearchTemplate elasticsearchRestTemplate,
			ElasticProperties elasticProperties
	) {
		super(elasticsearchRestTemplate, elasticProperties);
	}

	@Override
	protected Class<NestedPlanDescriptionTemplateElasticEntity> entityClass() {
		return NestedPlanDescriptionTemplateElasticEntity.class;
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
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._id), this.ids)._toQuery());
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._id), this.excludedIds)._toQuery())._toQuery());
		}

		if (this.descriptionTemplateGroupIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId), this.descriptionTemplateGroupIds)._toQuery());
		}

		if (this.sectionIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._sectionId), this.sectionIds)._toQuery());
		}

		if (this.planIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._planId), this.planIds)._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public NestedPlanDescriptionTemplateElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NestedPlanDescriptionTemplateElasticEntity mocDoc = new NestedPlanDescriptionTemplateElasticEntity();
		if (columns.contains(NestedPlanDescriptionTemplateElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanDescriptionTemplateElasticEntity._id), UUID.class));
		if (columns.contains(NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId), UUID.class));
		if (columns.contains(NestedPlanDescriptionTemplateElasticEntity._sectionId)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanDescriptionTemplateElasticEntity._sectionId), UUID.class));
		if (columns.contains(NestedPlanDescriptionTemplateElasticEntity._planId)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanDescriptionTemplateElasticEntity._planId), UUID.class));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NestedPlanDescriptionTemplateElasticEntity._id)) return this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._id).disableInfer(true);
		else if (item.match(NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId)) return this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId).disableInfer(true);
		else if (item.match(NestedPlanDescriptionTemplateElasticEntity._sectionId)) return this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._sectionId).disableInfer(true);
		else if (item.match(NestedPlanDescriptionTemplateElasticEntity._planId)) return this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._planId).disableInfer(true);
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
		return this.elasticFieldOf(NestedPlanDescriptionTemplateElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		return null;
	}
}
