package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticFields;
import gr.cite.tools.elastic.query.ElasticInnerObjectQuery;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.opencdmp.elastic.data.nested.NestedPlanElasticEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
//Like in C# make it Transient
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class InnerObjectPlanElasticQuery extends ElasticInnerObjectQuery<InnerObjectPlanElasticQuery, NestedPlanElasticEntity, UUID> {
	private String like;
	private Collection<UUID> ids;
	private Collection<UUID> excludedIds;
	private Collection<UUID> statusIds;
	private Collection<PlanVersionStatus> versionStatuses;
	private Collection<PlanAccessType> accessTypes;
	private Collection<Integer> versions;
	private Collection<UUID> groupIds;
	private NestedCollaboratorElasticQuery planUserSubQuery;


	public InnerObjectPlanElasticQuery planSubQuery(NestedCollaboratorElasticQuery subQuery) {
		this.planUserSubQuery = subQuery;
		return this;
	}

	public InnerObjectPlanElasticQuery like(String value) {
		this.like = value;
		return this;
	}

	public InnerObjectPlanElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public InnerObjectPlanElasticQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public InnerObjectPlanElasticQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery versionStatuses(PlanVersionStatus value) {
		this.versionStatuses = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery versionStatuses(PlanVersionStatus... value) {
		this.versionStatuses = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery versionStatuses(Collection<PlanVersionStatus> values) {
		this.versionStatuses = values;
		return this;
	}

	public InnerObjectPlanElasticQuery accessTypes(PlanAccessType value) {
		this.accessTypes = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery accessTypes(PlanAccessType... value) {
		this.accessTypes = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery accessTypes(Collection<PlanAccessType> values) {
		this.accessTypes = values;
		return this;
	}

	public InnerObjectPlanElasticQuery statusIds(UUID value) {
		this.statusIds = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery statusIds(UUID... value) {
		this.statusIds = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery statusIds(Collection<UUID> values) {
		this.statusIds = values;
		return this;
	}

	public InnerObjectPlanElasticQuery versions(Integer value) {
		this.versions = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery versions(Integer... value) {
		this.versions = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery versions(Collection<Integer> values) {
		this.versions = values;
		return this;
	}

	public InnerObjectPlanElasticQuery groupIds(UUID value) {
		this.groupIds = List.of(value);
		return this;
	}

	public InnerObjectPlanElasticQuery groupIds(UUID... value) {
		this.groupIds = Arrays.asList(value);
		return this;
	}

	public InnerObjectPlanElasticQuery groupIds(Collection<UUID> values) {
		this.groupIds = values;
		return this;
	}

	private String innerPath;

	@Override
	public InnerObjectPlanElasticQuery innerPath(String value) {
		this.innerPath = value;
		return this;
	}


	private final QueryFactory queryFactory;
	@Autowired
	public InnerObjectPlanElasticQuery(ElasticsearchTemplate elasticsearchTemplate, ElasticProperties elasticProperties, QueryFactory queryFactory) {
		super(elasticsearchTemplate, elasticProperties);
		this.queryFactory = queryFactory;
	}

	@Override
	protected Class<NestedPlanElasticEntity> entityClass() {
		return NestedPlanElasticEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return false;
	}

	@Override
	protected Query applyFilters() {
		List<Query> predicates = new ArrayList<>();

		if (this.like != null && !this.like.isBlank()) {
			if (!this.like.startsWith("*")) this.like = "*" + this.like;
			if (!this.like.endsWith("*")) this.like = this.like + "*";
			ElasticFields elasticFields = new ElasticFields(this.entityClass(), List.of(this.getInnerPath()));
			elasticFields.add(NestedPlanElasticEntity._label, true);
			elasticFields.add(NestedPlanElasticEntity._description, true);
			predicates.add(this.like(elasticFields, List.of(this.like))._toQuery());
		}
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanElasticEntity._id).disableInfer(true), this.ids)._toQuery());
		}
		if (this.groupIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanElasticEntity._groupId).disableInfer(true), this.groupIds)._toQuery());
		}
		if (this.versions != null) {
			predicates.add(this.contains(this.elasticFieldOf(NestedPlanElasticEntity._version).disableInfer(true), this.versions.toArray(new Integer[this.versions.size()]))._toQuery());
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsUUID(this.elasticFieldOf(NestedPlanElasticEntity._id).disableInfer(true), this.excludedIds)._toQuery())._toQuery());
		}
		if (this.statusIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(NestedPlanElasticEntity._id).disableInfer(true), this.statusIds)._toQuery());
		}
		if (this.versionStatuses != null) {
			predicates.add(this.contains(this.elasticFieldOf(NestedPlanElasticEntity._versionStatus).disableInfer(true), this.versionStatuses.stream().map(x-> x.getValue()).collect(Collectors.toList()).toArray(new Short[this.versionStatuses.size()]))._toQuery());
		}
		if (this.accessTypes != null) {
			predicates.add(this.contains(this.elasticFieldOf(NestedPlanElasticEntity._accessType).disableInfer(true), this.accessTypes.stream().map(x-> x.getValue()).collect(Collectors.toList()).toArray(new Short[this.accessTypes.size()]))._toQuery());
		}
		if (this.planUserSubQuery != null) {
			predicates.add(this.nestedQuery( this.planUserSubQuery.nestedPath(DescriptionElasticEntity._plan + "."  + NestedPlanElasticEntity._collaborators)).build()._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public NestedPlanElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		NestedPlanElasticEntity mocDoc = new NestedPlanElasticEntity();
		if (columns.contains(NestedPlanElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._id), UUID.class));
		if (columns.contains(NestedPlanElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._label), String.class));
		if (columns.contains(NestedPlanElasticEntity._description)) mocDoc.setDescription(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._description), String.class));
		if (columns.contains(NestedPlanElasticEntity._statusId)) mocDoc.setStatusId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._statusId), UUID.class));
		if (columns.contains(NestedPlanElasticEntity._versionStatus)) mocDoc.setVersionStatus(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._versionStatus), PlanVersionStatus.class));
		if (columns.contains(NestedPlanElasticEntity._version)) mocDoc.setVersion(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._version), Short.class));
		if (columns.contains(NestedPlanElasticEntity._groupId)) mocDoc.setGroupId(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._groupId), UUID.class));
		if (columns.contains(NestedPlanElasticEntity._accessType)) mocDoc.setAccessType(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._accessType), PlanAccessType.class));
		if (columns.contains(NestedPlanElasticEntity._finalizedAt)) mocDoc.setFinalizedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(NestedPlanElasticEntity._finalizedAt), Date.class));
		mocDoc.setCollaborators(this.convertNested(rawData, columns, this.queryFactory.query(NestedCollaboratorElasticQuery.class), NestedPlanElasticEntity._collaborators, null));
		mocDoc.setReferences(this.convertNested(rawData, columns, this.queryFactory.query(NestedReferenceElasticQuery.class), NestedPlanElasticEntity._references, null));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(NestedPlanElasticEntity._id)) return this.elasticFieldOf(NestedPlanElasticEntity._id);
		else if (item.match(NestedPlanElasticEntity._label)) return this.elasticFieldOf(NestedPlanElasticEntity._label);
		else if (item.match(NestedPlanElasticEntity._description)) return this.elasticFieldOf(NestedPlanElasticEntity._description);
		else if (item.match(NestedPlanElasticEntity._statusId)) return this.elasticFieldOf(NestedPlanElasticEntity._statusId);
		else if (item.match(NestedPlanElasticEntity._versionStatus)) return this.elasticFieldOf(NestedPlanElasticEntity._versionStatus);
		else if (item.match(NestedPlanElasticEntity._version)) return this.elasticFieldOf(NestedPlanElasticEntity._version);
		else if (item.match(NestedPlanElasticEntity._groupId)) return this.elasticFieldOf(NestedPlanElasticEntity._groupId);
		else if (item.match(NestedPlanElasticEntity._finalizedAt)) return this.elasticFieldOf(NestedPlanElasticEntity._finalizedAt);
		else if (item.match(NestedPlanElasticEntity._accessType)) return this.elasticFieldOf(NestedPlanElasticEntity._accessType);
		else if (item.prefix(NestedPlanElasticEntity._collaborators)) return this.queryFactory.query(NestedCollaboratorElasticQuery.class).nestedPath(NestedPlanElasticEntity._collaborators).fieldNameOf(this.extractPrefixed(item, NestedPlanElasticEntity._collaborators));
		else if (item.prefix(NestedPlanElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(NestedPlanElasticEntity._references).fieldNameOf(this.extractPrefixed(item, NestedPlanElasticEntity._references));
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
		return this.elasticFieldOf(NestedPlanElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		if (item.prefix(NestedPlanElasticEntity._collaborators)) return this.queryFactory.query(NestedCollaboratorElasticQuery.class).nestedPath(NestedPlanElasticEntity._collaborators);
		else if (item.prefix(NestedPlanElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(NestedPlanElasticEntity._references);
		else  return null;
	}
}
