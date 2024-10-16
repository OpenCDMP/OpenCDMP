package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.elastic.data.nested.NestedCollaboratorElasticEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NestedCollaboratorElasticQuery extends ElasticNestedQuery<NestedCollaboratorElasticQuery, NestedCollaboratorElasticEntity, UUID> {
	private Collection<UUID> ids;
	private Collection<UUID> userIds;


	private Collection<PlanUserRole> userRoles;
	
	public NestedCollaboratorElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public NestedCollaboratorElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NestedCollaboratorElasticQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public NestedCollaboratorElasticQuery userIds(UUID value) {
		this.userIds = List.of(value);
		return this;
	}

	public NestedCollaboratorElasticQuery userIds(UUID... value) {
		this.userIds = Arrays.asList(value);
		return this;
	}

	public NestedCollaboratorElasticQuery userIds(Collection<UUID> values) {
		this.userIds = values;
		return this;
	}

	public NestedCollaboratorElasticQuery userRoles(PlanUserRole value) {
		this.userRoles = List.of(value);
		return this;
	}

	public NestedCollaboratorElasticQuery userRoles(PlanUserRole... value) {
		this.userRoles = Arrays.asList(value);
		return this;
	}

	public NestedCollaboratorElasticQuery userRoles(Collection<PlanUserRole> values) {
		this.userRoles = values;
		return this;
	}
	private String nestedPath;

	@Override
	public NestedCollaboratorElasticQuery nestedPath(String value) {
		this.nestedPath = value;
		return this;
	}


	public NestedCollaboratorElasticQuery(
			ElasticsearchTemplate elasticsearchRestTemplate,
			ElasticProperties elasticProperties
	) {
		super(elasticsearchRestTemplate, elasticProperties);
	}

	@Override
	protected Class<NestedCollaboratorElasticEntity> entityClass() {
		return NestedCollaboratorElasticEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.userIds) ||this.isEmpty(this.userRoles);
	}

	@Override
	protected Query applyAuthZ() {
		return null;
	}

	@Override
	protected Query applyFilters() {
		List<Query> predicates = new ArrayList<>();
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedCollaboratorElasticEntity._id), this.ids)._toQuery());
		}
		if (this.userIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedCollaboratorElasticEntity._userId), this.userIds)._toQuery());
		}

		if (this.userRoles != null) {
			predicates.add(this.contains(this.elasticFieldOf(NestedCollaboratorElasticEntity._role), this.userRoles.stream().map(PlanUserRole::getValue).toList().toArray(new Short[this.userRoles.size()]))._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public NestedCollaboratorElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NestedCollaboratorElasticEntity mocDoc = new NestedCollaboratorElasticEntity();
		if (columns.contains(NestedCollaboratorElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedCollaboratorElasticEntity._id), UUID.class));
		if (columns.contains(NestedCollaboratorElasticEntity._userId)) mocDoc.setUserId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedCollaboratorElasticEntity._userId), UUID.class));
		if (columns.contains(NestedCollaboratorElasticEntity._name)) mocDoc.setName(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedCollaboratorElasticEntity._name), String.class));
		if (columns.contains(NestedCollaboratorElasticEntity._role)) mocDoc.setRole(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedCollaboratorElasticEntity._role), PlanUserRole.class));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NestedCollaboratorElasticEntity._id)) return this.elasticFieldOf(NestedCollaboratorElasticEntity._id).disableInfer(true);
		else if (item.match(NestedCollaboratorElasticEntity._userId)) return this.elasticFieldOf(NestedCollaboratorElasticEntity._userId).disableInfer(true);
		else if (item.match(NestedCollaboratorElasticEntity._name)) return this.elasticFieldOf(NestedCollaboratorElasticEntity._name).disableInfer(true);
		else if (item.match(NestedCollaboratorElasticEntity._role)) return this.elasticFieldOf(NestedCollaboratorElasticEntity._role).disableInfer(true);
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
		return this.elasticFieldOf(NestedCollaboratorElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		return null;
	}
}
