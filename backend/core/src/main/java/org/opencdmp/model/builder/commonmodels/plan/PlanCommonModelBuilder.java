package org.opencdmp.model.builder.commonmodels.plan;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.PlanAccessType;
import org.opencdmp.commonmodels.enums.PlanStatus;
import org.opencdmp.commonmodels.models.PlanUserModel;
import org.opencdmp.commonmodels.models.EntityDoiModel;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.UserModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plan.PlanStatusModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commonmodels.models.planreference.PlanReferenceModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.builder.commonmodels.*;
import org.opencdmp.model.builder.commonmodels.description.DescriptionCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.planblueprint.PlanBlueprintCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.planreference.PlanReferenceCommonModelBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.query.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanCommonModelBuilder extends BaseCommonModelBuilder<PlanModel, PlanEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final TenantEntityManager entityManager;
    private FileEnvelopeModel pdfFile;
    private FileEnvelopeModel rdaJsonFile;
    private String repositoryId;
    private String evaluatorId;
    private boolean disableDescriptions;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanCommonModelBuilder(ConventionService conventionService,
                                  QueryFactory queryFactory,
                                  BuilderFactory builderFactory, JsonHandlingService jsonHandlingService, XmlHandlingService xmlHandlingService, TenantEntityManager entityManager) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanCommonModelBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
	    this.jsonHandlingService = jsonHandlingService;
        this.xmlHandlingService = xmlHandlingService;
        this.entityManager = entityManager;
    }

    public PlanCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public PlanCommonModelBuilder setPdfFile(FileEnvelopeModel pdfFile) {
        this.pdfFile = pdfFile;
        return this;
    }

    public PlanCommonModelBuilder setRdaJsonFile(FileEnvelopeModel rdaJsonFile) {
        this.rdaJsonFile = rdaJsonFile;
        return this;
    }

    public PlanCommonModelBuilder setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    public PlanCommonModelBuilder setEvaluatorId(String evaluatorId){
        this.evaluatorId = evaluatorId;
        return this;
    }

    public PlanCommonModelBuilder setDisableDescriptions(boolean disableDescriptions) {
        this.disableDescriptions = disableDescriptions;
        return this;
    }


    private boolean useSharedStorage;
    public PlanCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    private boolean isPublic;
    public PlanCommonModelBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<PlanModel, PlanEntity>> buildInternal(List<PlanEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();
        
        List<CommonModelBuilderItemResponse<PlanModel, PlanEntity>> models = new ArrayList<>();
        
        Map<UUID, List<PlanReferenceModel>> planReferencesMap = this.collectPlanReferences(data);
        Map<UUID, List<PlanUserModel>> planUsersMap = this.collectPlanUsers(data);
        Map<UUID, List<DescriptionModel>> descriptionsMap = this.disableDescriptions ? null : this.collectPlanDescriptions(data);
        Map<UUID, List<EntityDoiModel>> entityDois = this.collectPlanEntityDois(data);
        Map<UUID, UserModel> creators = this.collectCreators(data);
        Map<UUID, PlanBlueprintModel> planBlueprints = this.collectPlanBlueprints(data);
        Map<UUID, DefinitionEntity> definitionEntityMap =  this.collectPlanBlueprintDefinitions(data);
        Map<UUID, PlanStatusModel> planStatuses = this.collectPlanStatuses(data);

        for (PlanEntity d : data) {
            PlanModel m = new PlanModel();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setVersion(d.getVersion());
            m.setDescription(d.getDescription());
            m.setFinalizedAt(d.getFinalizedAt());
            m.setCreatedAt(d.getCreatedAt());
            m.setLanguage(d.getLanguage());
            if (planStatuses != null && !planStatuses.isEmpty() && d.getStatusId() != null && planStatuses.containsKey(d.getStatusId())) m.setStatus(planStatuses.get(d.getStatusId()));
            if (entityDois != null && !entityDois.isEmpty() && entityDois.containsKey(d.getId())) m.setEntityDois(entityDois.get(d.getId()));
            if (creators != null && !creators.isEmpty() && d.getCreatorId() != null && creators.containsKey(d.getCreatorId())) m.setCreator(creators.get(d.getCreatorId()));
            if (planBlueprints != null && !planBlueprints.isEmpty() && d.getBlueprintId() != null && planBlueprints.containsKey(d.getBlueprintId())) m.setPlanBlueprint(planBlueprints.get(d.getBlueprintId()));
            if (d.getProperties() != null){
                //TODO Update with the new logic of property definition 
                PlanPropertiesEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, d.getProperties());
                m.setProperties(this.builderFactory.builder(PlanPropertiesCommonModelBuilder.class).withDefinition(definitionEntityMap != null ? definitionEntityMap.getOrDefault(d.getBlueprintId(), null) : null).authorize(this.authorize).build(propertyDefinition));
            }
            m.setPublicAfter(d.getPublicAfter());
            m.setUpdatedAt(d.getUpdatedAt());
            m.setPdfFile(this.pdfFile);
            m.setRdaJsonFile(this.rdaJsonFile);
            if (d.getVersion() > (short)1) m.setPreviousDOI(this.getPreviousDOI(d.getGroupId(), d.getId()));
            switch (d.getAccessType()){
                case Public -> m.setAccessType(PlanAccessType.Public);
                case Restricted -> m.setAccessType(PlanAccessType.Restricted);
                case null -> m.setAccessType(null);
                default -> throw new MyApplicationException("unrecognized type " + d.getAccessType());
            }
            
            if (planReferencesMap != null && !planReferencesMap.isEmpty() && planReferencesMap.containsKey(d.getId())) m.setReferences(planReferencesMap.get(d.getId()));
            if (planUsersMap != null && !planUsersMap.isEmpty() && planUsersMap.containsKey(d.getId())) m.setUsers(planUsersMap.get(d.getId()));
            if (descriptionsMap != null && !descriptionsMap.isEmpty() && descriptionsMap.containsKey(d.getId())) m.setDescriptions(descriptionsMap.get(d.getId()));

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private String getPreviousDOI(UUID planGroup, UUID currentPlanId) {
        if (this.repositoryId == null || this.repositoryId.isEmpty()) throw new MyApplicationException("repositoryId required");

        //GK: Step one get the previous version of the Data management plan
        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking();
        planQuery.setOrder(new Ordering().addDescending(Plan._version));
        List<UUID> planIds = planQuery.groupIds(planGroup).excludedIds(currentPlanId).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(Plan._id)).stream().map(PlanEntity::getId).toList();

        //GK: Step two get it's doiEntity
        List<EntityDoiEntity> dois = this.queryFactory.query(EntityDoiQuery.class).disableTracking().repositoryIds(this.repositoryId).entityIds(planIds).isActive(IsActive.Active).collectAs(new BaseFieldSet().ensure(EntityDoi._entityId, EntityDoi._doi));
        for(UUID uuid: planIds) {
            EntityDoiEntity doiEntity = dois.stream().filter(x -> x.getEntityId().equals(uuid)).findFirst().orElse(null);
            if (doiEntity != null) return doiEntity.getDoi();
        }
        return null;
    }

    private Map<UUID, List<PlanUserModel>> collectPlanUsers(List<PlanEntity> data) throws MyApplicationException {
        this.logger.debug("checking related - {}", PlanUser.class.getSimpleName());

        Map<UUID, List<PlanUserModel>> itemMap;
        PlanUserQuery query = this.queryFactory.query(PlanUserQuery.class).disableTracking().isActives(IsActive.Active).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanUserCommonModelBuilder.class).authorize(this.authorize).asMasterKey(query, PlanUserEntity::getPlanId);

        return itemMap;
    }

    private Map<UUID, List<PlanReferenceModel>> collectPlanReferences(List<PlanEntity> data) throws MyApplicationException {
        this.logger.debug("checking related - {}", PlanReference.class.getSimpleName());

        Map<UUID, List<PlanReferenceModel>> itemMap;
        PlanReferenceQuery query = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().isActives(IsActive.Active).authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanReferenceCommonModelBuilder.class).authorize(this.authorize).asMasterKey(query, PlanReferenceEntity::getPlanId);

        return itemMap;
    }

    private Map<UUID, List<DescriptionModel>> collectPlanDescriptions(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty()) return null;
        this.logger.debug("checking related - {}", Description.class.getSimpleName());

        Map<UUID, List<DescriptionModel>> itemMap;
        DescriptionQuery query = null;
        if (this.isPublic) {
            try {
                this.entityManager.disableTenantFilters();
                PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(org.opencdmp.commons.enums.PlanStatus.Finalized).isActives(IsActive.Active);
                query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList())).planSubQuery(this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(org.opencdmp.commons.enums.PlanAccessType.Public));
                itemMap = this.builderFactory.builder(DescriptionCommonModelBuilder.class).setRepositoryId(this.repositoryId).useSharedStorage(this.useSharedStorage).isPublic(this.isPublic).authorize(this.authorize).asMasterKey(query, DescriptionEntity::getPlanId);
                try {
                    this.entityManager.reloadTenantFilters();
                } catch (InvalidApplicationException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                try {
                    this.entityManager.reloadTenantFilters();
                } catch (InvalidApplicationException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            query = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionCommonModelBuilder.class).setRepositoryId(this.repositoryId).useSharedStorage(this.useSharedStorage).isPublic(this.isPublic).authorize(this.authorize).asMasterKey(query, DescriptionEntity::getPlanId);
        }

        return itemMap;
    }

    private Map<UUID, List<EntityDoiModel>> collectPlanEntityDois(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty()) return null;
        this.logger.debug("checking related - {}", EntityDoi.class.getSimpleName());

        Map<UUID, List<EntityDoiModel>> itemMap;
        EntityDoiQuery query = this.queryFactory.query(EntityDoiQuery.class).disableTracking().isActive(IsActive.Active).authorize(this.authorize).entityIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(EntityDoiCommonModelBuilder.class).authorize(this.authorize).asMasterKey(query, EntityDoiEntity::getEntityId);

        return itemMap;
    }

    private Map<UUID, UserModel> collectCreators(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", UserModel.class.getSimpleName());

        Map<UUID, UserModel> itemMap;
        UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().disableTracking().isActive(IsActive.Active).authorize(this.authorize).ids(data.stream().filter(x-> x.getCreatorId() != null).map(PlanEntity::getCreatorId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(UserCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, UserEntity::getId);
        return itemMap;
    }

    private Map<UUID, PlanBlueprintModel> collectPlanBlueprints(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanBlueprintModel.class.getSimpleName());

        Map<UUID, PlanBlueprintModel> itemMap;
        PlanBlueprintQuery q = this.queryFactory.query(PlanBlueprintQuery.class).isActive(IsActive.Active).authorize(this.authorize).ids(data.stream().filter(x-> x.getBlueprintId() != null).map(PlanEntity::getBlueprintId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanBlueprintCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, PlanBlueprintEntity::getId);
        return itemMap;
    }

    private Map<UUID, DefinitionEntity> collectPlanBlueprintDefinitions(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DefinitionEntity.class.getSimpleName());

        Map<UUID, DefinitionEntity> itemMap = new HashMap<>();
        PlanBlueprintQuery q = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanEntity::getBlueprintId).distinct().collect(Collectors.toList()));
        List<PlanBlueprintEntity> items = q.collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._definition));
        for (PlanBlueprintEntity item : items){
            DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, item.getDefinition());
            itemMap.put(item.getId(), definition);
        }

        return itemMap;
    }

    private Map<UUID, PlanStatusModel> collectPlanStatuses(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanStatusModel.class.getSimpleName());

        Map<UUID, PlanStatusModel> itemMap;
        PlanStatusQuery q = this.queryFactory.query(PlanStatusQuery.class).isActives(IsActive.Active).authorize(this.authorize).ids(data.stream().filter(x-> x.getStatusId() != null).map(PlanEntity::getStatusId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanStatusCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, PlanStatusEntity::getId);
        return itemMap;
    }

}
