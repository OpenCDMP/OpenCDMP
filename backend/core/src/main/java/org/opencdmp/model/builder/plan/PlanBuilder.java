package org.opencdmp.model.builder.plan;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.builder.*;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.builder.planblueprint.PlanBlueprintBuilder;
import org.opencdmp.model.builder.planreference.PlanReferenceBuilder;
import org.opencdmp.model.builder.planstatus.PlanStatusBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.planstatus.PlanStatusDefinitionAuthorization;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.opencdmp.service.planstatus.PlanStatusService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanBuilder extends BaseBuilder<Plan, PlanEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final AuthorizationService authorizationService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final TenantScope tenantScope;
    private final CustomPolicyService customPolicyService;
    private final PlanStatusService planStatusService;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PlanBuilder(ConventionService conventionService,
                       QueryFactory queryFactory,
                       BuilderFactory builderFactory, JsonHandlingService jsonHandlingService, XmlHandlingService xmlHandlingService, AuthorizationService authorizationService, AuthorizationContentResolver authorizationContentResolver, TenantScope tenantScope, CustomPolicyService customPolicyService, PlanStatusService planStatusService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
	    this.jsonHandlingService = jsonHandlingService;
        this.xmlHandlingService = xmlHandlingService;
        this.authorizationService = authorizationService;
	    this.authorizationContentResolver = authorizationContentResolver;
	    this.tenantScope = tenantScope;
        this.customPolicyService = customPolicyService;
        this.planStatusService = planStatusService;
    }

    public PlanBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Plan> build(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<Plan> models = new ArrayList<>();

        FieldSet statusFields = fields.extractPrefixed(this.asPrefix(Plan._status));
        Map<UUID, PlanStatus> statusItemsMap = this.collectPlanStatuses(statusFields, data);

        FieldSet availableStatusesFields = fields.extractPrefixed(this.asPrefix(Plan._availableStatuses));
        Map<UUID, List<PlanStatus>> avaialbleStatusesItemsMap = this.collectAvailablePlanStatuses(availableStatusesFields, data);

        FieldSet entityDoisFields = fields.extractPrefixed(this.asPrefix(Plan._entityDois));
        Map<UUID, List<EntityDoi>> entityDoisMap = this.collectEntityDois(entityDoisFields, data);

        FieldSet plamReferencesFields = fields.extractPrefixed(this.asPrefix(Plan._planReferences));
        Map<UUID, List<PlanReference>> planReferencesMap = this.collectPlanReferences(plamReferencesFields, data);

        FieldSet planUsersFields = fields.extractPrefixed(this.asPrefix(Plan._planUsers));
        Map<UUID, List<PlanUser>> planUsersMap = this.collectPlanUsers(planUsersFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(Plan._creator));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        FieldSet blueprintFields = fields.extractPrefixed(this.asPrefix(Plan._blueprint));
        Map<UUID, PlanBlueprint> blueprintItemsMap = this.collectPlanBlueprints(blueprintFields, data);

        FieldSet descriptionsFields = fields.extractPrefixed(this.asPrefix(Plan._descriptions));
        Map<UUID, List<Description>> descriptionsMap = this.collectPlanDescriptions(descriptionsFields, data);

        FieldSet planDescriptionTemplatesFields = fields.extractPrefixed(this.asPrefix(Plan._planDescriptionTemplates));
        Map<UUID, List<PlanDescriptionTemplate>> planDescriptionTemplatesMap = this.collectPlanDescriptionTemplates(planDescriptionTemplatesFields, data);

        FieldSet otherPlanVersionsFields = fields.extractPrefixed(this.asPrefix(Plan._otherPlanVersions));
        Map<UUID, List<Plan>> otherPlanVersionsMap = this.collectOtherPlanVersions(otherPlanVersionsFields, data);

        FieldSet planPropertiesFields = fields.extractPrefixed(this.asPrefix(Plan._properties));
        Map<UUID, DefinitionEntity> definitionEntityMap = !planPropertiesFields.isEmpty() ? this.collectPlanBlueprintDefinitions(data) : null;

        Set<String> authorizationFlags = this.extractAuthorizationFlags(fields, Plan._authorizationFlags, this.authorizationContentResolver.getPermissionNames());
        Map<UUID, AffiliatedResource>  affiliatedResourceMap = authorizationFlags == null || authorizationFlags.isEmpty() ? null : this.authorizationContentResolver.plansAffiliation(data.stream().map(PlanEntity::getId).collect(Collectors.toList()));

        FieldSet statusAuthorizationFlags = fields.extractPrefixed(this.asPrefix(Plan._statusAuthorizationFlags));
        for (PlanEntity d : data) {
            Plan m = new Plan();
            if (fields.hasField(this.asIndexer(Plan._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Plan._tenantId))) m.setTenantId(d.getTenantId());
            if (fields.hasField(this.asIndexer(Plan._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Plan._version))) m.setVersion(d.getVersion());
            if (!statusFields.isEmpty() && statusItemsMap != null && statusItemsMap.containsKey(d.getStatusId())) m.setStatus(statusItemsMap.get(d.getStatusId()));
            if (avaialbleStatusesItemsMap != null && !avaialbleStatusesItemsMap.isEmpty() && avaialbleStatusesItemsMap.containsKey(d.getId())) m.setAvailableStatuses(avaialbleStatusesItemsMap.get(d.getId()));
            if (fields.hasField(this.asIndexer(Plan._groupId))) m.setGroupId(d.getGroupId());
            if (fields.hasField(this.asIndexer(Plan._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Plan._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(Plan._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Plan._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(Plan._finalizedAt))) m.setFinalizedAt(d.getFinalizedAt());
            if (fields.hasField(this.asIndexer(Plan._accessType))) m.setAccessType(d.getAccessType());
            if (fields.hasField(this.asIndexer(Plan._language))) m.setLanguage(d.getLanguage());
            if (fields.hasField(this.asIndexer(Plan._versionStatus))) m.setVersionStatus(d.getVersionStatus());
            if (fields.hasField(this.asIndexer(Plan._publicAfter))) m.setPublicAfter(d.getPublicAfter());
            if (fields.hasField(this.asIndexer(Plan._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(Plan._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getCreatorId())) m.setCreator(userItemsMap.get(d.getCreatorId()));
            if (!blueprintFields.isEmpty() && blueprintItemsMap != null && blueprintItemsMap.containsKey(d.getBlueprintId())) m.setBlueprint(blueprintItemsMap.get(d.getBlueprintId()));
            if (entityDoisMap != null && !entityDoisMap.isEmpty() && entityDoisMap.containsKey(d.getId())) m.setEntityDois(entityDoisMap.get(d.getId()));
            if (planReferencesMap != null && !planReferencesMap.isEmpty() && planReferencesMap.containsKey(d.getId())) m.setPlanReferences(planReferencesMap.get(d.getId()));
            if (planUsersMap != null && !planUsersMap.isEmpty() && planUsersMap.containsKey(d.getId())) m.setPlanUsers(planUsersMap.get(d.getId()));
            if (descriptionsMap != null && !descriptionsMap.isEmpty() && descriptionsMap.containsKey(d.getId())) m.setDescriptions(descriptionsMap.get(d.getId()));
            if (planDescriptionTemplatesMap != null && !planDescriptionTemplatesMap.isEmpty() && planDescriptionTemplatesMap.containsKey(d.getId())) m.setPlanDescriptionTemplates(planDescriptionTemplatesMap.get(d.getId()));
            if (otherPlanVersionsMap != null && !otherPlanVersionsMap.isEmpty() && otherPlanVersionsMap.containsKey(d.getGroupId())) {
                m.setOtherPlanVersions(otherPlanVersionsMap.get(d.getGroupId()));
                m.getOtherPlanVersions().sort(Comparator.comparing(Plan::getVersion));
            }
            if (!planPropertiesFields.isEmpty() && d.getProperties() != null){
                PlanPropertiesEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, d.getProperties());
                m.setProperties(this.builderFactory.builder(PlanPropertiesBuilder.class).withDefinition(definitionEntityMap != null ? definitionEntityMap.getOrDefault(d.getBlueprintId(), null) : null).authorize(this.authorize).build(planPropertiesFields, propertyDefinition));
            }
            if (affiliatedResourceMap != null && !authorizationFlags.isEmpty()) m.setAuthorizationFlags(this.evaluateAuthorizationFlags(this.authorizationService, authorizationFlags, affiliatedResourceMap.getOrDefault(d.getId(), null)));
            if (!statusAuthorizationFlags.isEmpty() && !this.conventionService.isListNullOrEmpty(m.getAvailableStatuses())) {
                m.setStatusAuthorizationFlags(this.evaluateStatusAuthorizationFlags(this.authorizationService, statusAuthorizationFlags, d));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, PlanStatus> collectPlanStatuses(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanStatus.class.getSimpleName());

        Map<UUID, PlanStatus> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PlanStatus._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanEntity::getStatusId).distinct().collect(Collectors.toList()),
                    x -> {
                        PlanStatus item = new PlanStatus();
                        item.setId(x);
                        return item;
                    },
                    PlanStatus::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PlanStatus._id);
            PlanStatusQuery q = this.queryFactory.query(PlanStatusQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanEntity::getStatusId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanStatusBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PlanStatus::getId);
        }
        if (!fields.hasField(PlanStatus._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PlanStatus>> collectAvailablePlanStatuses(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PlanStatus.class.getSimpleName());

        Map<UUID, List<PlanStatus>> itemMap = new HashMap<>();
        FieldSet fieldSet = new BaseFieldSet(fields.getFields()).ensure(PlanStatus._id);
        Map<UUID, List<UUID>> itemStatusIdsMap = this.planStatusService.getAuthorizedAvailableStatusIds(data.stream().map(PlanEntity::getId).collect(Collectors.toList()));

        List<PlanStatusEntity> statusEntities = this.queryFactory.query(PlanStatusQuery.class).authorize(this.authorize).isActives(IsActive.Active).ids(itemStatusIdsMap.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList())).collectAs(fieldSet);
        List<PlanStatus> planStatuses = this.builderFactory.builder(PlanStatusBuilder.class).authorize(this.authorize).build(fieldSet, statusEntities);

        for (PlanEntity entity: data) {
            itemMap.put(entity.getId(), new ArrayList<>());
            List<UUID> statusIds = itemStatusIdsMap.getOrDefault(entity.getId(), new ArrayList<>());
            for (UUID statusId: statusIds) {
                itemMap.get(entity.getId()).addAll(planStatuses.stream().filter(x -> x.getId().equals(statusId)).collect(Collectors.toList()));
            }
        }

        return itemMap;
    }

    private Map<UUID, List<PlanReference>> collectPlanReferences(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PlanReference.class.getSimpleName());

        Map<UUID, List<PlanReference>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanReference._plan, PlanReference._id));
        PlanReferenceQuery query = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanReferenceBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(PlanReference._plan, Plan._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<EntityDoi>> collectEntityDois(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", EntityDoi.class.getSimpleName());

        Map<UUID, List<EntityDoi>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(EntityDoi._entityId));
        EntityDoiQuery query = this.queryFactory.query(EntityDoiQuery.class).disableTracking().authorize(this.authorize).types(EntityType.Plan).entityIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(EntityDoiBuilder.class).authorize(this.authorize).asMasterKey(query, clone, EntityDoi::getEntityId);

        if (!fields.hasField(this.asIndexer(EntityDoi._entityId))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getEntityId() != null).forEach(x -> {
                x.setEntityId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, PlanBlueprint> collectPlanBlueprints(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanBlueprint.class.getSimpleName());

        Map<UUID, PlanBlueprint> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PlanBlueprint._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanEntity::getBlueprintId).distinct().collect(Collectors.toList()),
                    x -> {
                        PlanBlueprint item = new PlanBlueprint();
                        item.setId(x);
                        return item;
                    },
                    PlanBlueprint::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PlanBlueprint._id);
            PlanBlueprintQuery q = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanEntity::getBlueprintId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PlanBlueprint::getId);
        }
        if (!fields.hasField(PlanBlueprint._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, DefinitionEntity> collectPlanBlueprintDefinitions(List<PlanEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DefinitionEntity.class.getSimpleName());

        Map<java.util.UUID, DefinitionEntity> itemMap = new HashMap<>();
        PlanBlueprintQuery q = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanEntity::getBlueprintId).distinct().collect(Collectors.toList()));
        List<PlanBlueprintEntity> items = q.collectAs(new BaseFieldSet().ensure(PlanBlueprint._id).ensure(PlanBlueprint._definition));
        for (PlanBlueprintEntity item : items){
            DefinitionEntity definition =this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, item.getDefinition());
            itemMap.put(item.getId(), definition);
        }

        return itemMap;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanEntity::getCreatorId).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanEntity::getCreatorId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, User::getId);
        }
        if (!fields.hasField(User._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PlanUser>> collectPlanUsers(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanUser.class.getSimpleName());

        Map<UUID, List<PlanUser>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanUser._plan, PlanUser._id));
        PlanUserQuery query = this.queryFactory.query(PlanUserQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanUserBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(PlanUser._plan, Plan._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<Description>> collectPlanDescriptions(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", Description.class.getSimpleName());

        Map<UUID, List<Description>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(Description._plan, Description._id));
        DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(Description._plan, Plan._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<PlanDescriptionTemplate>> collectPlanDescriptionTemplates(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", Description.class.getSimpleName());

        Map<UUID, List<PlanDescriptionTemplate>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanDescriptionTemplate._plan, PlanDescriptionTemplate._id));
        PlanDescriptionTemplateQuery query = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).planIds(data.stream().map(PlanEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanDescriptionTemplateBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getPlan().getId());

        if (!fields.hasField(this.asIndexer(PlanDescriptionTemplate._plan, Plan._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getPlan() != null).forEach(x -> {
                x.getPlan().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<Plan>> collectOtherPlanVersions(FieldSet fields, List<PlanEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, List<Plan>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Plan._id);
        PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(this.authorize).groupIds(data.stream().map(PlanEntity::getGroupId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanBuilder.class).authorize(this.authorize).asMasterKey(query, clone, Plan::getGroupId);

        if (!fields.hasField(Plan._id)) {
            itemMap.values().stream().flatMap(List::stream).filter(Objects::nonNull).forEach(x -> {
                x.setId(null);
            });
        }

        return itemMap;
    }

    private List<String> evaluateStatusAuthorizationFlags(AuthorizationService authorizationService, FieldSet statusAuthorizationFlags, PlanEntity plan) {
        List<String> allowed = new ArrayList<>();
        if (statusAuthorizationFlags == null) return allowed;
        if (authorizationService == null) return allowed;
        if (plan == null) return allowed;

        String editPermission = this.customPolicyService.getPlanStatusCanEditStatusPermission(plan.getStatusId());
        AffiliatedResource affiliatedResource = this.authorizationContentResolver.planAffiliation(plan.getId());
        for (String permission : statusAuthorizationFlags.getFields()) {
            if (statusAuthorizationFlags.hasField(this.asIndexer(PlanStatusDefinitionAuthorization._edit))) {
                Boolean isAllowed = affiliatedResource == null ? this.authorizationService.authorize(editPermission) : this.authorizationService.authorizeAtLeastOne(List.of(affiliatedResource), editPermission);
                if (isAllowed) allowed.add(permission);
            }
        }

        return allowed;
    }

}
