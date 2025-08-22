package org.opencdmp.model.builder.commonmodels.description;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.DescriptionStatus;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.description.DescriptionStatusModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.model.DescriptionTag;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.DescriptionTemplateCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.plan.PlanCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.query.*;
import org.opencdmp.service.visibility.VisibilityService;
import org.opencdmp.service.visibility.VisibilityServiceImpl;
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
public class DescriptionCommonModelBuilder extends BaseCommonModelBuilder<DescriptionModel, DescriptionEntity> {

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;
    private final JsonHandlingService jsonHandlingService;
    private final XmlHandlingService xmlHandlingService;
    private final TenantEntityManager entityManager;
    
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    private String repositoryId;

    @Autowired
    public DescriptionCommonModelBuilder(
            ConventionService conventionService,
            QueryFactory queryFactory,
            BuilderFactory builderFactory, JsonHandlingService jsonHandlingService, XmlHandlingService xmlHandlingService, TenantEntityManager entityManager) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionCommonModelBuilder.class)));
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.jsonHandlingService = jsonHandlingService;
	    this.xmlHandlingService = xmlHandlingService;
        this.entityManager = entityManager;
    }

    public DescriptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    private boolean useSharedStorage;
    public DescriptionCommonModelBuilder useSharedStorage(boolean useSharedStorage) {
        this.useSharedStorage = useSharedStorage;
        return this;
    }

    public DescriptionCommonModelBuilder setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    private boolean isPublic;
    public DescriptionCommonModelBuilder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<DescriptionModel, DescriptionEntity>> buildInternal(List<DescriptionEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        Map<UUID, DescriptionTemplateModel> descriptionTemplates = this.collectDescriptionTemplates(data);
        Map<UUID, PlanModel> plans = this.collectPlans(data);

        Map<UUID, DefinitionEntity> definitionEntityMap =  this.collectDescriptionTemplateDefinitions(data);
        Map<UUID, UUID> planDescriptionTemplateSections =  this.collectPlanDescriptionTemplateSections(data);

        Map<UUID, DescriptionStatusModel> descriptionStatuses =  this.collectDescriptionStatuses(data);
        Map<UUID, List<String>> descriptionTagsMap = this.collectDescriptionTags(data);

        List<CommonModelBuilderItemResponse<DescriptionModel, DescriptionEntity>> models = new ArrayList<>();
        for (DescriptionEntity d : data) {
            DescriptionModel m = new DescriptionModel();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            m.setDescription(d.getDescription());
            if (descriptionStatuses != null && d.getStatusId() != null && descriptionStatuses.containsKey(d.getStatusId())) m.setStatus(descriptionStatuses.get(d.getStatusId()));
            m.setCreatedAt(d.getCreatedAt());
            m.setDescription(d.getDescription());
            if (plans != null && d.getPlanId() != null && plans.containsKey(d.getPlanId())) m.setPlan(plans.get(d.getPlanId()));
            if (planDescriptionTemplateSections != null && d.getPlanDescriptionTemplateId() != null && planDescriptionTemplateSections.containsKey(d.getPlanDescriptionTemplateId())) m.setSectionId(planDescriptionTemplateSections.get(d.getPlanDescriptionTemplateId()));
            if (descriptionTemplates != null && d.getDescriptionTemplateId() != null && descriptionTemplates.containsKey(d.getDescriptionTemplateId())) m.setDescriptionTemplate(descriptionTemplates.get(d.getDescriptionTemplateId()));
            if (d.getProperties() != null){
                PropertyDefinitionEntity propertyDefinition = this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, d.getProperties());
                DefinitionEntity definition = definitionEntityMap != null ? definitionEntityMap.getOrDefault(d.getDescriptionTemplateId(), null) : null;
                m.setProperties(this.builderFactory.builder(PropertyDefinitionCommonModelBuilder.class).useSharedStorage(this.useSharedStorage).withDefinition(definition).authorize(this.authorize).build(propertyDefinition));
                VisibilityService visibilityService = new VisibilityServiceImpl(definition, propertyDefinition);
                m.setVisibilityStates(this.builderFactory.builder(VisibilityStateModelBuilder.class).authorize(this.authorize).build(visibilityService.getVisibilityStates().entrySet().stream().toList()));
            }
            if (descriptionTagsMap != null && descriptionTagsMap.containsKey(d.getId())) m.setTags(descriptionTagsMap.get(d.getId()));
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

    private Map<UUID, DefinitionEntity> collectDescriptionTemplateDefinitions(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DefinitionEntity.class.getSimpleName());

        Map<java.util.UUID, DefinitionEntity> itemMap = new HashMap<>();
        DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
        List<DescriptionTemplateEntity> items = q.collectAs(new BaseFieldSet().ensure(DescriptionTemplate._id).ensure(DescriptionTemplate._definition));
        for (DescriptionTemplateEntity item : items){
            DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, item.getDefinition());
            itemMap.put(item.getId(), definition);
        }

        return itemMap;
    }

    private Map<UUID, UUID> collectPlanDescriptionTemplateSections(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DefinitionEntity.class.getSimpleName());

        Map<UUID, UUID> itemMap = new HashMap<>();
        PlanDescriptionTemplateQuery q = null;
        List<PlanDescriptionTemplateEntity> items;
        if (this.isPublic) {
            try {
                this.entityManager.disableTenantFilters();
                q =this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().collect(Collectors.toList()));
                items = q.collectAs(new BaseFieldSet().ensure(PlanDescriptionTemplate._id).ensure(PlanDescriptionTemplate._sectionId));
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
            q =this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getPlanDescriptionTemplateId).distinct().collect(Collectors.toList()));
            items = q.collectAs(new BaseFieldSet().ensure(PlanDescriptionTemplate._id).ensure(PlanDescriptionTemplate._sectionId));
        }
        for (PlanDescriptionTemplateEntity item : items){
            itemMap.put(item.getId(), item.getSectionId());
        }

        return itemMap;
    }

    private Map<UUID, PlanModel> collectPlans(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", PlanModel.class.getSimpleName());

        Map<UUID, PlanModel> itemMap;
        PlanQuery q = null;
        if (this.isPublic) {
            try {
                this.entityManager.disableTenantFilters();
                PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
                q = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList())).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public);
                itemMap = this.builderFactory.builder(PlanCommonModelBuilder.class).setRepositoryId(this.repositoryId).useSharedStorage(this.useSharedStorage).setDisableDescriptions(true).authorize(this.authorize).asForeignKey(q, PlanEntity::getId);
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
            q = this.queryFactory.query(PlanQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(DescriptionEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanCommonModelBuilder.class).setRepositoryId(this.repositoryId).useSharedStorage(this.useSharedStorage).setDisableDescriptions(true).authorize(this.authorize).asForeignKey(q, PlanEntity::getId);
        }

        return itemMap;
    }

    private Map<UUID, DescriptionTemplateModel> collectDescriptionTemplates(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionTemplateModel.class.getSimpleName());

        Map<UUID, DescriptionTemplateModel> itemMap;
        DescriptionTemplateQuery q = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getDescriptionTemplateId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionTemplateCommonModelBuilder.class).useSharedStorage(useSharedStorage).setRepositoryId(repositoryId).authorize(this.authorize).asForeignKey(q, DescriptionTemplateEntity::getId);

        return itemMap;
    }

    private Map<UUID, DescriptionStatusModel> collectDescriptionStatuses(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", DescriptionStatusModel.class.getSimpleName());

        Map<UUID, DescriptionStatusModel> itemMap;
        DescriptionStatusQuery q = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(DescriptionEntity::getStatusId).distinct().collect(Collectors.toList()));
        itemMap = this.builderFactory.builder(DescriptionStatusCommonModelBuilder.class).authorize(this.authorize).asForeignKey(q, DescriptionStatusEntity::getId);

        return itemMap;
    }

    private Map<UUID, List<String>> collectDescriptionTags(List<DescriptionEntity> data) throws MyApplicationException {
        if (data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", String.class.getSimpleName());

        Map<UUID, List<String>> itemMap = new HashMap<>();
        List<DescriptionTagEntity> descriptionTagEntities = this.queryFactory.query(DescriptionTagQuery.class).disableTracking().authorize(this.authorize).descriptionIds(data.stream().map(DescriptionEntity::getId).distinct().collect(Collectors.toList())).collect();
        List<TagEntity> existingTags = this.queryFactory.query(TagQuery.class).authorize(this.authorize).disableTracking().ids(descriptionTagEntities.stream().map(DescriptionTagEntity::getTagId).distinct().collect(Collectors.toList())).collectAs(new BaseFieldSet().ensure(Tag._id).ensure(Tag._label));
        for (DescriptionEntity description: data) {
            List<DescriptionTagEntity> tagsByDescription = descriptionTagEntities.stream().filter(x -> x.getDescriptionId().equals(description.getId())).toList();
            if (!this.conventionService.isListNullOrEmpty(tagsByDescription)) {
                List<UUID> tagIds = tagsByDescription.stream().map(DescriptionTagEntity::getTagId).toList();
                if (!this.conventionService.isListNullOrEmpty(tagIds)){
                    List<TagEntity> tags = existingTags.stream().filter(x -> tagIds.contains(x.getId())).toList();
                    if (!this.conventionService.isListNullOrEmpty(tags)) {
                        itemMap.put(description.getId(), tags.stream().map(TagEntity::getLabel).toList());
                    }
                }
            }
        }

        return itemMap;
    }

}
