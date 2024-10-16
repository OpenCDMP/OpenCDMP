package org.opencdmp.service.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.*;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.Paging;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.elastic.ElasticConstants;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.elastic.data.DescriptionElasticEntity;
import org.opencdmp.elastic.data.PlanElasticEntity;
import org.opencdmp.elastic.data.nested.*;
import org.opencdmp.elastic.elasticbuilder.DescriptionElasticBuilder;
import org.opencdmp.elastic.elasticbuilder.PlanElasticBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.service.planblueprint.PlanBlueprintServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticServiceImpl implements ElasticService {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintServiceImpl.class));
	public final AppElasticConfiguration appElasticConfiguration;
	private final ElasticsearchClient restHighLevelClient;
	private final ElasticsearchTemplate elasticsearchTemplate;
	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final TenantEntityManager entityManager;
	private final MessageSource messageSource;
	private final AuthorizationService authorizationService;

	public ElasticServiceImpl(AppElasticConfiguration appElasticConfiguration, ElasticsearchClient restHighLevelClient, ElasticsearchTemplate elasticsearchTemplate, QueryFactory queryFactory, BuilderFactory builderFactory, TenantEntityManager entityManager, MessageSource messageSource, AuthorizationService authorizationService) {
		this.appElasticConfiguration = appElasticConfiguration;
		this.restHighLevelClient = restHighLevelClient;
		this.elasticsearchTemplate = elasticsearchTemplate;
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.entityManager = entityManager;
		this.messageSource = messageSource;
		this.authorizationService = authorizationService;
	}

	@Override
	public boolean enabled() {
		return this.appElasticConfiguration.getAppElasticProperties().isEnabled();
	}

	@Override
	public boolean existsPlanIndex() throws IOException {
		if (!this.enabled()) return false;
		return this.restHighLevelClient.indices().exists(new ExistsRequest.Builder().index(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()).includeDefaults(true).build()).value();
	}


	@Override
	public boolean existsDescriptionIndex() throws IOException {
		if (!this.enabled()) return false;
		return this.restHighLevelClient.indices().exists(new ExistsRequest.Builder().index(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()).includeDefaults(true).build()).value();
	}
	
	//region ensure index

	@Override
	public void ensurePlanIndex() throws IOException {
		if (!this.enabled()) return ;
		boolean exists = this.existsPlanIndex();
		if (exists) return ;

		this.ensureIndex(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName(), this.createPlanTemplatePropertyMap());
	}

	@Override
	public void ensureDescriptionIndex() throws IOException {
		if (!this.enabled()) return ;
		boolean exists = this.existsDescriptionIndex();
		if (exists) return ;
		this.ensureIndex(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName(), this.createDescriptionTemplatePropertyMap());
	}

	@Override
	public void ensureIndexes() throws IOException {
		if (!this.enabled()) return ;

		this.ensurePlanIndex();
		this.ensureDescriptionIndex();
	}

	private void ensureIndex(String indexName, Map<String, Property> propertyMap) throws IOException {
		TypeMapping.Builder typeMapping = new TypeMapping.Builder();
		typeMapping.properties(propertyMap);

		IndexSettings.Builder indexSettings = new IndexSettings.Builder();
		IndexSettingsAnalysis.Builder indexSettingsAnalysis = new IndexSettingsAnalysis.Builder();
		indexSettingsAnalysis.filter("english_stemmer", ((tf) -> tf.definition(tfdb -> tfdb.stemmer(stemmerBuilder -> stemmerBuilder.language("english")))))
				.filter("english_stop", tf -> tf.definition(tfdb -> tfdb.stop(stopTokenBuilder -> stopTokenBuilder)));

		if (this.appElasticConfiguration.getAppElasticProperties().isEnableIcuAnalysisPlugin()){
			indexSettingsAnalysis.analyzer("icu_analyzer_text", ab -> ab.custom(x-> x.filter("icu_folding", "english_stop", "english_stemmer").tokenizer("icu_tokenizer")));
		} else {
			indexSettingsAnalysis.analyzer("icu_analyzer_text",  ab -> ab.custom(x-> x.filter("icu_folding", "english_stop", "english_stemmer").tokenizer("standard")));
		}
		indexSettings.analysis(indexSettingsAnalysis.build());
		this.restHighLevelClient.indices().create(new CreateIndexRequest.Builder().index(indexName).mappings(typeMapping.build()).settings(indexSettings.build()).build());

	}

	private Map<String, Property> createDescriptionTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(DescriptionElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(DescriptionElasticEntity._tenantId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(DescriptionElasticEntity._label, this.createElastic(FieldType.Text, true));
		propertyMap.put(DescriptionElasticEntity._description, this.createElastic(FieldType.Text, true));
		propertyMap.put(DescriptionElasticEntity._status, this.createElastic(FieldType.Short, false));
		propertyMap.put(DescriptionElasticEntity._finalizedAt, this.createElastic(FieldType.Date, false));
		propertyMap.put(DescriptionElasticEntity._createdAt, this.createElastic(FieldType.Date, false));
		propertyMap.put(DescriptionElasticEntity._updatedAt, this.createElastic(FieldType.Date, false));

		propertyMap.put(DescriptionElasticEntity._tags, new Property.Builder().nested(x -> x.properties(this.createNestedTagsTemplatePropertyMap())).build());
		propertyMap.put(DescriptionElasticEntity._references, new Property.Builder().nested(x -> x.properties(this.createNestedReferencesTemplatePropertyMap())).build());
		propertyMap.put(DescriptionElasticEntity._descriptionTemplate, new Property.Builder().object(x -> x.properties(this.createNestedDescriptionTemplateTemplatePropertyMap())).build());
		propertyMap.put(DescriptionElasticEntity._plan, new Property.Builder().object(x -> x.properties(this.createNetsedPlanTemplatePropertyMap())).build());
		return propertyMap;
	}
	
	private Map<String, Property> createPlanTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(PlanElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(PlanElasticEntity._tenantId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(PlanElasticEntity._label, this.createElastic(FieldType.Text, true));
		propertyMap.put(PlanElasticEntity._description, this.createElastic(FieldType.Text, false));
		propertyMap.put(PlanElasticEntity._status, this.createElastic(FieldType.Short, false));
		propertyMap.put(PlanElasticEntity._version, this.createElastic(FieldType.Short, false));
		propertyMap.put(PlanElasticEntity._language, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(PlanElasticEntity._blueprintId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(PlanElasticEntity._accessType, this.createElastic(FieldType.Short, false));
		propertyMap.put(PlanElasticEntity._groupId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(PlanElasticEntity._finalizedAt, this.createElastic(FieldType.Date, false));
		propertyMap.put(PlanElasticEntity._updatedAt, this.createElastic(FieldType.Date, false));
		propertyMap.put(PlanElasticEntity._createdAt, this.createElastic(FieldType.Date, false));
		propertyMap.put(PlanElasticEntity._versionStatus, this.createElastic(FieldType.Short, false));

		propertyMap.put(PlanElasticEntity._descriptions, new Property.Builder().nested(x -> x.properties(this.createNestedDescriptionTemplatePropertyMap())).build());
		propertyMap.put(PlanElasticEntity._planDescriptionTemplates, new Property.Builder().nested(x -> x.properties(this.createNestedPlanDescriptionTemplateElasticEntityPropertyMap())).build());
		propertyMap.put(PlanElasticEntity._references, new Property.Builder().nested(x -> x.properties(this.createNestedReferencesTemplatePropertyMap())).build());
		propertyMap.put(PlanElasticEntity._collaborators, new Property.Builder().nested(x -> x.properties(this.createNestedCollaboratorTemplatePropertyMap())).build());
		propertyMap.put(PlanElasticEntity._dois, new Property.Builder().nested(x -> x.properties(this.createNestedDoisTemplatePropertyMap())).build());
		return propertyMap;
	}

	private Map<String, Property> createNestedPlanDescriptionTemplateElasticEntityPropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedPlanDescriptionTemplateElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanDescriptionTemplateElasticEntity._descriptionTemplateGroupId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanDescriptionTemplateElasticEntity._sectionId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanDescriptionTemplateElasticEntity._planId, this.createElastic(FieldType.Keyword, false));

		return propertyMap;
	}
	
	private Map<String, Property> createNestedDescriptionTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedDescriptionElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedDescriptionElasticEntity._label, this.createElastic(FieldType.Text, true));
		propertyMap.put(NestedDescriptionElasticEntity._planId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedDescriptionElasticEntity._description, this.createElastic(FieldType.Text, true));
		propertyMap.put(NestedDescriptionElasticEntity._status, this.createElastic(FieldType.Short, false));
		propertyMap.put(NestedDescriptionElasticEntity._finalizedAt, this.createElastic(FieldType.Date, false));

		propertyMap.put(NestedDescriptionElasticEntity._tags, new Property.Builder().nested(x -> x.properties(this.createNestedTagsTemplatePropertyMap())).build());
		propertyMap.put(NestedDescriptionElasticEntity._references, new Property.Builder().nested(x -> x.properties(this.createNestedReferencesTemplatePropertyMap())).build());
		propertyMap.put(NestedDescriptionElasticEntity._descriptionTemplate, new Property.Builder().object(x -> x.properties(this.createNestedDescriptionTemplateTemplatePropertyMap())).build());

		return propertyMap;
	}

	private Map<String, Property> createNestedTagsTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedTagElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedTagElasticEntity._label, this.createElastic(FieldType.Text, true));

		return propertyMap;
	}

	private Map<String, Property> createNestedReferencesTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedReferenceElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedReferenceElasticEntity._label, this.createElastic(FieldType.Text, true));

		return propertyMap;
	}

	private Map<String, Property> createNestedDescriptionTemplateTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedDescriptionTemplateElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedDescriptionTemplateElasticEntity._label, this.createElastic(FieldType.Text, true));
		propertyMap.put(NestedDescriptionTemplateElasticEntity._versionStatus, this.createElastic(FieldType.Short, true));
		propertyMap.put(NestedDescriptionTemplateElasticEntity._groupId, this.createElastic(FieldType.Keyword, true));

		return propertyMap;
	}
	
	private Map<String, Property> createNetsedPlanTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedPlanElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanElasticEntity._label, this.createElastic(FieldType.Text, true));
		propertyMap.put(NestedPlanElasticEntity._description, this.createElastic(FieldType.Text, false));
		propertyMap.put(NestedPlanElasticEntity._status, this.createElastic(FieldType.Short, false));
		propertyMap.put(NestedPlanElasticEntity._version, this.createElastic(FieldType.Short, false));
		propertyMap.put(NestedPlanElasticEntity._versionStatus, this.createElastic(FieldType.Short, false));
		propertyMap.put(NestedPlanElasticEntity._language, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanElasticEntity._blueprintId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanElasticEntity._accessType, this.createElastic(FieldType.Short, false));
		propertyMap.put(NestedPlanElasticEntity._groupId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedPlanElasticEntity._finalizedAt, this.createElastic(FieldType.Date, false));

		propertyMap.put(NestedPlanElasticEntity._references, new Property.Builder().nested(x -> x.properties(this.createNestedReferencesTemplatePropertyMap())).build());
		propertyMap.put(NestedPlanElasticEntity._collaborators, new Property.Builder().nested(x -> x.properties(this.createNestedCollaboratorTemplatePropertyMap())).build());
		propertyMap.put(NestedPlanElasticEntity._dois, new Property.Builder().nested(x -> x.properties(this.createNestedDoisTemplatePropertyMap())).build());
		return propertyMap;
	}

	private Map<String, Property> createNestedCollaboratorTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedCollaboratorElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedCollaboratorElasticEntity._userId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedCollaboratorElasticEntity._name, this.createElastic(FieldType.Text, true));
		propertyMap.put(NestedCollaboratorElasticEntity._role, this.createElastic(FieldType.Short, false));

		return propertyMap;
	}

	private Map<String, Property> createNestedDoisTemplatePropertyMap(){
		Map<String, Property> propertyMap = new HashMap<>();
		propertyMap.put(NestedDoiElasticEntity._id, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedDoiElasticEntity._repositoryId, this.createElastic(FieldType.Keyword, false));
		propertyMap.put(NestedDoiElasticEntity._doi, this.createElastic(FieldType.Keyword, false));
		return propertyMap;
	}
	
	private Property createElastic(FieldType fieldType, boolean hasKeywordSubField){
		switch (fieldType){
			case Keyword -> {
				return new Property.Builder().keyword(x -> x).build();
			}
			case Text -> {
				return hasKeywordSubField ? new Property.Builder().text(x -> x.analyzer("icu_analyzer_text").fields(ElasticConstants.SubFields.keyword, y -> y.keyword(z-> z))).build() : new Property.Builder().text(x -> x).build();
			}
			case Date -> {
				return new Property.Builder().date(x -> x).build();
			}
			case Short -> {
				return new Property.Builder().short_(x -> x).build();
			}
			case Boolean -> {
				return new Property.Builder().boolean_(x -> x).build();
			}
			default -> throw new RuntimeException();
		}
	}
	
	//endregion
	
	//region persist delete

	@Override
	public void persistPlan(PlanEntity plan) throws IOException {
		if (!this.enabled()) return;
		this.ensureIndexes();

		PlanElasticEntity planElasticEntity = this.builderFactory.builder(PlanElasticBuilder.class).build(plan);
		this.elasticsearchTemplate.save(planElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()));
		List<DescriptionElasticEntity> descriptions = this.builderFactory.builder(DescriptionElasticBuilder.class).build(this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).planSubQuery(this.queryFactory.query(PlanQuery.class).ids(plan.getId())).collect());
		this.elasticsearchTemplate.save(descriptions, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
	}

	@Override
	public void deletePlan(PlanEntity plan) throws IOException {
		if (!this.enabled()) return;
		this.ensureIndexes();
		PlanElasticEntity planElasticEntity = this.elasticsearchTemplate.get(plan.getId().toString(), PlanElasticEntity.class, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()));
		if (planElasticEntity == null) return;
		this.elasticsearchTemplate.delete(planElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()));
		List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class).planSubQuery(this.queryFactory.query(PlanQuery.class).disableTracking().ids(plan.getId())).collectAs(new BaseFieldSet().ensure(Description._id));
		for (DescriptionEntity description: descriptions) {
			DescriptionElasticEntity descriptionElasticEntity = this.elasticsearchTemplate.get(description.getId().toString(), DescriptionElasticEntity.class, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
			if (descriptionElasticEntity == null) continue;
			this.elasticsearchTemplate.delete(descriptionElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
		}
	}

	@Override
	public void persistDescription(DescriptionEntity description) throws IOException, InvalidApplicationException {
		if (!this.enabled()) return;
		this.ensureIndexes();

		DescriptionElasticEntity descriptionElasticEntity = this.builderFactory.builder(DescriptionElasticBuilder.class).build(description);
		this.elasticsearchTemplate.save(descriptionElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
		PlanEntity planEntity = this.entityManager.find(PlanEntity.class, description.getPlanId(), true);
		if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{description.getPlanId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		if (planEntity.getIsActive().equals(IsActive.Active)) {
			PlanElasticEntity planElasticEntity = this.builderFactory.builder(PlanElasticBuilder.class).build(planEntity);
			this.elasticsearchTemplate.save(planElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()));
		}
	}	
	
	@Override
	public void deleteDescription(DescriptionEntity description) throws IOException, InvalidApplicationException {
		if (!this.enabled()) return;
		this.ensureIndexes();

		DescriptionElasticEntity descriptionElasticEntity = this.elasticsearchTemplate.get(description.getId().toString(), DescriptionElasticEntity.class, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
		if (descriptionElasticEntity == null) return;
		this.elasticsearchTemplate.delete(descriptionElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
		
		PlanEntity planEntity = this.entityManager.find(PlanEntity.class, description.getPlanId(), true);
		if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{description.getPlanId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		if (planEntity.getIsActive().equals(IsActive.Active)) {
			PlanElasticEntity planElasticEntity = this.builderFactory.builder(PlanElasticBuilder.class).build(planEntity);
			this.elasticsearchTemplate.save(planElasticEntity, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()));
		}
	}
	
	//endregion
	
	//region manage
	
	@Override
	public void deletePlanIndex() throws IOException {
		logger.debug(new MapLogEntry("delete plan index"));
		this.authorizationService.authorizeForce(Permission.ManageElastic);
		
		if (!this.enabled()) return;
		boolean exists = this.existsPlanIndex();
		if (!exists) return ;
		
		this.restHighLevelClient.indices().delete(new DeleteIndexRequest.Builder().index(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()).build());
	}

	@Override
	public void deleteDescriptionIndex() throws IOException {
		logger.debug(new MapLogEntry("delete description index"));
		this.authorizationService.authorizeForce(Permission.ManageElastic);
		
		if (!this.enabled()) return;
		boolean exists = this.existsDescriptionIndex();
		if (!exists) return ;
		this.restHighLevelClient.indices().delete(new DeleteIndexRequest.Builder().index(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()).build());
	}

	@Override
	public void resetPlanIndex() throws IOException, InvalidApplicationException {
		logger.debug(new MapLogEntry("reset plan index"));
		this.authorizationService.authorizeForce(Permission.ManageElastic);
		
		if (!this.enabled()) return;
		this.deletePlanIndex();
		this.ensurePlanIndex();

		try {
			this.entityManager.disableTenantFilters();
			int page = 0;
			int pageSize = this.appElasticConfiguration.getAppElasticProperties().getResetBatchSize();
			List<PlanEntity> items;
			do {
				PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking();
				query.setOrder(new Ordering().addAscending(Plan._createdAt));
				query.setPage(new Paging(page * pageSize, pageSize));
	
				items = query.collect();
				if (items != null && !items.isEmpty()) {
					List<PlanElasticEntity> elasticEntities = this.builderFactory.builder(PlanElasticBuilder.class).build(items);
					this.elasticsearchTemplate.save(elasticEntities, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getPlanIndexName()));
					page++;
				}
			} while (items != null && !items.isEmpty());
		}finally {
			this.entityManager.reloadTenantFilters();
		}
	}

	@Override
	public void resetDescriptionIndex() throws IOException, InvalidApplicationException {
		logger.debug(new MapLogEntry("reset description index"));
		this.authorizationService.authorizeForce(Permission.ManageElastic);

		if (!this.enabled()) return;
		this.deleteDescriptionIndex();
		this.ensureDescriptionIndex();
		
		try {
			this.entityManager.disableTenantFilters();

			int page = 0;
			int pageSize = this.appElasticConfiguration.getAppElasticProperties().getResetBatchSize();
			List<DescriptionEntity> items;
			do {
				DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking();
				query.setOrder(new Ordering().addAscending(Description._createdAt));
				query.setPage(new Paging(page * pageSize, pageSize));

				items = query.collect();
				if (items != null && !items.isEmpty()) {
					List<DescriptionElasticEntity> elasticEntities = this.builderFactory.builder(DescriptionElasticBuilder.class).build(items);
					this.elasticsearchTemplate.save(elasticEntities, IndexCoordinates.of(this.appElasticConfiguration.getAppElasticProperties().getDescriptionIndexName()));
					page++;
				}
			} while (items != null && !items.isEmpty());
		}finally {
			this.entityManager.reloadTenantFilters();
		}
	}
	
	//endregion
}
