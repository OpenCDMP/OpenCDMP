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
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.opencdmp.elastic.data.PlanElasticEntity;
import org.opencdmp.elastic.data.nested.NestedPlanElasticEntity;
import org.opencdmp.service.elastic.AppElasticConfiguration;
import org.opencdmp.service.elastic.ElasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
//Like in C# make it Transient
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DescriptionElasticQuery extends ElasticQuery<DescriptionElasticEntity, UUID> {

	private Collection<UUID> ids;
	private String like;
	private InnerObjectPlanElasticQuery planSubQuery;
	private Instant createdAfter;
	private Instant createdBefore;
	private Instant finalizedAfter;
	private Instant finalizedBefore;
	private Collection<UUID> excludedIds;
	private Collection<UUID> tenantIds;
	private Collection<UUID> statusIds;
	private NestedDescriptionTemplateElasticQuery descriptionTemplateSubQuery;
	private NestedReferenceElasticQuery referenceSubQuery;
	private NestedTagElasticQuery tagSubQuery;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public DescriptionElasticQuery like(String value) {
		this.like = value;
		return this;
	}

	public DescriptionElasticQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public DescriptionElasticQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public DescriptionElasticQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public DescriptionElasticQuery planSubQuery(InnerObjectPlanElasticQuery subQuery) {
		this.planSubQuery = subQuery;
		return this;
	}


	public DescriptionElasticQuery createdAfter(Instant value) {
		this.createdAfter = value;
		return this;
	}

	public DescriptionElasticQuery createdBefore(Instant value) {
		this.createdBefore = value;
		return this;
	}

	public DescriptionElasticQuery finalizedAfter(Instant value) {
		this.finalizedAfter = value;
		return this;
	}

	public DescriptionElasticQuery finalizedBefore(Instant value) {
		this.finalizedBefore = value;
		return this;
	}


	public DescriptionElasticQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public DescriptionElasticQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public DescriptionElasticQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public DescriptionElasticQuery tenantIds(Collection<UUID> values) {
		this.tenantIds = values;
		return this;
	}

	public DescriptionElasticQuery tenantIds(UUID value) {
		this.tenantIds = List.of(value);
		return this;
	}

	public DescriptionElasticQuery tenantIds(UUID... value) {
		this.tenantIds = Arrays.asList(value);
		return this;
	}

	public DescriptionElasticQuery statusIds(UUID value) {
		this.statusIds = List.of(value);
		return this;
	}

	public DescriptionElasticQuery statusIds(UUID... value) {
		this.statusIds = Arrays.asList(value);
		return this;
	}

	public DescriptionElasticQuery statusIds(Collection<UUID> values) {
		this.statusIds = values;
		return this;
	}

	public DescriptionElasticQuery descriptionTemplateSubQuery(NestedDescriptionTemplateElasticQuery subQuery) {
		this.descriptionTemplateSubQuery = subQuery;
		return this;
	}

	public DescriptionElasticQuery referenceSubQuery(NestedReferenceElasticQuery subQuery) {
		this.referenceSubQuery = subQuery;
		return this;
	}

	public DescriptionElasticQuery tagSubQuery(NestedTagElasticQuery subQuery) {
		this.tagSubQuery = subQuery;
		return this;
	}

	public DescriptionElasticQuery authorize(EnumSet<AuthorizationFlags> values) {
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
	public DescriptionElasticQuery(ElasticsearchTemplate elasticsearchTemplate, ElasticProperties elasticProperties, QueryFactory queryFactory, AppElasticConfiguration appElasticConfiguration, ElasticService elasticService, UserScope userScope, TenantScope tenantScope, AuthorizationService authService) {
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
		return  this.isEmpty(this.ids) ||
				this.isEmpty(this.excludedIds) ||
				this.isEmpty(this.statusIds);
	}

	@Override
	protected Class<DescriptionElasticEntity> entityClass() {
		return DescriptionElasticEntity.class;
	}

	private Query applyTenant(List<Query> predicates){
		if (this.tenantScope.isSet()){
			Query tenantQuery;
			if (this.tenantScope.isDefaultTenant()){
				tenantQuery = this.fieldNotExists(this.elasticFieldOf(DescriptionElasticEntity._tenantId))._toQuery();
			}
			else {
				try {
					tenantQuery = this.or(this.fieldNotExists(this.elasticFieldOf(DescriptionElasticEntity._tenantId))._toQuery(),
							this.equals(this.elasticFieldOf(DescriptionElasticEntity._tenantId), this.tenantScope.getTenant()))._toQuery();
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
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseDescription)) return this.applyTenant(null);
		UUID userId = null;
		boolean usePublic = this.authorize.contains(AuthorizationFlags.Public);
		if (this.authorize.contains(AuthorizationFlags.PlanAssociated)) userId = this.userScope.getUserIdSafe();

		List<Query> predicates = new ArrayList<>();
		if (usePublic ) {
			predicates.add(this.and(
					this.equals(new ElasticField(DescriptionElasticEntity._plan + "." + PlanElasticEntity._accessType, this.entityClass()).disableInfer(true), PlanAccessType.Public.getValue())
			));
		}
		if (userId != null) {
			NestedCollaboratorElasticQuery query = this.queryFactory.query(NestedCollaboratorElasticQuery.class).nestedPath(DescriptionElasticEntity._plan + "."  + NestedPlanElasticEntity._collaborators);
			query.userIds(userId);
			predicates.add(this.nestedQuery(query).build()._toQuery());
		}

		if (!predicates.isEmpty()) {
			return this.applyTenant(predicates);
		} else {
			return this.equals(this.elasticFieldOf(DescriptionElasticEntity._id), UUID.randomUUID());
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
					this.like(elasticFields, List.of(this.like))._toQuery(),
					QueryBuilders.nested().path(DescriptionElasticEntity._tags).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(DescriptionElasticEntity._references).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(DescriptionElasticEntity._plan + "." + NestedPlanElasticEntity._references).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(DescriptionElasticEntity._plan + "." + NestedPlanElasticEntity._collaborators).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery(),
					QueryBuilders.nested().path(DescriptionElasticEntity._plan + "." + NestedPlanElasticEntity._dois).query(
							this.like(elasticFields, List.of(this.like))._toQuery()
					).build()._toQuery()
			)._toQuery());
		}
		if (this.ids != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(DescriptionElasticEntity._id), this.ids)._toQuery());
		}
		if (this.excludedIds != null) {
			predicates.add(this.not(this.containsUUID(this.elasticFieldOf(DescriptionElasticEntity._id), this.excludedIds)._toQuery())._toQuery());
		}
		if (this.tenantIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(DescriptionElasticEntity._tenantId), this.tenantIds)._toQuery());
		}
		if (this.statusIds != null) {
			predicates.add(this.containsUUID(this.elasticFieldOf(DescriptionElasticEntity._statusId), this.statusIds)._toQuery());
		}
		if (this.finalizedAfter != null) {
			predicates.add(this.dateGreaterThanQuery(this.elasticFieldOf(DescriptionElasticEntity._finalizedAt), this.finalizedAfter)._toQuery());
		}
		if (this.finalizedBefore != null) {
			predicates.add(this.dateLessThanQuery(this.elasticFieldOf(DescriptionElasticEntity._finalizedAt), this.finalizedBefore)._toQuery());
		}
		if (this.createdAfter != null) {
			predicates.add(this.dateGreaterThanQuery(this.elasticFieldOf(DescriptionElasticEntity._createdAt), this.createdAfter)._toQuery());
		}
		if (this.createdBefore != null) {
			predicates.add(this.dateLessThanQuery(this.elasticFieldOf(DescriptionElasticEntity._createdAt), this.createdBefore)._toQuery());
		}
		if (this.planSubQuery != null) {
			predicates.add(this.planSubQuery.innerPath(DescriptionElasticEntity._plan).applyFilters());
		}
		if (this.descriptionTemplateSubQuery != null) {
			predicates.add(this.nestedQuery( this.descriptionTemplateSubQuery.nestedPath(DescriptionElasticEntity._descriptionTemplate)).build()._toQuery());
		}
		if (this.referenceSubQuery != null) {
			predicates.add(this.nestedQuery( this.referenceSubQuery.nestedPath(DescriptionElasticEntity._references)).build()._toQuery());
		}
		if (this.tagSubQuery != null) {
			predicates.add(this.nestedQuery( this.tagSubQuery.nestedPath(DescriptionElasticEntity._tags)).build()._toQuery());
		}
		if (!predicates.isEmpty()) {
			return this.and(predicates);
		} else {
			return null;
		}
	}

	@Override
	public DescriptionElasticEntity convert(Map<String, Object> rawData, Set<String> columns) {
		DescriptionElasticEntity mocDoc = new DescriptionElasticEntity();
		if (columns.contains(DescriptionElasticEntity._id)) mocDoc.setId(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._id), UUID.class));
		if (columns.contains(DescriptionElasticEntity._label)) mocDoc.setLabel(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._label), String.class));
		if (columns.contains(DescriptionElasticEntity._description)) mocDoc.setDescription(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._description), String.class));
		if (columns.contains(DescriptionElasticEntity._statusId)) mocDoc.setStatusId(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._statusId), UUID.class));
		if (columns.contains(DescriptionElasticEntity._finalizedAt)) mocDoc.setFinalizedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._finalizedAt), Date.class));
		if (columns.contains(DescriptionElasticEntity._createdAt)) mocDoc.setCreatedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._createdAt), Date.class));
		if (columns.contains(DescriptionElasticEntity._updatedAt)) mocDoc.setUpdatedAt(FieldBasedMapper.shallowSafeConversion(rawData.get(DescriptionElasticEntity._updatedAt), Date.class));
		mocDoc.setTags(this.convertNested(rawData, columns, this.queryFactory.query(NestedTagElasticQuery.class), DescriptionElasticEntity._tags, null));
		mocDoc.setReferences(this.convertNested(rawData, columns, this.queryFactory.query(NestedReferenceElasticQuery.class), DescriptionElasticEntity._references, null));
		mocDoc.setDescriptionTemplate(this.convertInnerObject(rawData, columns, this.queryFactory.query(InnerObjectDescriptionTemplateElasticQuery.class), DescriptionElasticEntity._descriptionTemplate, null));
		mocDoc.setPlan(this.convertInnerObject(rawData, columns, this.queryFactory.query(InnerObjectPlanElasticQuery.class), DescriptionElasticEntity._plan, null));
		return mocDoc;
	}

	@Override
	protected ElasticField fieldNameOf(FieldResolver item) {
		if (item.match(DescriptionElasticEntity._id)) return this.elasticFieldOf(DescriptionElasticEntity._id);
		else if (item.match(DescriptionElasticEntity._label)) return item instanceof OrderingFieldResolver ?  this.elasticFieldOf(DescriptionElasticEntity._label).subfield(ElasticConstants.SubFields.keyword) : this.elasticFieldOf(DescriptionElasticEntity._label);
		else if (item.match(DescriptionElasticEntity._description)) return this.elasticFieldOf(DescriptionElasticEntity._description);
		else if (item.match(DescriptionElasticEntity._statusId)) return this.elasticFieldOf(DescriptionElasticEntity._statusId);
		else if (item.match(DescriptionElasticEntity._finalizedAt)) return this.elasticFieldOf(DescriptionElasticEntity._finalizedAt);
		else if (item.match(DescriptionElasticEntity._createdAt)) return this.elasticFieldOf(DescriptionElasticEntity._createdAt);
		else if (item.match(DescriptionElasticEntity._updatedAt)) return this.elasticFieldOf(DescriptionElasticEntity._updatedAt);
		else if (item.prefix(DescriptionElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(DescriptionElasticEntity._references).fieldNameOf(this.extractPrefixed(item, DescriptionElasticEntity._references));
		else if (item.prefix(DescriptionElasticEntity._tags)) return this.queryFactory.query(NestedTagElasticQuery.class).nestedPath(DescriptionElasticEntity._tags).fieldNameOf(this.extractPrefixed(item, DescriptionElasticEntity._tags));
		else if (item.prefix(DescriptionElasticEntity._descriptionTemplate)) return this.queryFactory.query(InnerObjectDescriptionTemplateElasticQuery.class).innerPath(DescriptionElasticEntity._descriptionTemplate).fieldNameOf(this.extractPrefixed(item, DescriptionElasticEntity._description));
		else if (item.prefix(DescriptionElasticEntity._plan)) return this.queryFactory.query(InnerObjectPlanElasticQuery.class).innerPath(DescriptionElasticEntity._plan).fieldNameOf(this.extractPrefixed(item, DescriptionElasticEntity._plan));
		else return null;
	}

	@Override
	protected String[] getIndex() {
		List<String> indexNames = new ArrayList<>();
		indexNames.add(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName());
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
		return this.elasticFieldOf(DescriptionElasticEntity._id);
	}

	@Override
	protected ElasticNestedQuery<?, ?, ?> nestedQueryOf(FieldResolver item) {
		if (item.prefix(DescriptionElasticEntity._references)) return this.queryFactory.query(NestedReferenceElasticQuery.class).nestedPath(DescriptionElasticEntity._references);
		else if (item.prefix(DescriptionElasticEntity._tags)) return this.queryFactory.query(NestedTagElasticQuery.class).nestedPath(DescriptionElasticEntity._tags);
		else return null;
	}
}
