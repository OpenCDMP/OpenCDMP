package org.opencdmp.service.planblueprint;

import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.FieldSetModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateSectionImportExport;
import org.opencdmp.commons.types.planblueprint.DefinitionEntity;
import org.opencdmp.commons.types.planblueprinttype.PlanBlueprintTypeImportExport;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.model.descriptiontemplate.Field;
import org.opencdmp.model.descriptiontemplate.Rule;
import org.opencdmp.model.descriptiontemplate.Section;
import org.opencdmp.model.persist.descriptiontemplatedefinition.SectionPersist;
import org.opencdmp.model.planblueprint.*;
import org.opencdmp.service.iconconfiguration.IconConfigurationService;
import org.opencdmp.service.planblueprintdescriptiontemplatefields.PlanBlueprintDescriptionTemplateFieldsService;
import tools.jackson.core.JacksonException;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commonmodels.models.planblueprint.*;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.enums.kpi.KpiVersionType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.model.persist.descriptiontemplatedefinition.FieldSetPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.RulePersist;
import org.opencdmp.commons.types.planblueprint.*;
import org.opencdmp.commons.types.planblueprint.BlueprintDescriptionTemplateEntity;
import org.opencdmp.commons.types.planblueprint.importexport.*;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.importexport.PluginConfigurationImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.planblueprint.PlanBlueprintBuilder;
import org.opencdmp.model.deleter.PlanBlueprintDeleter;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.persist.NewVersionPlanBlueprintPersist;
import org.opencdmp.model.persist.PlanBlueprintPersist;
import org.opencdmp.model.persist.planblueprintdefinition.*;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.kpi.KpiService;
import org.opencdmp.service.lock.LockService;
import org.opencdmp.service.pluginconfiguration.PluginConfigurationService;
import org.opencdmp.service.pluginintegration.PluginIntegrationService;
import org.opencdmp.service.responseutils.ResponseUtilsService;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanBlueprintServiceImpl implements PlanBlueprintService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintServiceImpl.class));

    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final MessageSource messageSource;

    private final QueryFactory queryFactory;

    private final ResponseUtilsService responseUtilsService;

    private final XmlHandlingService xmlHandlingService;

    private final ErrorThesaurusProperties errors;

    private final ValidatorFactory validatorFactory;
    private final TenantScopeFactory tenantScopeFactory;

    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;

    private final LockService lockService;
    private final StorageFileService storageFileService;

    private final PluginConfigurationService pluginConfigurationService;
    private final PluginIntegrationService pluginIntegrationService;

    private final KpiService kpiService;

    private final IconConfigurationService iconConfigurationService;

    private final org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider fieldDataHelperServiceProvider;

    private final PlanBlueprintDescriptionTemplateFieldsService fieldsService;

    @Autowired
    public PlanBlueprintServiceImpl(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            MessageSource messageSource, QueryFactory queryFactory,
            ResponseUtilsService responseUtilsService,
            XmlHandlingService xmlHandlingService,
            ErrorThesaurusProperties errors,
            PluginConfigurationService pluginConfigurationService, PluginIntegrationService pluginIntegrationService,
            ValidatorFactory validatorFactory, TenantScopeFactory tenantScopeFactory, UsageLimitService usageLimitService, AccountingService accountingService, LockService lockService, StorageFileService storageFileService, KpiService kpiService, IconConfigurationService iconConfigurationService,
            org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider fieldDataHelperServiceProvider, PlanBlueprintDescriptionTemplateFieldsService fieldsService) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.queryFactory = queryFactory;
        this.responseUtilsService = responseUtilsService;
        this.xmlHandlingService = xmlHandlingService;
        this.errors = errors;
        this.validatorFactory = validatorFactory;
	    this.tenantScopeFactory = tenantScopeFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.lockService = lockService;
        this.storageFileService = storageFileService;
        this.pluginConfigurationService = pluginConfigurationService;
        this.pluginIntegrationService = pluginIntegrationService;
        this.kpiService = kpiService;
        this.iconConfigurationService = iconConfigurationService;
        this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
        this.fieldsService = fieldsService;
    }

    //region Persist

    /**
     * Every semantic key the tenant's other blueprints carry, with how many of them (counted by group, so versions of
     * one blueprint count once). The editors suggest these first, so custom vocabularies stay consistent.
     */
    public Map<String, Integer> semanticsInUse(UUID excludeGroupId) {
        this.authorizationService.authorizeForce(Permission.BrowsePlanBlueprint);
        List<PlanBlueprintEntity> items = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).isActive(IsActive.Active).collect();
        Map<String, Set<UUID>> groups = new HashMap<>();
        for (PlanBlueprintEntity item : items) {
            if (item.getGroupId() == null || item.getGroupId().equals(excludeGroupId) || this.conventionService.isNullOrEmpty(item.getDefinition())) continue;
            DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, item.getDefinition());
            if (definition == null || definition.getSections() == null) continue;
            // keys on inputs, answer options, questions and sections all count
            definition.getSections().forEach(section -> section.getAllSemantics().stream().filter(k -> k != null && !k.isBlank())
                    .forEach(k -> groups.computeIfAbsent(k, x -> new HashSet<>()).add(item.getGroupId())));
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        groups.entrySet().stream().sorted((a, b) -> b.getValue().size() - a.getValue().size()).forEach(e -> result.put(e.getKey(), e.getValue().size()));
        return result;
    }

    public PlanBlueprint persist(PlanBlueprintPersist model, UUID groupId, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JacksonException, TransformerException, ParserConfigurationException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanBlueprint);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanBlueprintEntity data;
        if (isUpdate) {
            data = this.tenantEntityManagerFactory.getInstance().find(PlanBlueprintEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (this.lockService.isLocked(data.getId(), null).getStatus()) throw new MyApplicationException(this.errors.getLockedPlanBlueprint().getCode(), this.errors.getLockedPlanBlueprint().getMessage());
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash()))
                throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            if (data.getStatus().equals(PlanBlueprintStatus.Finalized))
                throw new MyForbiddenException("Cannot update finalized blueprint");
            if (!data.getCode().equals(model.getCode()))
                throw new MyForbiddenException("Code can not change");
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.BLUEPRINT_COUNT);
            data = new PlanBlueprintEntity();
            data.setId(UUID.randomUUID());
            data.setStatus(PlanBlueprintStatus.Draft);
            data.setGroupId(groupId != null ? groupId : UUID.randomUUID());
            data.setVersion((short) 1);
            data.setVersionStatus(PlanBlueprintVersionStatus.NotFinalized);
            data.setCreatedAt(Instant.now());
            data.setIsActive(IsActive.Active);
            long activeDescriptionTemplatesForTheGroup = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .groupIds(data.getGroupId())
                    .count();
            if (activeDescriptionTemplatesForTheGroup > 0) throw new MyApplicationException("Description template group id is in use please use new version endpoint");
        }
        if (groupId != null && !data.getGroupId().equals(groupId)) throw new MyApplicationException("Can not change description template group id");

        PlanBlueprintStatus previousStatus = data.getStatus();
        PlanBlueprintVersionStatus previousVersionStatus = data.getVersionStatus();
        UUID previousType = data.getTypeId();

        if (model.getDefinition() != null && model.getDefinition().getSections().stream().noneMatch(RootSectionPersist::getHasTemplates)) {
            throw new MyValidationException(this.errors.getPlanBlueprintHasNoDescriptionTemplates().getCode(), this.errors.getPlanBlueprintHasNoDescriptionTemplates().getMessage());
        }

        data.setLabel(model.getLabel());
        data.setCode(model.getCode());
        data.setStatus(model.getStatus());
        data.setUpdatedAt(Instant.now());
        data.setTypeId(model.getType());

        DefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(data.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, data.getDefinition());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), oldDefinition, null)));
        data.setDescription(model.getDescription());

        if (isUpdate) {
            this.tenantEntityManagerFactory.getInstance().merge(data);
            if (previousStatus != null && previousStatus.equals(PlanBlueprintStatus.Draft) && data.getStatus().equals(PlanBlueprintStatus.Finalized)) {
                this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());
                this.accountingService.decrease(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
            }
            if (previousType != null && !previousType.equals(data.getTypeId())) {
                this.kpiService.sendIndicatorPointPlanBlueprintPerTypeEntry(KpiDirectionType.Decrease, previousType);
                if (data.getTypeId() != null) this.kpiService.sendIndicatorPointPlanBlueprintPerTypeEntry(KpiDirectionType.Increase, data.getTypeId());
            } else if (previousType == null && data.getTypeId() != null){
                this.kpiService.sendIndicatorPointPlanBlueprintPerTypeEntry(KpiDirectionType.Increase, data.getTypeId());
            }
        } else {
            this.tenantEntityManagerFactory.getInstance().persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue());
            this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Increase, KpiVersionType.TotalCount);
            if (data.getStatus().equals(PlanBlueprintStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
            if (data.getStatus().equals(PlanBlueprintStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());
            if (data.getTypeId() != null) this.kpiService.sendIndicatorPointPlanBlueprintPerTypeEntry(KpiDirectionType.Increase, data.getTypeId());
        }

        this.tenantEntityManagerFactory.getInstance().flush();

        if (!isUpdate) {
            Long planBlueprintCodes = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(model.getCode())
                    .count();

            if (planBlueprintCodes > 0) throw new MyValidationException(this.errors.getPlanBlueprintCodeExists().getCode(), this.errors.getPlanBlueprintCodeExists().getMessage());
        }

        this.updateVersionStatusAndSave(data, previousStatus, data.getStatus());

        this.tenantEntityManagerFactory.getInstance().flush();

        if (previousVersionStatus != null && !previousVersionStatus.equals(data.getVersionStatus()) && data.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)) {
            this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Increase, KpiVersionType.LatestVersion);
        }

        return this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, PlanBlueprint._id), data);
    }

    private void updateVersionStatusAndSave(PlanBlueprintEntity data, PlanBlueprintStatus previousStatus, PlanBlueprintStatus newStatus) throws InvalidApplicationException {
        if (previousStatus.equals(newStatus))
            return;
        if (previousStatus.equals(PlanBlueprintStatus.Finalized))
            throw new MyForbiddenException("Can not update finalized blueprint");

        if (newStatus.equals(PlanBlueprintStatus.Finalized)) {
            List<PlanBlueprintEntity> latestVersionPlanBlueprints = this.queryFactory.query(PlanBlueprintQuery.class)
                    .disableTracking()
                    .versionStatuses(PlanBlueprintVersionStatus.Current)
                    .isActive(IsActive.Active)
                    .groupIds(data.getGroupId())
                    .collect();
            if (latestVersionPlanBlueprints.size() > 1) throw new MyValidationException(this.errors.getMultiplePlanVersionsNotSupported().getCode(), this.errors.getMultiplePlanVersionsNotSupported().getMessage());
            PlanBlueprintEntity oldPlanBlueprintEntity = latestVersionPlanBlueprints.stream().findFirst().orElse(null);

            data.setVersionStatus(PlanBlueprintVersionStatus.Current);

            if (oldPlanBlueprintEntity != null) {
                data.setVersion((short) (oldPlanBlueprintEntity.getVersion() + 1));

                oldPlanBlueprintEntity.setVersionStatus(PlanBlueprintVersionStatus.Previous);
                this.tenantEntityManagerFactory.getInstance().merge(oldPlanBlueprintEntity);
                this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Decrease, KpiVersionType.LatestVersion);
            } else {
                data.setVersion((short) 1);
            }
        }

    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist, DefinitionEntity oldValue, List<PluginConfigurationEntity> inheritedPlugins) throws InvalidApplicationException {
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null)
            return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (RootSectionPersist rootSectionPersist : persist.getSections()) {
                data.getSections().add(this.buildSectionEntity(rootSectionPersist));
            }
        }

        if (!this.conventionService.isListNullOrEmpty(persist.getPluginConfigurations())) {
            data.setPluginConfigurations(new ArrayList<>());
            for (PluginConfigurationPersist pluginConfigurationPersist : persist.getPluginConfigurations()) {
                data.getPluginConfigurations().add(this.pluginConfigurationService.buildPluginConfigurationEntity(pluginConfigurationPersist, oldValue != null ? oldValue.getPluginConfigurations(): null, inheritedPlugins, this.pluginIntegrationService.secretFieldCodes(pluginConfigurationPersist.getPluginType(), pluginConfigurationPersist.getPluginCode())));
            }
        }

        data.setPlanCreationConsent(this.buildPlanCreationConsentEntity(persist.getPlanCreationConsent()));

        UUID oldStorageFileId = null;
        if (oldValue != null && oldValue.getIconConfiguration() != null && oldValue.getIconConfiguration().getStorageFileId() != null) {
            oldStorageFileId = oldValue.getIconConfiguration().getStorageFileId();
        }
        data.setIconConfiguration(this.iconConfigurationService.buildIconConfigurationEntity(persist.getIconConfiguration(), oldStorageFileId));

        return data;
    }

    private @NotNull RootSectionEntity buildSectionEntity(RootSectionPersist persist) {
        RootSectionEntity data = new RootSectionEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setDescription(persist.getDescription());
        data.setOrdinal(persist.getOrdinal());
        data.setTitle(persist.getTitle());
        data.setSemantics(persist.getSemantics());

        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (SectionPersist sectionPersist : persist.getSections()) {
                data.getSections().add(this.fieldsService.buildSectionEntity(sectionPersist));
            }
        }

        if (!this.conventionService.isListNullOrEmpty(persist.getFieldSets())) {
            data.setFieldSets(this.fieldsService.buildFieldSetEntities(persist.getFieldSets()));
        }

        data.setHasTemplates(persist.getHasTemplates());
        data.setPrefillingSourcesEnabled(persist.getPrefillingSourcesEnabled());
        data.setCanEditDescriptionTemplates(persist.getCanEditDescriptionTemplates());
        data.setPrefillingSourcesIds(persist.getPrefillingSourcesIds());

        if (!this.conventionService.isListNullOrEmpty(persist.getDescriptionTemplates())) {
            data.setDescriptionTemplates(new ArrayList<>());
            for (BlueprintDescriptionTemplatePersist descriptionTemplatePersist : persist.getDescriptionTemplates()) {
                data.getDescriptionTemplates().add(this.buildDescriptionTemplateEntity(descriptionTemplatePersist));
            }
        }

        return data;
    }

    private @NotNull BlueprintDescriptionTemplateEntity buildDescriptionTemplateEntity(BlueprintDescriptionTemplatePersist persist) {
        BlueprintDescriptionTemplateEntity data = new BlueprintDescriptionTemplateEntity();
        if (persist == null)
            return data;

        data.setDescriptionTemplateGroupId(persist.getDescriptionTemplateGroupId());
        data.setMaxMultiplicity(persist.getMaxMultiplicity());
        data.setMinMultiplicity(persist.getMinMultiplicity());

        return data;
    }

    private @NotNull PlanCreationConsentEntity buildPlanCreationConsentEntity(PlanCreationConsentPersist persist) {
        PlanCreationConsentEntity data = new PlanCreationConsentEntity();
        if (persist == null)
            return data;

        data.setEnabled(persist.getEnabled());
        data.setMessage(persist.getMessage());
        data.setDetails(persist.getDetails());

        return data;
    }
    //endregion

    //region Delete

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanBlueprint);

        PlanBlueprintEntity data = this.tenantEntityManagerFactory.getInstance().find(PlanBlueprintEntity.class, id);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!data.getVersionStatus().equals(PlanBlueprintVersionStatus.Previous)){
            PlanBlueprintQuery planBlueprintQuery = this.queryFactory.query(PlanBlueprintQuery.class)
                    .excludedIds(data.getId())
                    .isActive(IsActive.Active)
                    .groupIds(data.getGroupId());

            planBlueprintQuery.setOrder(new Ordering().addDescending(PlanBlueprint._version));
            PlanBlueprintEntity previousPlanBlueprint = planBlueprintQuery.count() > 0 ? planBlueprintQuery.collect().getFirst() : null;
            if (previousPlanBlueprint != null){
                if (previousPlanBlueprint.getStatus().equals(PlanBlueprintStatus.Finalized)) previousPlanBlueprint.setVersionStatus(PlanBlueprintVersionStatus.Current);
                else previousPlanBlueprint.setVersionStatus(PlanBlueprintVersionStatus.NotFinalized);
                this.tenantEntityManagerFactory.getInstance().merge(previousPlanBlueprint);
                this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Increase, KpiVersionType.LatestVersion);
            }
            this.tenantEntityManagerFactory.getInstance().merge(data);
            this.tenantEntityManagerFactory.getInstance().flush();
        }

        this.deleterFactory.deleter(PlanBlueprintDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    //endregion

    //region Clone

    public PlanBlueprint buildClone(UUID id, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException {
        logger.debug(new MapLogEntry("persisting data").And("id", id).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ClonePlanBlueprint);

        PlanBlueprintQuery query = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PlanBlueprint model = this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fields, query.firstAs(fields));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        model.setLabel(model.getLabel() + " new ");
        model.setId(null);
        model.setHash(null);
        model.setStatus(PlanBlueprintStatus.Draft);
        this.reassignDefinition(model.getDefinition());

        return model;
    }

    private void reassignDefinition(Definition model) {
        if (model == null)
            return;

        Map<String, String> visibilityRulesMap = new HashMap<>();
        List<Field> fields = this.getAllFieldByDefinition(model);
        if (!this.conventionService.isListNullOrEmpty(fields)){
            for (Field field: fields) {
                if (!this.conventionService.isListNullOrEmpty(field.getVisibilityRules())){
                    field.getVisibilityRules().stream().map(x -> x.getTarget()).collect(Collectors.toList()).forEach(y -> {
                        if (!visibilityRulesMap.containsKey(y)) visibilityRulesMap.put(y, null);
                    });
                }
            }
        }
        if (model.getSections() != null) {
            for (RootSection section : model.getSections()) {
                this.fieldsService.reassignSection(section, visibilityRulesMap);
            }
        }

        if (model.getPluginConfigurations() != null) {
            for (PluginConfiguration pluginConfiguration : model.getPluginConfigurations()) {
                this.pluginConfigurationService.reassignPluginConfiguration(pluginConfiguration);
            }
        }

        for (Field field: this.getAllFieldByDefinition(model)) {
            if (!this.conventionService.isListNullOrEmpty(field.getVisibilityRules())){
                for (Rule rule: field.getVisibilityRules()) {
                    if (visibilityRulesMap.containsKey(rule.getTarget())) rule.setTarget(visibilityRulesMap.get(rule.getTarget()));
                }
            }
        }

        if (model.getIconConfiguration() != null) {
            this.iconConfigurationService.reassignIconConfiguration(model.getIconConfiguration());
        }
    }


    private List<Field> getAllFieldByDefinition(Definition definition){
        List<Field> fields = new ArrayList<>();
        if (definition == null) return  fields;

        if (definition.getSections() != null){
            fields.addAll(this.getAllFieldBySections(definition.getSections()));
        }
        return fields;
    }


    private List<Field> getAllFieldBySections(List<? extends Section> sections){
        List<Field> fields = new ArrayList<>();

        if (sections != null){
            for (Section section: sections) {
                if (section.getSections() != null) fields.addAll(this.getAllFieldBySections(section.getSections()));
                if (section.getFieldSets() != null) {
                    for (org.opencdmp.model.descriptiontemplate.FieldSet fieldSet: section.getFieldSets()) {
                        fields.addAll(fieldSet.getFields());
                    }
                }

            }
        }
        return fields;
    }

    //endregion

    //region NewVersion

    @Override
    public PlanBlueprint createNewVersion(NewVersionPlanBlueprintPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data planBlueprint").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.CreateNewVersionPlanBlueprint);
        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.BLUEPRINT_COUNT);

        PlanBlueprintEntity oldPlanBlueprintEntity = this.tenantEntityManagerFactory.getInstance().find(PlanBlueprintEntity.class, model.getId(), true);
        if (oldPlanBlueprintEntity == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(oldPlanBlueprintEntity.getUpdatedAt()).equals(model.getHash()))
            throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        if (!this.tenantScopeFactory.getInstance().isSet() || !Objects.equals(oldPlanBlueprintEntity.getTenantId(), this.tenantScopeFactory.getInstance().getTenant())) throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());

        List<PlanBlueprintEntity> latestVersionPlanBlueprints = this.queryFactory.query(PlanBlueprintQuery.class)
                .disableTracking()
                .versionStatuses(PlanBlueprintVersionStatus.Current)
                .isActive(IsActive.Active)
                .statuses(PlanBlueprintStatus.Finalized)
                .groupIds(oldPlanBlueprintEntity.getGroupId())
                .collect();
        if (latestVersionPlanBlueprints.isEmpty()) throw new MyValidationException(this.errors.getPlanIsNotFinalized().getCode(), this.errors.getPlanIsNotFinalized().getMessage());
        if (latestVersionPlanBlueprints.size() > 1) throw new MyValidationException(this.errors.getMultiplePlanVersionsNotSupported().getCode(), this.errors.getMultiplePlanVersionsNotSupported().getMessage());
        if (!latestVersionPlanBlueprints.getFirst().getVersion().equals(oldPlanBlueprintEntity.getVersion())) throw new MyValidationException(this.errors.getPlanBlueprintNewVersionConflict().getCode(), this.errors.getPlanBlueprintNewVersionConflict().getMessage());
        Long notFinalizedCount = this.queryFactory.query(PlanBlueprintQuery.class)
                .disableTracking()
                .versionStatuses(PlanBlueprintVersionStatus.NotFinalized)
                .groupIds(oldPlanBlueprintEntity.getGroupId())
                .isActive(IsActive.Active)
                .count();
        if (notFinalizedCount > 0)
            throw new MyValidationException(this.errors.getPlanBlueprintNewVersionAlreadyCreatedDraft().getCode(), this.errors.getPlanBlueprintNewVersionAlreadyCreatedDraft().getMessage());

        PlanBlueprintEntity data = new PlanBlueprintEntity();
        data.setId(UUID.randomUUID());
        data.setLabel(model.getLabel());
        data.setDescription(model.getDescription());
        data.setStatus(model.getStatus());

        if (!this.conventionService.isListNullOrEmpty(model.getDefinition().getPluginConfigurations())) {
            DefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(oldPlanBlueprintEntity.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, oldPlanBlueprintEntity.getDefinition());
            if (oldDefinition != null && !this.conventionService.isListNullOrEmpty(oldDefinition.getPluginConfigurations())) {
                // reassign new storage files if equals with old
                this.pluginConfigurationService.reassignNewStorageFilesIfEqualsWithOld(model.getDefinition().getPluginConfigurations(), oldDefinition.getPluginConfigurations());
            }
        }

        if (model.getDefinition().getIconConfiguration() != null) {
            // reassign new icon storage file if equals with old
            this.iconConfigurationService.reassignNewStorageFilesIfEqualsWithOld(model.getDefinition().getIconConfiguration());
        }
        // the new version keeps the previous one's secrets, which the editor never receives
        DefinitionEntity previousDefinition = this.conventionService.isNullOrEmpty(oldPlanBlueprintEntity.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, oldPlanBlueprintEntity.getDefinition());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), null, previousDefinition != null ? previousDefinition.getPluginConfigurations() : null)));

        data.setGroupId(oldPlanBlueprintEntity.getGroupId());
        data.setCode(oldPlanBlueprintEntity.getCode());
        data.setVersion((short) (oldPlanBlueprintEntity.getVersion() + 1));
        data.setVersionStatus(PlanBlueprintVersionStatus.NotFinalized);
        data.setCreatedAt(Instant.now());
        data.setUpdatedAt(Instant.now());
        data.setIsActive(IsActive.Active);
        data.setTypeId(model.getType());

        this.tenantEntityManagerFactory.getInstance().persist(data);

        this.tenantEntityManagerFactory.getInstance().flush();

        this.updateVersionStatusAndSave(data, PlanBlueprintStatus.Draft, data.getStatus());

        this.tenantEntityManagerFactory.getInstance().flush();

        this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue());
        if (data.getStatus().equals(PlanBlueprintStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
        if (data.getStatus().equals(PlanBlueprintStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());

        if (oldPlanBlueprintEntity.getVersionStatus() != null && !oldPlanBlueprintEntity.getVersionStatus().equals(data.getVersionStatus()) && data.getVersionStatus().equals(PlanBlueprintVersionStatus.Current)) {
            this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Increase, KpiVersionType.LatestVersion);
        }
        this.kpiService.sendIndicatorPointPlanBlueprintEntry(KpiDirectionType.Increase, KpiVersionType.TotalCount);
        if (data.getTypeId() != null) this.kpiService.sendIndicatorPointPlanBlueprintPerTypeEntry(KpiDirectionType.Increase, data.getTypeId());
        return this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, PlanBlueprint._id), data);
    }

    //endregion

    //region Export


    @Override
    public BlueprintImportExport getExportXmlEntity(UUID id, boolean ignoreAuthorize) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));

        if (!ignoreAuthorize) this.authorizationService.authorizeForce(Permission.ExportPlanBlueprint);
        PlanBlueprintEntity data = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.AllExceptPublic).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        return this.definitionXmlToExport(data);
    }


    @Override
    public ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, TransformerException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));
        PlanBlueprintEntity data = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.AllExceptPublic).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String xml = this.xmlHandlingService.toXml(this.getExportXmlEntity(id, false));
        this.accountingService.increase(UsageLimitTargetMetric.EXPORT_BLUEPRINT_XML_EXECUTION_COUNT.getValue());
        return this.responseUtilsService.buildResponseFileFromText(xml, data.getLabel() + ".xml");
    }

    private BlueprintImportExport definitionXmlToExport(PlanBlueprintEntity entity) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        if (entity == null)
            return null;
        BlueprintImportExport xml = new BlueprintImportExport();
        xml.setId(entity.getId());
        xml.setLabel(entity.getLabel());
        xml.setDescription(entity.getDescription());
        xml.setCode(entity.getCode());
        xml.setPlanBlueprintType(this.planBlueprintTypeXmlToExport(entity.getTypeId()));
        xml.setGroupId(entity.getGroupId());
        DefinitionEntity planBlueprintDefinition = this.xmlHandlingService.fromXml(DefinitionEntity.class, entity.getDefinition());
        xml.setPlanBlueprintDefinition(this.definitionXmlToExport(planBlueprintDefinition));
        return xml;
    }

    private PlanBlueprintTypeImportExport planBlueprintTypeXmlToExport(UUID typeId) throws InvalidApplicationException {
        PlanBlueprintTypeImportExport xml = new PlanBlueprintTypeImportExport();

        if (typeId == null) return xml;

        PlanBlueprintTypeEntity data = this.tenantEntityManagerFactory.getInstance().find(PlanBlueprintTypeEntity.class, typeId);
        if (data == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{typeId, PlanBlueprintType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        xml.setId(typeId);
        xml.setName(data.getName());
        xml.setCode(data.getCode());

        return xml;
    }

    private BlueprintDefinitionImportExport definitionXmlToExport(DefinitionEntity entity) {
        if (entity == null)
            return null;
        BlueprintDefinitionImportExport xml = new BlueprintDefinitionImportExport();
        List<BlueprintSectionImportExport> planBlueprintSections = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getSections())) {
            for (RootSectionEntity section : entity.getSections()) {
                planBlueprintSections.add(this.sectionXmlToExport(section));
            }
        }
        xml.setSections(planBlueprintSections);

        List<PluginConfigurationImportExport> pluginConfigurations = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getPluginConfigurations())) {
            for (PluginConfigurationEntity pluginConfiguration : entity.getPluginConfigurations()) {
                pluginConfigurations.add(this.pluginConfigurationService.pluginConfigurationXmlToExport(pluginConfiguration));
            }
            xml.setPluginConfigurations(pluginConfigurations);
        }

        if (entity.getPlanCreationConsent() != null) {
            xml.setPlanCreationConsent(this.planCreationConsentToExport(entity.getPlanCreationConsent()));
        }

        if (entity.getIconConfiguration() != null) {
            xml.setIconConfiguration(this.iconConfigurationService.iconConfigurationXmlToExport(entity.getIconConfiguration()));
        }


        return xml;
    }

    private BlueprintSectionImportExport sectionXmlToExport(RootSectionEntity entity) {

        BlueprintSectionImportExport xml = new BlueprintSectionImportExport();
        DescriptionTemplateSectionImportExport baseSectionXml = this.fieldsService.sectionXmlToExport(entity);

        xml.setId(baseSectionXml.getId());
        xml.setOrdinal(baseSectionXml.getOrdinal());
        xml.setFieldSets(baseSectionXml.getFieldSets());
        xml.setDescription(baseSectionXml.getDescription());
        xml.setTitle(baseSectionXml.getTitle());
        xml.setSections(baseSectionXml.getSections());
        xml.setSemantics(baseSectionXml.getSemantics());

        xml.setHasTemplates(entity.getHasTemplates());
        xml.setPrefillingSourcesEnabled(entity.getPrefillingSourcesEnabled());
        xml.setCanEditDescriptionTemplates(entity.getCanEditDescriptionTemplates());

        List<BlueprintDescriptionTemplateImportExport> planBlueprintDescriptionTemplates = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getDescriptionTemplates())) {
            List<org.opencdmp.data.DescriptionTemplateEntity> templatesWithCode = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(entity.getDescriptionTemplates().stream().map(BlueprintDescriptionTemplateEntity::getDescriptionTemplateGroupId).distinct().collect(Collectors.toList())).disableTracking().collectAs(new BaseFieldSet().ensure(org.opencdmp.data.DescriptionTemplateEntity._code).ensure(org.opencdmp.data.DescriptionTemplateEntity._groupId).ensure(DescriptionTemplateEntity._label));

            for (BlueprintDescriptionTemplateEntity descriptionTemplate : entity.getDescriptionTemplates()) {
                planBlueprintDescriptionTemplates.add(this.blueprintDescriptionTemplateXmlToExport(descriptionTemplate, templatesWithCode));
            }
        }
        xml.setDescriptionTemplates(planBlueprintDescriptionTemplates);

        List<BlueprintPrefillingSourceImportExport> blueprintPrefillingSourceImportExports = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getPrefillingSourcesIds())) {
            for (UUID prefillingSourceIda : entity.getPrefillingSourcesIds()) {
                blueprintPrefillingSourceImportExports.add(this.prefillingSourceXmlToExport(prefillingSourceIda));
            }
        }
        xml.setPrefillingSources(blueprintPrefillingSourceImportExports);
        return xml;
    }

    private BlueprintDescriptionTemplateImportExport blueprintDescriptionTemplateXmlToExport(BlueprintDescriptionTemplateEntity entity, List<org.opencdmp.data.DescriptionTemplateEntity> templatesWithCode) {
        BlueprintDescriptionTemplateImportExport xml = new BlueprintDescriptionTemplateImportExport();

        if (entity == null) return xml;

        if (!this.conventionService.isListNullOrEmpty(templatesWithCode)) {
            org.opencdmp.data.DescriptionTemplateEntity code = templatesWithCode.stream().filter(x -> x.getGroupId().equals(entity.getDescriptionTemplateGroupId())).findFirst().orElse(null);
            if (code != null){
                xml.setDescriptionTemplateCode(code.getCode());
                xml.setLabel(code.getLabel());}
        }
        xml.setDescriptionTemplateGroupId(entity.getDescriptionTemplateGroupId());
        if (entity.getMinMultiplicity() != null ) xml.setMinMultiplicity(entity.getMinMultiplicity());
        if (entity.getMaxMultiplicity() != null ) xml.setMaxMultiplicity(entity.getMaxMultiplicity());
        return xml;
    }

    private BlueprintPrefillingSourceImportExport prefillingSourceXmlToExport(UUID id) {
        BlueprintPrefillingSourceImportExport xml = new BlueprintPrefillingSourceImportExport();
        PrefillingSourceEntity data = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().ids(id).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._code));
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PrefillingSource.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        xml.setId(id);
        xml.setCode(data.getCode());
        return xml;
    }

    private BlueprintPlanCreationConsentImportExport planCreationConsentToExport(PlanCreationConsentEntity entity) {
        BlueprintPlanCreationConsentImportExport xml = new BlueprintPlanCreationConsentImportExport();
        xml.setEnabled(entity.getEnabled());
        xml.setMessage(entity.getMessage());
        xml.setDetails(entity.getDetails());

        return xml;
    }
    //endregion

    //region Import

    @Override
    public PlanBlueprint importXml(BlueprintImportExport planBlueprintDefinition, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("planBlueprintDefinition", planBlueprintDefinition).And("label", label).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ImportPlanBlueprint);

        if (groupId == null) groupId = planBlueprintDefinition.getGroupId();

        long activeBlueprintForTheGroup = groupId != null ? this.queryFactory.query(PlanBlueprintQuery.class).disableTracking()
                .isActive(IsActive.Active)
                .groupIds(groupId)
                .count() : 0;

        if (activeBlueprintForTheGroup == 0 && !this.conventionService.isNullOrEmpty(planBlueprintDefinition.getCode())) {
            this.queryFactory.query(PlanBlueprintQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .codes(planBlueprintDefinition.getCode())
                    .count();
        }

        if (activeBlueprintForTheGroup == 0) {
            PlanBlueprintPersist persist = new PlanBlueprintPersist();

            persist.setLabel(label);
            persist.setDescription(planBlueprintDefinition.getDescription());
            persist.setCode(planBlueprintDefinition.getCode());
            persist.setStatus(PlanBlueprintStatus.Draft);
            persist.setType(this.xmlPlanBlueprintTypeToPersist(planBlueprintDefinition.getPlanBlueprintType(), label));
            persist.setDefinition(this.xmlDefinitionToPersist(planBlueprintDefinition.getPlanBlueprintDefinition()));

            this.validatorFactory.validator(PlanBlueprintPersist.PlanBlueprintPersistValidator.class).validateForce(persist);
            return this.persist(persist, groupId, fields);
        } else {
            PlanBlueprintEntity latestVersionPlanBlueprint = null;
            if (groupId != null) {
                latestVersionPlanBlueprint = this.queryFactory.query(PlanBlueprintQuery.class)
                        .disableTracking()
                        .versionStatuses(PlanBlueprintVersionStatus.Current)
                        .isActive(IsActive.Active)
                        .statuses(PlanBlueprintStatus.Finalized)
                        .groupIds(groupId)
                        .first();
            }
            if (latestVersionPlanBlueprint == null && !this.conventionService.isNullOrEmpty(planBlueprintDefinition.getCode())) {
                latestVersionPlanBlueprint = this.queryFactory.query(PlanBlueprintQuery.class)
                        .disableTracking()
                        .versionStatuses(PlanBlueprintVersionStatus.Current)
                        .isActive(IsActive.Active)
                        .statuses(PlanBlueprintStatus.Finalized)
                        .codes(planBlueprintDefinition.getCode())
                        .first();
            }
            if (latestVersionPlanBlueprint == null) throw new MyValidationException(this.errors.getPlanIsNotFinalized().getCode(), this.errors.getPlanIsNotFinalized().getMessage());
            NewVersionPlanBlueprintPersist persist = new NewVersionPlanBlueprintPersist();
            persist.setId(latestVersionPlanBlueprint.getId());
            persist.setLabel(label);
            persist.setDescription(planBlueprintDefinition.getDescription());
            persist.setStatus(PlanBlueprintStatus.Draft);
            persist.setType(this.xmlPlanBlueprintTypeToPersist(planBlueprintDefinition.getPlanBlueprintType(), label));
            persist.setDefinition(this.xmlDefinitionToPersist(planBlueprintDefinition.getPlanBlueprintDefinition()));
            persist.setHash(this.conventionService.hashValue(latestVersionPlanBlueprint.getUpdatedAt()));

            this.validatorFactory.validator(NewVersionPlanBlueprintPersist.NewVersionPlanBlueprintPersistValidator.class).validateForce(persist);
            this.accountingService.increase(UsageLimitTargetMetric.IMPORT_BLUEPRINT_XML_EXECUTION_COUNT.getValue());
            return this.createNewVersion(persist, fields);
        }
    }

    @Override
    public PlanBlueprint importXml(byte[] bytes, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("bytes", bytes).And("label", label).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ImportPlanBlueprint);

        BlueprintImportExport planBlueprintDefinition = this.xmlHandlingService.fromXmlSafe(BlueprintImportExport.class, new String(bytes, StandardCharsets.UTF_8));

        if (planBlueprintDefinition == null) {
            logger.warn("Plan blueprint import xml failed. Input: " + new String(bytes, StandardCharsets.UTF_8));
            throw new MyApplicationException(this.errors.getInvalidPlanBlueprintImportXml().getCode(), this.errors.getInvalidPlanBlueprintImportXml().getMessage());
        }

        return this.importXml(planBlueprintDefinition, groupId, label, fields);
    }

    private UUID xmlPlanBlueprintTypeToPersist(PlanBlueprintTypeImportExport importXml, String label) {
        if (importXml == null) return null;

        PlanBlueprintTypeQuery query = null;

        if (importXml.getId() != null) {
            // search by id
            PlanBlueprintTypeEntity entity = this.queryFactory.query(PlanBlueprintTypeQuery.class).ids(importXml.getId()).firstAs(new BaseFieldSet().ensure(PlanBlueprintType._id).ensure(PlanBlueprintType._code).ensure(PlanBlueprintType._status));
            if (entity != null ) {
                if (!entity.getStatus().equals(PlanBlueprintTypeStatus.Finalized)) throw new MyValidationException(this.errors.getPlanBlueprintTypeImportDraft().getCode(), entity.getCode());
                return entity.getId();
            } else {
                if (!this.conventionService.isNullOrEmpty(importXml.getCode())){
                    // search by code
                    entity = this.queryFactory.query(PlanBlueprintTypeQuery.class).codes(importXml.getCode()).isActive(IsActive.Active).firstAs(new BaseFieldSet().ensure(PlanBlueprintType._id).ensure(PlanBlueprintType._code).ensure(PlanBlueprintType._status));
                    if (entity != null) {
                        if (!entity.getStatus().equals(PlanBlueprintTypeStatus.Finalized)) throw new MyValidationException(this.errors.getPlanBlueprintTypeImportDraft().getCode(), entity.getCode());
                        return entity.getId();
                    }
                }

            }
            throw new MyValidationException(this.errors.getPlanBlueprintTypeImportNotFound().getCode(), importXml.getCode());
        }
        return null;
    }

    private DefinitionPersist xmlDefinitionToPersist(BlueprintDefinitionImportExport importXml) throws IOException {
        if (importXml == null)
            return null;
        DefinitionPersist persist = new DefinitionPersist();
        List<RootSectionPersist> planBlueprintSections = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getSections())) {
            for (BlueprintSectionImportExport section : importXml.getSections()) {
                planBlueprintSections.add(this.xmlSectionToPersist(section));
            }
        }
        persist.setSections(planBlueprintSections);


        List<PluginConfigurationPersist> pluginConfigurations = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getPluginConfigurations())) {
            for (PluginConfigurationImportExport pluginConfiguration : importXml.getPluginConfigurations()) {
                pluginConfigurations.add(this.pluginConfigurationService.xmlPluginConfigurationToPersist(pluginConfiguration));
            }
        }
        persist.setPluginConfigurations(pluginConfigurations);

        if (importXml.getPlanCreationConsent() != null) {
            persist.setPlanCreationConsent(this.xmlPlanCreationConsentToPersist(importXml.getPlanCreationConsent()));
        }

        if (importXml.getIconConfiguration() != null) {
            persist.setIconConfiguration(this.iconConfigurationService.xmlIconConfigurationToPersist(importXml.getIconConfiguration()));
        }

        return persist;
    }

    private RootSectionPersist xmlSectionToPersist(BlueprintSectionImportExport importXml) {
        SectionPersist baseSectionPersist = this.fieldsService.xmlSectionToPersist(importXml);

        RootSectionPersist persist = new RootSectionPersist();
        persist.setId(importXml.getId());
        persist.setOrdinal(importXml.getOrdinal());
        persist.setTitle(importXml.getTitle());
        persist.setDescription(importXml.getDescription());
        persist.setSections(baseSectionPersist.getSections());
        persist.setFieldSets(baseSectionPersist.getFieldSets());
        persist.setSemantics(baseSectionPersist.getSemantics());

        persist.setHasTemplates(importXml.isHasTemplates());
        persist.setPrefillingSourcesEnabled(importXml.getPrefillingSourcesEnabled());
        persist.setCanEditDescriptionTemplates(importXml.getCanEditDescriptionTemplates());

        List<BlueprintDescriptionTemplatePersist> planBlueprintDescriptionTemplates = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getDescriptionTemplates())) {
            for (BlueprintDescriptionTemplateImportExport descriptionTemplate : importXml.getDescriptionTemplates()) {
                planBlueprintDescriptionTemplates.add(this.xmlDescriptionTemplateToPersist(descriptionTemplate));
            }
        }
        persist.setDescriptionTemplates(planBlueprintDescriptionTemplates);

        List<UUID> prefillingSources = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getPrefillingSources())) {
            for (BlueprintPrefillingSourceImportExport prefillingSource : importXml.getPrefillingSources()) {
                prefillingSources.add(this.xmlPrefillingSourceToPersist(prefillingSource));
            }
        }
        persist.setPrefillingSourcesIds(prefillingSources);
        return persist;
    }

    private BlueprintDescriptionTemplatePersist xmlDescriptionTemplateToPersist(BlueprintDescriptionTemplateImportExport importXml) {
        BlueprintDescriptionTemplatePersist persist = new BlueprintDescriptionTemplatePersist();

        org.opencdmp.data.DescriptionTemplateEntity data = importXml.getDescriptionTemplateGroupId() != null ? this.queryFactory.query(DescriptionTemplateQuery.class).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).disableTracking().groupIds(importXml.getDescriptionTemplateGroupId()).disableTracking().firstAs(new BaseFieldSet().ensure(DescriptionTemplate._code).ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._status)) : null;
        if (data != null ) {
            if (!data.getStatus().equals(DescriptionTemplateStatus.Finalized)) throw new MyValidationException(this.errors.getBlueprintDescriptionTemplateImportDraft().getCode(), data.getCode());
            persist.setDescriptionTemplateGroupId(importXml.getDescriptionTemplateGroupId());
        } else {
            if (!this.conventionService.isNullOrEmpty(importXml.getDescriptionTemplateCode())) {
                data = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().codes(importXml.getDescriptionTemplateCode()).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).disableTracking().firstAs(new BaseFieldSet().ensure(DescriptionTemplate._code).ensure(DescriptionTemplate._groupId).ensure(DescriptionTemplate._status));
                if (data != null) {
                    if (!data.getStatus().equals(DescriptionTemplateStatus.Finalized)) throw new MyValidationException(this.errors.getBlueprintDescriptionTemplateImportDraft().getCode(), data.getCode());
                    persist.setDescriptionTemplateGroupId(data.getGroupId());
                }
            }
        }

        if (data == null) throw new MyValidationException(this.errors.getDescriptionTemplateImportNotFound().getCode(), importXml.getDescriptionTemplateCode());

        persist.setMinMultiplicity(importXml.getMinMultiplicity());
        persist.setMaxMultiplicity(importXml.getMaxMultiplicity());
        return persist;
    }

    private UUID xmlPrefillingSourceToPersist(BlueprintPrefillingSourceImportExport importXml) {
        PrefillingSourceEntity data = importXml.getId() != null ? this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().ids(importXml.getId()).disableTracking().firstAs(new BaseFieldSet().ensure(PrefillingSource._id)) : null;
        if (data == null) {
            if (!this.conventionService.isNullOrEmpty(importXml.getCode())) data = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().codes(importXml.getCode()).disableTracking().firstAs(new BaseFieldSet().ensure(PrefillingSource._id));
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{importXml.getId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

        return data.getId();
    }

    private PlanCreationConsentPersist xmlPlanCreationConsentToPersist(BlueprintPlanCreationConsentImportExport importXml) {
        PlanCreationConsentPersist persist = new PlanCreationConsentPersist();
        persist.setEnabled(importXml.getEnabled());
        persist.setMessage(importXml.getMessage());
        persist.setDetails(importXml.getDetails());

        return persist;
    }
    //endregion

    //region Import Common Model 

    @Override
    public PlanBlueprint importCommonModel(PlanBlueprintModel planBlueprintModel, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("planBlueprintModel", planBlueprintModel).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ImportPlanBlueprint);

        long activeBlueprintForTheGroup = planBlueprintModel.getGroupId() != null ? this.queryFactory.query(PlanBlueprintQuery.class).disableTracking()
                .isActive(IsActive.Active)
                .groupIds(planBlueprintModel.getGroupId())
                .count() : 0;

        if (activeBlueprintForTheGroup == 0) {
            PlanBlueprintPersist persist = new PlanBlueprintPersist();

            persist.setLabel(planBlueprintModel.getLabel());
            persist.setStatus(PlanBlueprintStatus.Draft);
            persist.setDefinition(this.commonModelDefinitionToPersist(planBlueprintModel.getDefinition()));

            this.validatorFactory.validator(PlanBlueprintPersist.PlanBlueprintPersistValidator.class).validateForce(persist);
            return this.persist(persist, planBlueprintModel.getGroupId(), fields);
        } else {
            PlanBlueprintEntity latestVersionPlanBlueprint = this.queryFactory.query(PlanBlueprintQuery.class)
                    .disableTracking()
                    .versionStatuses(PlanBlueprintVersionStatus.Current)
                    .isActive(IsActive.Active)
                    .statuses(PlanBlueprintStatus.Finalized)
                    .groupIds(planBlueprintModel.getGroupId())
                    .first();
            if (latestVersionPlanBlueprint == null) throw new MyValidationException(this.errors.getPlanIsNotFinalized().getCode(), this.errors.getPlanIsNotFinalized().getMessage());
            NewVersionPlanBlueprintPersist persist = new NewVersionPlanBlueprintPersist();
            persist.setId(latestVersionPlanBlueprint.getId());
            persist.setLabel(planBlueprintModel.getLabel());
            persist.setStatus(PlanBlueprintStatus.Draft);
            persist.setDefinition(this.commonModelDefinitionToPersist(planBlueprintModel.getDefinition()));
            persist.setHash(this.conventionService.hashValue(latestVersionPlanBlueprint.getUpdatedAt()));

            this.validatorFactory.validator(NewVersionPlanBlueprintPersist.NewVersionPlanBlueprintPersistValidator.class).validateForce(persist);
            return this.createNewVersion(persist, fields);
        }
    }

    private DefinitionPersist commonModelDefinitionToPersist(DefinitionModel commonModel) {
        if (commonModel == null)
            return null;
        DefinitionPersist persist = new DefinitionPersist();
        List<RootSectionPersist> planBlueprintSections = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            for (RootSectionModel section : commonModel.getSections()) {
                planBlueprintSections.add(this.commonModelSectionToPersist(section));
            }
        }
        persist.setSections(planBlueprintSections);
        return persist;
    }

    private RootSectionPersist commonModelSectionToPersist(RootSectionModel commonModel) {
        RootSectionPersist persist = new RootSectionPersist();

        persist.setId(commonModel.getId());
        persist.setOrdinal(commonModel.getOrdinal());
        persist.setTitle(commonModel.getTitle());
        persist.setDescription(commonModel.getDescription());
        persist.setSemantics(commonModel.getSemantics());
        persist.setHasTemplates(commonModel.getHasTemplates());
        persist.setCanEditDescriptionTemplates(commonModel.getCanEditDescriptionTemplates());

        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            List<SectionPersist> sectionsListEntity = new LinkedList<>();
            for (SectionModel sectionModel : commonModel.getSections()) {
                sectionsListEntity.add(this.fieldsService.commonModelSectionToPersist(sectionModel));
            }
            persist.setSections(sectionsListEntity);
        }

        if (!this.conventionService.isListNullOrEmpty(commonModel.getFieldSets())) {
            List<FieldSetPersist> fieldSetEntity = new LinkedList<>();
            for (FieldSetModel fieldSetModel : commonModel.getFieldSets()) {
                fieldSetEntity.add(this.fieldsService.commonModelToFieldSetModel(fieldSetModel));
            }
            persist.setFieldSets(fieldSetEntity);
        }

        return persist;
    }

    //endregion
}

