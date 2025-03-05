package org.opencdmp.model.builder.description;

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
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.DescriptionTagBuilder;
import org.opencdmp.model.builder.PlanDescriptionTemplateBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.builder.descriptionreference.DescriptionReferenceBuilder;
import org.opencdmp.model.builder.descriptionstatus.DescriptionStatusBuilder;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinitionAuthorization;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.opencdmp.service.descriptionstatus.DescriptionStatusService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionBuilder extends BaseBuilder<Description, DescriptionEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final AuthorizationService authorizationService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final TenantScope tenantScope;
    private final CustomPolicyService customPolicyService;
    private final DescriptionStatusService descriptionStatusService;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionBuilder(
            ConventionService conventionService,
            QueryFactory queryFactory,
            BuilderFactory builderFactory, JsonHandlingService jsonHandlingService, XmlHandlingService xmlHandlingService, AuthorizationService authorizationService, AuthorizationContentResolver authorizationContentResolver, TenantScope tenantScope, CustomPolicyService customPolicyService, DescriptionStatusService descriptionStatusService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.jsonHandlingService = jsonHandlingService;
	    this.xmlHandlingService = xmlHandlingService;
	    this.authorizationService = authorizationService;
	    this.authorizationContentResolver = authorizationContentResolver;
	    this.tenantScope = tenantScope;
        this.customPolicyService = customPolicyService;
        this.descriptionStatusService = descriptionStatusService;
    }

    public DescriptionBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Description> build(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet statusFields = fields.extractPrefixed(this.asPrefix(Description._status));
        Map<UUID, DescriptionStatus> statusItemsMap = this.collectDescriptionStatuses(statusFields, data);

        FieldSet availableStatusesFields = fields.extractPrefixed(this.asPrefix(Description._availableStatuses));
        Map<UUID, List<DescriptionStatus>> avaialbleStatusesItemsMap = this.collectAvailableDescriptionStatuses(availableStatusesFields, data);

        FieldSet planDescriptionTemplateFields = fields.extractPrefixed(this.asPrefix(Description._planDescriptionTemplate));
        Map<UUID, PlanDescriptionTemplate> planDescriptionTemplateItemsMap = this.collectPlanDescriptionTemplates(planDescriptionTemplateFields, data);

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(Description._plan));
        Map<UUID, Plan> planItemsMap = this.collectPlans(planFields, data);

        FieldSet descriptionTemplateFields = fields.extractPrefixed(this.asPrefix(Description._descriptionTemplate));
        Map<UUID, DescriptionTemplate> descriptionTemplateItemsMap = this.collectDescriptionTemplates(descriptionTemplateFields, data);

        FieldSet descriptionReferencesFields = fields.extractPrefixed(this.asPrefix(Description._descriptionReferences));
        Map<UUID, List<DescriptionReference>> descriptionReferencesMap = this.collectDescriptionReferences(descriptionReferencesFields, data);

        FieldSet descriptionTagsFields = fields.extractPrefixed(this.asPrefix(Description._descriptionTags));
        Map<UUID, List<DescriptionTag>> descriptionTagsMap = this.collectDescriptionTags(descriptionTagsFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(Description._createdBy));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        FieldSet definitionPropertiesFields = fields.extractPrefixed(this.asPrefix(Description._properties));

        Map<UUID, DefinitionEntity> definitionEntityMap = !definitionPropertiesFields.isEmpty() ? this.collectDescriptionTemplateDefinitions(data) : null;

        Set<String> authorizationFlags = this.extractAuthorizationFlags(fields, Description._authorizationFlags, this.authorizationContentResolver.getPermissionNames());
        Map<UUID, AffiliatedResource>  affiliatedResourceMap = authorizationFlags == null || authorizationFlags.isEmpty() ? null : this.authorizationContentResolver.descriptionsAffiliation(data.stream().map(DescriptionEntity::getId).collect(Collectors.toList()));

        FieldSet statusAuthorizationFlags = fields.extractPrefixed(this.asPrefix(Description._statusAuthorizationFlags));
        List<Description> models = new ArrayList<>();
        for (DescriptionEntity d : data) {
            Description m = new Description();
            if (fields.hasField(this.asIndexer(Description._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Description._tenantId))) m.setTenantId(d.getTenantId());
            if (fields.hasField(this.asIndexer(Description._label)))  m.setLabel(d.getLabel());
            if (!statusFields.isEmpty() && statusItemsMap != null && statusItemsMap.containsKey(d.getStatusId())) m.setStatus(statusItemsMap.get(d.getStatusId()));
            if (avaialbleStatusesItemsMap != null && !avaialbleStatusesItemsMap.isEmpty() && avaialbleStatusesItemsMap.containsKey(d.getId())) m.setAvailableStatuses(avaialbleStatusesItemsMap.get(d.getId()));
            if (fields.hasField(this.asIndexer(Description._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Description._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(Description._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Description._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(Description._finalizedAt))) m.setFinalizedAt(d.getFinalizedAt());
            if (fields.hasField(this.asIndexer(Description._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(Description._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId()))  m.setPlan(planItemsMap.get(d.getPlanId()));
            if (!planDescriptionTemplateFields.isEmpty() && planDescriptionTemplateItemsMap != null && planDescriptionTemplateItemsMap.containsKey(d.getPlanDescriptionTemplateId()))  m.setPlanDescriptionTemplate(planDescriptionTemplateItemsMap.get(d.getPlanDescriptionTemplateId()));
            if (!descriptionTemplateFields.isEmpty() && descriptionTemplateItemsMap != null && descriptionTemplateItemsMap.containsKey(d.getDescriptionTemplateId()))  m.setDescriptionTemplate(descriptionTemplateItemsMap.get(d.getDescriptionTemplateId()));
            if (!descriptionReferencesFields.isEmpty() && descriptionReferencesMap != null && descriptionReferencesMap.containsKey(d.getId())) m.setDescriptionReferences(descriptionReferencesMap.get(d.getId()));
            if (!descriptionTagsFields.isEmpty() && descriptionTagsMap != null && descriptionTagsMap.containsKey(d.getId())) m.setDescriptionTags(descriptionTagsMap.get(d.getId()));
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getCreatedById())) m.setCreatedBy(userItemsMap.get(d.getCreatedById()));
            if (!definitionPropertiesFields.isEmpty() && d.getProperties() != null){
                PropertyDefinitionEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, d.getProperties());
                m.setProperties(this.builderFactory.builder(PropertyDefinitionBuilder.class).withDefinition(definitionEntityMap != null ? definitionEntityMap.getOrDefault(d.getDescriptionTemplateId(), null) : null).authorize(this.authorize).build(definitionPropertiesFields, propertyDefinition));
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

    private Map<UUID, DescriptionStatus> collectDescriptionStatuses(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionStatus.class.getSimpleName());

        Map<UUID, DescriptionStatus> itemMap;
        if (!fields.hasOtherField(this.asIndexer(DescriptionStatus._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getStatusId).distinct().collect(Collectors.toList()),
                    x -> {
                        DescriptionStatus item = new DescriptionStatus();
                        item.setId(x);
                        return item;
                    },
                    DescriptionStatus::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(DescriptionStatus._id);
            DescriptionStatusQuery q = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getStatusId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionStatusBuilder.class).authorize(this.authorize).asForeignKey(q, clone, DescriptionStatus::getId);
        }
        if (!fields.hasField(DescriptionStatus._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<DescriptionStatus>> collectAvailableDescriptionStatuses(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", DescriptionStatus.class.getSimpleName());

        Map<UUID, List<DescriptionStatus>> itemMap = new HashMap<>();
        FieldSet fieldSet = new BaseFieldSet(fields.getFields()).ensure(DescriptionStatus._id);
        Map<UUID, List<UUID>> itemStatusIdsMap = this.descriptionStatusService.getAuthorizedAvailableStatusIds(data.stream().map(DescriptionEntity::getId).collect(Collectors.toList()));

        List<DescriptionStatusEntity> statusEntities = this.queryFactory.query(DescriptionStatusQuery.class).authorize(this.authorize).isActive(IsActive.Active).ids(itemStatusIdsMap.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList())).collectAs(fieldSet);
        List<DescriptionStatus> descriptionStatuses = this.builderFactory.builder(DescriptionStatusBuilder.class).authorize(this.authorize).build(fieldSet, statusEntities);

        for (DescriptionEntity entity: data) {
            itemMap.put(entity.getId(), new ArrayList<>());
            List<UUID> statusIds = itemStatusIdsMap.getOrDefault(entity.getId(), new ArrayList<>());
            for (UUID statusId: statusIds) {
                itemMap.get(entity.getId()).addAll(descriptionStatuses.stream().filter(x -> x.getId().equals(statusId)).collect(Collectors.toList()));
            }
        }

        return itemMap;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getCreatedById).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getCreatedById).distinct().collect(Collectors.toList()));
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

    private Map<UUID, PlanDescriptionTemplate> collectPlanDescriptionTemplates(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanDescriptionTemplate.class.getSimpleName());

        Map<UUID, PlanDescriptionTemplate> itemMap;
        if (!fields.hasOtherField(this.asIndexer(PlanDescriptionTemplate._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().collect(Collectors.toList()),
                    x -> {
                        PlanDescriptionTemplate item = new PlanDescriptionTemplate();
                        item.setId(x);
                        return item;
                    },
                    PlanDescriptionTemplate::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(PlanDescriptionTemplate._id);
            PlanDescriptionTemplateQuery q = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanDescriptionTemplateBuilder.class).authorize(this.authorize).asForeignKey(q, clone, PlanDescriptionTemplate::getId);
        }
        if (!fields.hasField(PlanDescriptionTemplate._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, DescriptionTemplate> collectDescriptionTemplates(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplate.class.getSimpleName());

        Map<UUID, DescriptionTemplate> itemMap;
        if (!fields.hasOtherField(this.asIndexer(DescriptionTemplate._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()),
                    x -> {
                        DescriptionTemplate item = new DescriptionTemplate();
                        item.setId(x);
                        return item;
                    },
                    DescriptionTemplate::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(DescriptionTemplate._id);
            DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(this.authorize).asForeignKey(q, clone, DescriptionTemplate::getId);
        }
        if (!fields.hasField(DescriptionTemplate._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, DefinitionEntity> collectDescriptionTemplateDefinitions(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DefinitionEntity.class.getSimpleName());

        Map<java.util.UUID, DefinitionEntity> itemMap = new HashMap<>();
        DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
        List<DescriptionTemplateEntity> items = q.collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._definition));
        for (DescriptionTemplateEntity item : items){
            DefinitionEntity definition =this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, item.getDefinition());
            itemMap.put(item.getId(), definition);
        }

        return itemMap;
    }

    private Map<UUID, Plan> collectPlans(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, Plan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Plan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        Plan item = new Plan();
                        item.setId(x);
                        return item;
                    },
                    Plan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Plan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Plan::getId);
        }
        if (!fields.hasField(Plan._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<DescriptionReference>> collectDescriptionReferences(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", DescriptionReference.class.getSimpleName());

        Map<UUID, List<DescriptionReference>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(DescriptionReference._description, Description._id));
        DescriptionReferenceQuery query = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().authorize(this.authorize).descriptionIds(data.stream().map(DescriptionEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionReferenceBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getDescription().getId());

        if (!fields.hasField(this.asIndexer(DescriptionReference._description, Description._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getDescription() != null).forEach(x -> {
                x.getDescription().setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, List<DescriptionTag>> collectDescriptionTags(FieldSet fields, List<DescriptionEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", DescriptionTag.class.getSimpleName());

        Map<UUID, List<DescriptionTag>> itemMap;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(DescriptionTag._description, Description._id));
        DescriptionTagQuery query = this.queryFactory.query(DescriptionTagQuery.class).disableTracking().authorize(this.authorize).descriptionIds(data.stream().map(DescriptionEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionTagBuilder.class).authorize(this.authorize).asMasterKey(query, clone, x -> x.getDescription().getId());

        if (!fields.hasField(this.asIndexer(DescriptionTag._description, Description._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getDescription() != null).forEach(x -> {
                x.getDescription().setId(null);
            });
        }

        return itemMap;
    }

    private List<String> evaluateStatusAuthorizationFlags(AuthorizationService authorizationService, FieldSet statusAuthorizationFlags, DescriptionEntity description) {
        List<String> allowed = new ArrayList<>();
        if (statusAuthorizationFlags == null) return allowed;
        if (authorizationService == null) return allowed;
        if (description == null) return allowed;

        String editPermission = this.customPolicyService.getDescriptionStatusCanEditStatusPermission(description.getStatusId());
        AffiliatedResource affiliatedResource = this.authorizationContentResolver.descriptionAffiliation(description.getId());
        for (String permission : statusAuthorizationFlags.getFields()) {
            if (statusAuthorizationFlags.hasField(this.asIndexer(DescriptionStatusDefinitionAuthorization._edit))) {
                Boolean isAllowed = affiliatedResource == null ? this.authorizationService.authorize(editPermission) : this.authorizationService.authorizeAtLeastOne(List.of(affiliatedResource), editPermission);
                if (isAllowed) allowed.add(permission);
            }
        }

        return allowed;
    }

}
