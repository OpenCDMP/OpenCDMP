package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticInnerObjectQuery;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.commons.enums.DescriptionTemplateVersionStatus;
import org.opencdmp.elastic.data.nested.NestedDescriptionTemplateElasticEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InnerObjectDescriptionTemplateElasticQuery extends ElasticInnerObjectQuery<InnerObjectDescriptionTemplateElasticQuery, NestedDescriptionTemplateElasticEntity, UUID> {
	private Collection<UUID> groupIds;

	private String innerPath;

	@Override
	public InnerObjectDescriptionTemplateElasticQuery innerPath(String value) {
		this.innerPath = value;
		return this;
	}

	public InnerObjectDescriptionTemplateElasticQuery groupIds(UUID value) {
		this.groupIds = List.of(value);
		return this;
	}

	public InnerObjectDescriptionTemplateElasticQuery groupIds(UUID... value) {
		this.groupIds = Arrays.asList(value);
		return this;
	}

	public InnerObjectDescriptionTemplateElasticQuery groupIds(Collection<UUID> values) {
		this.groupIds = values;
		return this;
	}


	public InnerObjectDescriptionTemplateElasticQuery(
			ElasticsearchTemplate elasticsearchRestTemplate,
			ElasticProperties elasticProperties
	) {
		super(elasticsearchRestTemplate, elasticProperties);
	}

	@Override
	protected Class<NestedDescriptionTemplateElasticEntity> entityClass() {
		return NestedDescriptionTemplateElasticEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.groupIds);
	}

	@Override
	protected Query applyAuthZ() {
		return null;
	}

	@Override
	protected Query applyFilters() {
		List<Query> predicates = new ArrayList<>();
		if (this.groupIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._groupId).disableInfer(true), this.groupIds)._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public NestedDescriptionTemplateElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NestedDescriptionTemplateElasticEntity mocDoc = new NestedDescriptionTemplateElasticEntity();
		if (columns.contains(NestedDescriptionTemplateElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionTemplateElasticEntity._id), UUID.class));
		if (columns.contains(NestedDescriptionTemplateElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionTemplateElasticEntity._label), String.class));
		if (columns.contains(NestedDescriptionTemplateElasticEntity._groupId)) mocDoc.setGroupId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionTemplateElasticEntity._groupId), UUID.class));
		if (columns.contains(NestedDescriptionTemplateElasticEntity._versionStatus)) mocDoc.setVersionStatus(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionTemplateElasticEntity._versionStatus), DescriptionTemplateVersionStatus.class));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NestedDescriptionTemplateElasticEntity._id)) return this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._id).disableInfer(true);
		else if (item.match(NestedDescriptionTemplateElasticEntity._label)) return this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._label).disableInfer(true);
		else if (item.match(NestedDescriptionTemplateElasticEntity._groupId)) return this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._groupId).disableInfer(true);
		else if (item.match(NestedDescriptionTemplateElasticEntity._versionStatus)) return this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._versionStatus).disableInfer(true);
		else return null;
	}

	@Override
	protected String getInnerPath() {
		return this.innerPath;
	}

	@Override
	protected UUID toKey(String key) {
		return UUID.fromString(key);
	}

	@Override
	protected ElasticField getKeyField() {
		return this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		return null;
	}
}
