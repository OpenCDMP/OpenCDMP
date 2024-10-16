package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
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
public class NestedDescriptionTemplateElasticQuery extends ElasticNestedQuery<NestedDescriptionTemplateElasticQuery, NestedDescriptionTemplateElasticEntity, UUID> {

	private Collection<UUID> ids;
	private Collection<UUID> groupIds;
	private Collection<DescriptionTemplateVersionStatus> versionStatuses;
	private Collection<UUID> excludedIds;
	private Collection<UUID> excludedGroupIds;

	private String nestedPath;

	public NestedDescriptionTemplateElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery ids(Collection<UUID> value) {
		this.ids = value;
		return this;
	}

	public NestedDescriptionTemplateElasticQuery groupIds(UUID value) {
		this.groupIds = List.of(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery groupIds(UUID... value) {
		this.groupIds = Arrays.asList(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery groupIds(Collection<UUID> value) {
		this.groupIds = value;
		return this;
	}

	public NestedDescriptionTemplateElasticQuery versionStatuses(DescriptionTemplateVersionStatus value) {
		this.versionStatuses = List.of(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery versionStatuses(DescriptionTemplateVersionStatus... value) {
		this.versionStatuses = Arrays.asList(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery versionStatuses(Collection<DescriptionTemplateVersionStatus> value) {
		this.versionStatuses = value;
		return this;
	}

	public NestedDescriptionTemplateElasticQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery excludedIds(Collection<UUID> value) {
		this.excludedIds = value;
		return this;
	}

	public NestedDescriptionTemplateElasticQuery excludedGroupIds(UUID value) {
		this.excludedGroupIds = List.of(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery excludedGroupIds(UUID... value) {
		this.excludedGroupIds = Arrays.asList(value);
		return this;
	}

	public NestedDescriptionTemplateElasticQuery excludedGroupIds(Collection<UUID> value) {
		this.excludedGroupIds = value;
		return this;
	}

	@Override
	public NestedDescriptionTemplateElasticQuery nestedPath(String value) {
		this.nestedPath = value;
		return this;
	}


	public NestedDescriptionTemplateElasticQuery(
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
		return false;
	}

	@Override
	protected Query applyAuthZ() {
		return null;
	}

	@Override
	protected Query applyFilters() {
		List<Query> predicates= new ArrayList<>();
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._id), this.ids)._toQuery());
		}
		if (this.groupIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._groupId), this.groupIds)._toQuery());
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._id), this.ids)._toQuery())._toQuery());
		}
		if (this.excludedGroupIds != null) {
			predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._groupId), this.groupIds)._toQuery())._toQuery());
		}
		if (this.versionStatuses != null) {
			predicates.add(this.contains(this.elasticFieldOf(NestedDescriptionTemplateElasticEntity._versionStatus), this.versionStatuses.stream().map(DescriptionTemplateVersionStatus::getValue).toList().toArray(new Short[this.versionStatuses.size()]))._toQuery());
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
		if (columns.contains(NestedDescriptionTemplateElasticEntity._groupId)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedDescriptionTemplateElasticEntity._groupId), UUID.class));
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
	protected String getNestedPath() {
		return this.nestedPath;
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
