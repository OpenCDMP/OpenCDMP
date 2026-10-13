package org.opencdmp.model.builder.reference;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanVersionStatus;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.externalfetcher.ExternalFetcherBaseSourceConfigurationEntity;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.commons.types.reference.FieldEntity;
import org.opencdmp.commons.types.referencetype.ReferenceTypeDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionReferenceEntity;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanReferenceEntity;
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.builder.planreference.PlanReferenceBuilder;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.externalfetcher.ExternalFetcherBaseSourceConfiguration;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.DescriptionReferenceQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanReferenceQuery;
import org.opencdmp.query.ReferenceQuery;
import org.opencdmp.query.ReferenceTypeQuery;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceBuilder extends BaseBuilder<Reference, ReferenceEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantScopeFactory tenantScopeFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private boolean isPublic;
    private boolean enableNavigationUrlFieldValues;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_-]+)}");

    @Autowired
    public ReferenceBuilder(
		    ConventionService conventionService,
		    BuilderFactory builderFactory, QueryFactory queryFactory, XmlHandlingService xmlHandlingService, TenantScopeFactory tenantScopeFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.xmlHandlingService = xmlHandlingService;
	    this.tenantScopeFactory = tenantScopeFactory;
    }

    public ReferenceBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    public ReferenceBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    public ReferenceBuilder enableNavigationUrlFieldValues(Boolean enableNavigationUrlFieldValues) {
        this.enableNavigationUrlFieldValues = enableNavigationUrlFieldValues;
        return this;
    }

    @Override
    public List<Reference> build(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        //ToDo FieldSet userInfoFields = fields.extractPrefixed(this.asPrefix(Reference._createdBy));
        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(Reference._definition));

        FieldSet planReferencesFields = fields.extractPrefixed(this.asPrefix(Reference._planReferences));
        Map<UUID, List<PlanReference>> planReferenceMap = this.collectPlanReferences(planReferencesFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(Reference._createdBy));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        FieldSet typeFields = fields.extractPrefixed(this.asPrefix(Reference._type));
        Map<UUID, ReferenceType> typeItemsMap = this.collectReferenceTypes(typeFields, data);

        List<UUID> ids = data.stream().map(ReferenceEntity::getId).toList();
        // Counted within the current tenant's filters: plans are private to their tenant even when the reference is shared.
        Map<UUID, Long> planCounts = !this.isPublic && fields.hasField(this.asIndexer(Reference._planCount)) ? this.countPlans(ids) : null;
        Map<UUID, Long> descriptionCounts = !this.isPublic && fields.hasField(this.asIndexer(Reference._descriptionCount)) ? this.countDescriptions(ids) : null;

        List<Reference> models = new ArrayList<>();
        for (ReferenceEntity d : data) {
            Reference m = new Reference();
            if (fields.hasField(this.asIndexer(Reference._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Reference._label))) m.setLabel(d.getLabel());
            if (!this.isPublic && fields.hasField(this.asIndexer(Reference._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (!this.isPublic && fields.hasField(this.asIndexer(Reference._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Reference._isActive))) m.setIsActive(d.getIsActive());
            if (!this.isPublic && fields.hasField(this.asIndexer(Reference._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (!this.isPublic && !definitionFields.isEmpty() && d.getDefinition() != null){
                DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(DefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            if (fields.hasField(this.asIndexer(Reference._reference))) m.setReference(d.getReference());
            if (!this.isPublic && fields.hasField(this.asIndexer(Reference._abbreviation))) m.setAbbreviation(d.getAbbreviation());
            if (!this.isPublic && fields.hasField(this.asIndexer(Reference._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Reference._source))) m.setSource(d.getSource());
            if (fields.hasField(this.asIndexer(Reference._sourceType))) m.setSourceType(d.getSourceType());
            if (!typeFields.isEmpty() && typeItemsMap != null && typeItemsMap.containsKey(d.getTypeId())) m.setType(typeItemsMap.get(d.getTypeId()));
            if (planReferenceMap != null && !planReferenceMap.isEmpty() && planReferenceMap.containsKey(d.getId())) m.setPlanReferences(planReferenceMap.get(d.getId()));
            if (!this.isPublic && !userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getCreatedById())) m.setCreatedBy(userItemsMap.get(d.getCreatedById()));
            if (!this.isPublic && fields.hasField(this.asIndexer(Reference._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));
            if (planCounts != null) m.setPlanCount(planCounts.getOrDefault(d.getId(), 0L));
            if (descriptionCounts != null) m.setDescriptionCount(descriptionCounts.getOrDefault(d.getId(), 0L));
            models.add(m);
        }

        if (this.enableNavigationUrlFieldValues) this.applyNavigationUrlValues(models);

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }

    private Map<UUID, ReferenceType> collectReferenceTypes(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", ReferenceType.class.getSimpleName());

        Map<UUID, ReferenceType> itemMap;
        if (!fields.hasOtherField(this.asIndexer(ReferenceType._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()),
                    x -> {
                        ReferenceType item = new ReferenceType();
                        item.setId(x);
                        return item;
                    },
                    ReferenceType::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(ReferenceType._id);
            ReferenceTypeQuery q = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(ReferenceEntity::getTypeId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(this.authorize).isPublic(this.isPublic).enableNavigationUrlFieldValues(this.enableNavigationUrlFieldValues).asForeignKey(q, clone, ReferenceType::getId);
        }
        if (!fields.hasField(ReferenceType._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty() || this.isPublic)
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(ReferenceEntity::getCreatedById).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(ReferenceEntity::getCreatedById).distinct().collect(Collectors.toList()));
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

    private Map<UUID, List<PlanReference>> collectPlanReferences(FieldSet fields, List<ReferenceEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty()) return null;
        this.logger.debug("checking related - {}", PlanReference.class.getSimpleName());

        Map<UUID, List<PlanReference>> itemMap = null;
        FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(PlanReference._reference, Reference._id));
        PlanReferenceQuery query = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().authorize(this.authorize).referenceIds(data.stream().map(ReferenceEntity::getId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(PlanReferenceBuilder.class).authorize(this.authorize).isPublic(this.isPublic).asMasterKey(query, clone, x -> x.getReference().getId());

        if (!fields.hasField(this.asIndexer(PlanReference._reference, Reference._id))) {
            itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getReference() != null).map(x -> {
                x.getReference().setId(null);
                return x;
            }).collect(Collectors.toList());
        }
        return itemMap;
    }

    private void applyNavigationUrlValues(List<Reference> models) {
        if (models == null || models.isEmpty()) {
            return;
        }

        List<UUID> ids = models.stream().map(Reference::getId).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return;
        }
        this.logger.debug("applying reference type source navigation urls field mapping - {}", Reference.class.getSimpleName());

        List<ReferenceEntity> referenceEntities = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(this.authorize).ids(ids).collect();
        List<ReferenceTypeEntity> referenceTypeEntities = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().authorize(this.authorize).collect();

        if (referenceEntities == null || referenceTypeEntities == null)
            return;

        for (ReferenceEntity entity: referenceEntities) {
            // replace code value from source.getNavigationUrl()
            ReferenceTypeEntity referenceTypeEntity = referenceTypeEntities.stream().filter(x -> x.getId().equals(entity.getTypeId())).findFirst().orElse(null);
            if (referenceTypeEntity != null) {
                ReferenceTypeDefinitionEntity referenceTypeDefinition = this.xmlHandlingService.fromXmlSafe(ReferenceTypeDefinitionEntity.class, referenceTypeEntity.getDefinition());
                if (referenceTypeDefinition != null && !this.conventionService.isListNullOrEmpty(referenceTypeDefinition.getSources())) {
                    ExternalFetcherBaseSourceConfigurationEntity sourceEntity = referenceTypeDefinition.getSources().stream().filter(x -> x.getKey() != null && x.getKey().equals(entity.getSource())).findFirst().orElse(null);
                    if (sourceEntity != null && !this.conventionService.isNullOrEmpty(sourceEntity.getNavigationUrl())) {
                        List<String> fields = this.extractPlaceholders(sourceEntity.getNavigationUrl());
                        String newUrl = sourceEntity.getNavigationUrl();
                        for (String field: fields) {

                            if (ReferenceEntity.KnownFields.ReferenceId.equals(field)) {
                                newUrl = this.replacePlaceholder(newUrl, ReferenceEntity.KnownFields.ReferenceId, entity.getReference());
                            } else if (ReferenceEntity.KnownFields.Label.equals(field)) {
                                newUrl = this.replacePlaceholder(newUrl, ReferenceEntity.KnownFields.Label, entity.getLabel());
                            } else if (ReferenceEntity.KnownFields.Description.equals(field)) {
                                newUrl = this.replacePlaceholder(newUrl, ReferenceEntity.KnownFields.Description, entity.getDescription());
                            } else if (entity.getDefinition() != null){
                                DefinitionEntity referenceDefinition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, entity.getDefinition());
                                if (referenceDefinition != null && !this.conventionService.isListNullOrEmpty(referenceDefinition.getFields())) {
                                    for (FieldEntity fieldEntity: referenceDefinition.getFields()) {
                                        if (fieldEntity != null && fieldEntity.getCode().equals(field)) {
                                            newUrl = this.replacePlaceholder(newUrl,fieldEntity.getCode(), fieldEntity.getValue());
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        Reference model = models.stream().filter(x -> x.getId() != null && x.getId().equals(entity.getId())).findFirst().orElse(null);
                        if (model != null) {
                            ReferenceType referenceType = model.getType();
                            if (referenceType != null && referenceType.getDefinition() != null && !this.conventionService.isListNullOrEmpty(referenceType.getDefinition().getSources())) {
                                ExternalFetcherBaseSourceConfiguration sourceModel = referenceType.getDefinition().getSources().stream().filter(x -> x.getKey() != null && x.getKey().equals(sourceEntity.getKey())).findFirst().orElse(null);
                                if (sourceModel != null && !this.conventionService.isNullOrEmpty(sourceModel.getNavigationUrl())) {
                                    sourceModel.setNavigationUrl(newUrl);
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private List<String> extractPlaceholders(String navigationUrl) {
        List<String> placeholders = new ArrayList<>();

        if (navigationUrl == null || navigationUrl.isBlank()) {
            return placeholders;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(navigationUrl);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }

        return placeholders;
    }

    private String replacePlaceholder(String navigationUrl, String placeholder, String value) {
        if (navigationUrl == null || navigationUrl.isBlank()) {
            return navigationUrl;
        }

        if (placeholder == null || placeholder.isBlank()) {
            return navigationUrl;
        }

        String code = "{" + placeholder + "}";

        if (!navigationUrl.contains(code)) {
            return navigationUrl;
        }

        return navigationUrl.replace(code, value != null ? value : "");
    }

    private Map<UUID, Long> countPlans(List<UUID> ids) {
        if (ids.isEmpty()) return new HashMap<>();
        // a plan counts once however many versions it has, so the references are grouped by the plan's groupId
        PlanReferenceQuery planReferenceSubQuery = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceIds(ids).isActives(IsActive.Active);
        Map<UUID, UUID> groupIds = this.queryFactory.query(PlanQuery.class).disableTracking().planReferenceSubQuery(planReferenceSubQuery).isActive(IsActive.Active).versionStatuses(PlanVersionStatus.Current, PlanVersionStatus.NotFinalized)
                .collectAs(new BaseFieldSet(Plan._id, Plan._groupId)).stream().collect(Collectors.toMap(PlanEntity::getId, PlanEntity::getGroupId));

        List<PlanReferenceEntity> planReferences = this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceIds(ids).isActives(IsActive.Active)
                .collectAs(new BaseFieldSet(this.asIndexer(PlanReference._plan, Plan._id), this.asIndexer(PlanReference._reference, Reference._id)));
        return planReferences.stream().filter(x -> groupIds.containsKey(x.getPlanId())).collect(Collectors.groupingBy(
                PlanReferenceEntity::getReferenceId,
                Collectors.collectingAndThen(Collectors.mapping(x -> groupIds.get(x.getPlanId()), Collectors.toSet()), x -> (long) x.size())
        ));
    }

    private Map<UUID, Long> countDescriptions(List<UUID> ids) {
        if (ids.isEmpty()) return new HashMap<>();
        PlanQuery planSubQuery = this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).versionStatuses(PlanVersionStatus.Current, PlanVersionStatus.NotFinalized);
        DescriptionQuery descriptionSubQuery = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).planSubQuery(planSubQuery);
        List<DescriptionReferenceEntity> descriptionReferences = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().referenceIds(ids).isActive(IsActive.Active).descriptionSubQuery(descriptionSubQuery)
                .collectAs(new BaseFieldSet(this.asIndexer(DescriptionReference._description, Description._id), this.asIndexer(DescriptionReference._reference, Reference._id)));
        return descriptionReferences.stream().collect(Collectors.groupingBy(
                DescriptionReferenceEntity::getReferenceId,
                Collectors.collectingAndThen(Collectors.mapping(DescriptionReferenceEntity::getDescriptionId, Collectors.toSet()), x -> (long) x.size())
        ));
    }
}
