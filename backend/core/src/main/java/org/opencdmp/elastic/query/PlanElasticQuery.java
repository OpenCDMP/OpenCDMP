package org.opencdmp.elastic.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.OrderingFieldResolver;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.elastic.ElasticConstants;
import gr.cite.tools.elastic.configuration.ElasticProperties;
import gr.cite.tools.elastic.mapper.FieldBasedMapper;
import gr.cite.tools.elastic.query.ElasticField;
import gr.cite.tools.elastic.query.ElasticFields;
import gr.cite.tools.elastic.query.ElasticNestedQuery;
import gr.cite.tools.elastic.query.ElasticQuery;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.elastic.data.PlanElasticEntity;
import org.opencdmp.elastic.data.nested.NestedDescriptionElasticEntity;
import org.opencdmp.service.elastic.AppElasticConfiguration;
import org.opencdmp.service.elastic.ElasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.*;

@Component
//Like in C# make it Transient
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PlanElasticQuery extends ElasticQuery<PlanElasticEntity, UUID> {

	private String like;
	private Collection<UUID> ids;
	private Collection<UUID> excludedIds;
	private Collection<PlanStatus> statuses;
	private Collection<PlanVersionStatus> versionStatuses;
	private Collection<PlanAccessType> accessTypes;
	private Collection<Integer> versions;
	private Collection<UUID> groupIds;
	private Collection<UUID> blueprintIds;
	private NestedCollaboratorElasticQuery planUserSubQuery;
	private NestedDescriptionElasticQuery descriptionSubQuery;
	private NestedPlanDescriptionTemplateElasticQuery planDescriptionTemplateSubQuery;
	private NestedReferenceElasticQuery referenceSubQuery;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


	public PlanElasticQuery planSubQuery(NestedCollaboratorElasticQuery subQuery) {
		this.planUserSubQuery = subQuery;
		return this;
	}
	
	public PlanElasticQuery descriptionSubQuery(NestedDescriptionElasticQuery subQuery) {
		this.descriptionSubQuery = subQuery;
		return this;
	}

	public PlanElasticQuery planDescriptionTemplateSubQuery(NestedPlanDescriptionTemplateElasticQuery subQuery) {
		this.planDescriptionTemplateSubQuery = subQuery;
		return this;
	}

	public PlanElasticQuery referenceSubQuery(NestedReferenceElasticQuery subQuery) {
		this.referenceSubQuery = subQuery;
		return this;
	}
	
	public PlanElasticQuery like(String value) {
		this.like = value;
		return this;
	}

	public PlanElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public PlanElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public PlanElasticQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public PlanElasticQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public PlanElasticQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery versionStatuses(PlanVersionStatus value) {
		this.versionStatuses = List.of(value);
		return this;
	}

	public PlanElasticQuery versionStatuses(PlanVersionStatus... value) {
		this.versionStatuses = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery versionStatuses(Collection<PlanVersionStatus> values) {
		this.versionStatuses = values;
		return this;
	}

	public PlanElasticQuery accessTypes(PlanAccessType value) {
		this.accessTypes = List.of(value);
		return this;
	}

	public PlanElasticQuery accessTypes(PlanAccessType... value) {
		this.accessTypes = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery accessTypes(Collection<PlanAccessType> values) {
		this.accessTypes = values;
		return this;
	}

	public PlanElasticQuery statuses(PlanStatus value) {
		this.statuses = List.of(value);
		return this;
	}

	public PlanElasticQuery statuses(PlanStatus... value) {
		this.statuses = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery statuses(Collection<PlanStatus> values) {
		this.statuses = values;
		return this;
	}

	public PlanElasticQuery versions(Integer value) {
		this.versions = List.of(value);
		return this;
	}

	public PlanElasticQuery versions(Integer... value) {
		this.versions = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery versions(Collection<Integer> values) {
		this.versions = values;
		return this;
	}

	public PlanElasticQuery groupIds(UUID value) {
		this.groupIds = List.of(value);
		return this;
	}

	public PlanElasticQuery groupIds(UUID... value) {
		this.groupIds = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery groupIds(Collection<UUID> values) {
		this.groupIds = values;
		return this;
	}

	public PlanElasticQuery blueprintIds(UUID value) {
		this.blueprintIds = List.of(value);
		return this;
	}

	public PlanElasticQuery blueprintIds(UUID... value) {
		this.blueprintIds = Arrays.asList(value);
		return this;
	}

	public PlanElasticQuery blueprintIds(Collection<UUID> value) {
		this.blueprintIds = value;
		return this;
	}

	public PlanElasticQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final QueryFactory queryFactory;
	private final AppElasticConfiguration appElasticConfiguration;
	private final ElasticService elasticService;
	private final UserScope userScope;
	private final TenantScope tenantScope;
	private final AuthorizationService authService;
	@Autowired
	public PlanElasticQuery(ElasticsearchTemplate elasticsearchTemplate, ElasticProperties elasticProperties, QueryFactory queryFactory, AppElasticConfiguration appElasticConfiguration, ElasticService elasticService, UserScope userScope, TenantScope tenantScope, AuthorizationService authService) {
		super(elasticsearchTemplate, elasticProperties);
		this.queryFactory = queryFactory;
		this.appElasticConfiguration = appElasticConfiguration;
		this.elasticService = elasticService;
		this.userScope = userScope;
		this.tenantScope = tenantScope;
		this.authService = authService;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.versionStatuses) || this.isEmpty(this.excludedIds)  || this.isEmpty(this.accessTypes)|| this.isEmpty(this.statuses);
	}

	@Override
	protected Class<PlanElasticEntity> entityClass() {
		return PlanElasticEntity.class;
	}

	private Query applyTenant(List<Query> predicates){
		if (this.tenantScope.isSet()){
			Query tenantQuery;
			if (this.tenantScope.isDefaultTenant()){
				tenantQuery = this.fieldNotExists(this.elasticFieldOf(PlanElasticEntity._tenantId))._toQuery();
			}
			else {
				try {
					tenantQuery = this.or(this.fieldNotExists(this.elasticFieldOf(PlanElasticEntity._tenantId))._toQuery(),
							this.equals(this.elasticFieldOf(PlanElasticEntity._tenantId), this.tenantScope.getTenant()))._toQuery();
				} catch (InvalidApplicationException e) {
					throw new RuntimeException(e);
				}
			}
			if (predicates == null) return tenantQuery;
			else return this.and(tenantQuery, this.or(predicates)._toQuery());
		} else {
			if (predicates != null) return this.or(predicates)._toQuery();
		}
		return null;
	}
	@Override
	protected Query applyAuthZ() {
		
		if (this.authorize.contains(AuthorizationFlags.None)) return this.applyTenant(null);
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowsePlan)) return this.applyTenant(null);
		UUID userId = null;
		boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
		if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();

		List<Query> predicates = new ArrayList<>();
		if (usePublic) {
			predicates.add(this.and(
					this.equals(this.elasticFieldOf(PlanElasticEntity._status), PlanStatus.Finalized.getValue()),
					this.equals(this.elasticFieldOf(PlanElasticEntity._accessType), PlanAccessType.Public.getValue())
			));
		}
		if (userId != null) {
			NestedCollaboratorElasticQuery query = this.queryFactory.query(NestedCollaboratorElasticQuery.class).nestedPath(PlanElasticEntity._collaborators);
			query.userIds(userId);
			predicates.add(this.nestedQuery(query).build()._toQuery());
		}
		if (!predicates.isEmpty()) {
			return this.applyTenant(predicates);
		} else {
			return this.equals(this.elasticFieldOf(PlanElasticEntity._id), UUID.randomUUID());
		}
	}
	
	@Override
	protected Query applyFilters() {
		List<Query> predicates = new ArrayList<>();
		
		if (this.like != null && !this.like.isBlank()) {

			if (!this.like.startsWith("*")) this.like = "*" + this.like;
			if (!this.like.endsWith("*")) this.like = this.like + "*";
			ElasticFields elasticFields = this.elasticFieldsOf();
			elasticFields.add("*", null, true);

			predicates.add(this.or(
					this.like(elasticFields, List.of(this.like))._toQuery()
					,
					QueryBuilders.nested().path(PlanElasticEntity._collaborators).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(PlanElasticEntity._references).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(PlanElasticEntity._descriptions + "." + NestedDescriptionElasticEntity._references).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(PlanElasticEntity._descriptions + "." + NestedDescriptionElasticEntity._tags).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(PlanElasticEntity._descriptions).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery()
					
			)._toQuery());
		}
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(PlanElasticEntity._id), this.ids)._toQuery());
		}
		if (this.groupIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(PlanElasticEntity._groupId), this.groupIds)._toQuery());
		}
		if (this.blueprintIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(PlanElasticEntity._blueprintId), this.blueprintIds)._toQuery());
		}
		if (this.versions != null) {
			predicates.add(this.contains(this.elasticFieldOf(PlanElasticEntity._version), this.versions.toArray(new Integer[this.versions.size()]))._toQuery());
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsUUID(this.elasticFieldOf(PlanElasticEntity._id), this.excludedIds)._toQuery())._toQuery());
		}
		if (this.statuses != null) {
			predicates.add(this.contains(this.elasticFieldOf(PlanElasticEntity._status), this.statuses.stream().map(PlanStatus::getValue).toList().toArray(new Short[this.statuses.size()]))._toQuery());
		}
		if (this.versionStatuses != null) {
			predicates.add(this.contains(this.elasticFieldOf(PlanElasticEntity._versionStatus), this.versionStatuses.stream().map(PlanVersionStatus::getValue).toList().toArray(new Short[this.versionStatuses.size()]))._toQuery());
		}
		if (this.accessTypes != null) {
			predicates.add(this.contains(this.elasticFieldOf(PlanElasticEntity._accessType), this.accessTypes.stream().map(PlanAccessType::getValue).toList().toArray(new Short[this.accessTypes.size()]))._toQuery());
		}
		if (this.planUserSubQuery != null) {
			predicates.add(this.nestedQuery( this.planUserSubQuery.nestedPath(PlanElasticEntity._collaborators)).build()._toQuery());
		}
		if (this.descriptionSubQuery != null) {
			predicates.add(this.nestedQuery( this.descriptionSubQuery.nestedPath(PlanElasticEntity._descriptions)).build()._toQuery());
		}
		if (this.planDescriptionTemplateSubQuery != null) {
			predicates.add(this.nestedQuery( this.planDescriptionTemplateSubQuery.nestedPath(PlanElasticEntity._planDescriptionTemplates)).build()._toQuery());
		}
		if (this.referenceSubQuery != null) {
			predicates.add(this.nestedQuery( this.referenceSubQuery.nestedPath(PlanElasticEntity._references)).build()._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public PlanElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		PlanElasticEntity mocDoc = new PlanElasticEntity();
		if (columns.contains(PlanElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._id), UUID.class));
		if (columns.contains(PlanElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._label), String.class));
		if (columns.contains(PlanElasticEntity._description)) mocDoc.setDescription(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._description), String.class));
		if (columns.contains(PlanElasticEntity._status)) mocDoc.setStatus(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._status), PlanStatus.class));
		if (columns.contains(PlanElasticEntity._versionStatus)) mocDoc.setVersionStatus(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._versionStatus), PlanVersionStatus.class));
		if (columns.contains(PlanElasticEntity._version)) mocDoc.setVersion(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._version), Short.class));
		if (columns.contains(PlanElasticEntity._groupId)) mocDoc.setGroupId(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._groupId), UUID.class));
		if (columns.contains(PlanElasticEntity._accessType)) mocDoc.setAccessType(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._accessType), PlanAccessType.class));
		if (columns.contains(PlanElasticEntity._finalizedAt)) mocDoc.setFinalizedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._finalizedAt), Date.class));
		if (columns.contains(PlanElasticEntity._createdAt)) mocDoc.setCreatedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._createdAt), Date.class));
		if (columns.contains(PlanElasticEntity._updatedAt)) mocDoc.setUpdatedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(PlanElasticEntity._updatedAt), Date.class));
		mocDoc.setCollaborators(this.convertNested(rawData, columns, this.queryFactory.query(NestedCollaboratorElasticQuery.class), PlanElasticEntity._collaborators, null));
		mocDoc.setReferences(this.convertNested(rawData, columns, this.queryFactory.query(NestedReferenceElasticQuery.class), PlanElasticEntity._references, null));
		mocDoc.setDescriptions(this.convertNested(rawData, columns, this.queryFactory.query(NestedDescriptionElasticQuery.class), PlanElasticEntity._descriptions, null));
		mocDoc.setPlanDescriptionTemplates(this.convertNested(rawData, columns, this.queryFactory.query(NestedPlanDescriptionTemplateElasticQuery.class), PlanElasticEntity._planDescriptionTemplates, null));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(PlanElasticEntity._id)) return this.elasticFieldOf(PlanElasticEntity._id);
		else if (item.match(PlanElasticEntity._label)) return item instanceof OrderingFieldResolver ?  this.elasticFieldOf(PlanElasticEntity._label).subfield(ElasticConstants.SubFields.keyword) : this.elasticFieldOf(PlanElasticEntity._label);
		else if (item.match(PlanElasticEntity._description)) return this.elasticFieldOf(PlanElasticEntity._description);
		else if (item.match(PlanElasticEntity._status)) return this.elasticFieldOf(PlanElasticEntity._status);
		else if (item.match(PlanElasticEntity._version)) return this.elasticFieldOf(PlanElasticEntity._version);
		else if (item.match(PlanElasticEntity._versionStatus)) return this.elasticFieldOf(PlanElasticEntity._versionStatus);
		else if (item.match(PlanElasticEntity._groupId)) return this.elasticFieldOf(PlanElasticEntity._groupId);
		else if (item.match(PlanElasticEntity._finalizedAt)) return this.elasticFieldOf(PlanElasticEntity._finalizedAt);
		else if (item.match(PlanElasticEntity._updatedAt)) return this.elasticFieldOf(PlanElasticEntity._updatedAt);
		else if (item.match(PlanElasticEntity._createdAt)) return this.elasticFieldOf(PlanElasticEntity._createdAt);
		else if (item.match(PlanElasticEntity._accessType)) return this.elasticFieldOf(PlanElasticEntity._accessType);
		else if (item.prefix(PlanElasticEntity._collaborators)) return this.queryFactory.query(NestedCollaboratorElasticQuery.class).nestedPath(PlanElasticEntity._collaborators).fieldNameOf(this.extractPrefixed(item, PlanElasticEntity._collaborators));
		else if (item.prefix(PlanElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(PlanElasticEntity._references).fieldNameOf(this.extractPrefixed(item, PlanElasticEntity._references));
		else if (item.prefix(PlanElasticEntity._descriptions)) return this.queryFactory.query(NestedDescriptionElasticQuery.class).nestedPath(PlanElasticEntity._descriptions).fieldNameOf(this.extractPrefixed(item, PlanElasticEntity._descriptions));
		else if (item.prefix(PlanElasticEntity._planDescriptionTemplates)) return this.queryFactory.query(NestedPlanDescriptionTemplateElasticQuery.class).nestedPath(PlanElasticEntity._planDescriptionTemplates).fieldNameOf(this.extractPrefixed(item, PlanElasticEntity._planDescriptionTemplates));
		else return null;
	}

	@Override
	protected String[] getIndex() {
		List<String> indexNames = new ArrayList<>();
		indexNames.add(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName());
		try {
			this.elasticService.ensureDescriptionIndex();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return indexNames.toArray(new String[indexNames.size()]);
	}

	@Override
	protected UUID toKey(String key) {
		return UUID.fromString(key);
	}

	@Override
	protected ElasticField getKeyField() {
		return this.elasticFieldOf(PlanElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		if (item.prefix(PlanElasticEntity._collaborators)) return this.queryFactory.query(NestedCollaboratorElasticQuery.class).nestedPath(PlanElasticEntity._collaborators);
		else if (item.prefix(PlanElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(PlanElasticEntity._references);
		else if (item.prefix(PlanElasticEntity._descriptions)) return this.queryFactory.query(NestedDescriptionElasticQuery.class).nestedPath(PlanElasticEntity._descriptions);
		else if (item.prefix(PlanElasticEntity._planDescriptionTemplates)) return this.queryFactory.query(NestedPlanDescriptionTemplateElasticQuery.class).nestedPath(PlanElasticEntity._planDescriptionTemplates);
		else return null;
	}
}
