package org.opencdmp.service.planblueprint;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.opencdmp.commons.scope.tenant.TenantScope;
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
import org.opencdmp.model.planblueprint.Definition;
import org.opencdmp.model.planblueprint.Field;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.planblueprint.Section;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.lock.LockService;
import org.opencdmp.service.pluginconfiguration.PluginConfigurationService;
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

    private final TenantEntityManager entityManager;

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
    private final TenantScope tenantScope;

    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;

    private final LockService lockService;
    private final StorageFileService storageFileService;

    private final PluginConfigurationService pluginConfigurationService;

    @Autowired
    public PlanBlueprintServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            MessageSource messageSource, QueryFactory queryFactory,
            ResponseUtilsService responseUtilsService,
            XmlHandlingService xmlHandlingService,
            ErrorThesaurusProperties errors,
            PluginConfigurationService pluginConfigurationService,
            ValidatorFactory validatorFactory, TenantScope tenantScope, UsageLimitService usageLimitService, AccountingService accountingService, LockService lockService, StorageFileService storageFileService) {
        this.entityManager = entityManager;
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
	    this.tenantScope = tenantScope;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.lockService = lockService;
        this.storageFileService = storageFileService;
        this.pluginConfigurationService = pluginConfigurationService;
    }

    //region Persist

    public PlanBlueprint persist(PlanBlueprintPersist model, UUID groupId, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, TransformerException, ParserConfigurationException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditPlanBlueprint);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanBlueprintEntity data;
        if (isUpdate) {
            data = this.entityManager.find(PlanBlueprintEntity.class, model.getId());
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

        if (model.getDefinition() != null && model.getDefinition().getSections().stream().noneMatch(SectionPersist::getHasTemplates)) {
            throw new MyValidationException(this.errors.getPlanBlueprintHasNoDescriptionTemplates().getCode(), this.errors.getPlanBlueprintHasNoDescriptionTemplates().getMessage());
        }

        data.setLabel(model.getLabel());
        data.setCode(model.getCode());
        data.setStatus(model.getStatus());
        data.setUpdatedAt(Instant.now());

        DefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(data.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, data.getDefinition());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), oldDefinition)));
        data.setDescription(model.getDescription());

        if (isUpdate) {
            this.entityManager.merge(data);
            if (previousStatus != null && previousStatus.equals(PlanBlueprintStatus.Draft) && data.getStatus().equals(PlanBlueprintStatus.Finalized)) {
                this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());
                this.accountingService.decrease(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
            }
        } else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue());
            if (data.getStatus().equals(PlanBlueprintStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
            if (data.getStatus().equals(PlanBlueprintStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());
        }

        this.entityManager.flush();

        if (!isUpdate) {
            Long planBlueprintCodes = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .codes(model.getCode())
                    .count();

            if (planBlueprintCodes > 0) throw new MyValidationException(this.errors.getPlanBlueprintCodeExists().getCode(), this.errors.getPlanBlueprintCodeExists().getMessage());
        }

        this.updateVersionStatusAndSave(data, previousStatus, data.getStatus());

        this.entityManager.flush();

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
                this.entityManager.merge(oldPlanBlueprintEntity);
            } else {
                data.setVersion((short) 1);
            }
        }

    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist, DefinitionEntity oldValue) throws InvalidApplicationException {
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null)
            return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (SectionPersist sectionPersist : persist.getSections()) {
                data.getSections().add(this.buildSectionEntity(sectionPersist));
            }
        }

        if (!this.conventionService.isListNullOrEmpty(persist.getPluginConfigurations())) {
            data.setPluginConfigurations(new ArrayList<>());
            for (PluginConfigurationPersist pluginConfigurationPersist : persist.getPluginConfigurations()) {
                data.getPluginConfigurations().add(this.pluginConfigurationService.buildPluginConfigurationEntity(pluginConfigurationPersist, oldValue != null ? oldValue.getPluginConfigurations(): null));
            }
        }
        return data;
    }

    private @NotNull SectionEntity buildSectionEntity(SectionPersist persist) {
        SectionEntity data = new SectionEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setDescription(persist.getDescription());
        data.setLabel(persist.getLabel());
        data.setOrdinal(persist.getOrdinal());
        data.setHasTemplates(persist.getHasTemplates());
        data.setPrefillingSourcesEnabled(persist.getPrefillingSourcesEnabled());
        data.setCanEditDescriptionTemplates(persist.getCanEditDescriptionTemplates());
        data.setPrefillingSourcesIds(persist.getPrefillingSourcesIds());
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())) {
            data.setFields(new ArrayList<>());
            for (FieldPersist fieldPersist : persist.getFields()) {
                data.getFields().add(this.buildExtraFieldEntity(fieldPersist));
            }
        }

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

    private @NotNull FieldEntity buildExtraFieldEntity(FieldPersist persist) {
        if (persist == null)
            return new ExtraFieldEntity();
        FieldEntity data;
        
        switch (persist.getCategory()){
            case Extra -> {
                ExtraFieldEntity dataTyped = new ExtraFieldEntity();
                dataTyped.setType(((ExtraFieldPersist) persist).getDataType());
                data = dataTyped;
            }
            case System -> {
                SystemFieldEntity dataTyped = new SystemFieldEntity();
                dataTyped.setType(((SystemFieldPersist) persist).getSystemFieldType());
                data = dataTyped;
            }
            case ReferenceType -> {
                ReferenceTypeFieldEntity dataTyped = new ReferenceTypeFieldEntity();
                dataTyped.setReferenceTypeId(((ReferenceTypeFieldPersist) persist).getReferenceTypeId());
                dataTyped.setMultipleSelect(((ReferenceTypeFieldPersist) persist).getMultipleSelect());
                data = dataTyped;
            }
            case Upload -> data = this.buildUploadDataEntity((UploadFieldPersist) persist);
            default -> throw new  InternalError("unknown type: " + persist.getCategory());
        }
        
        data.setId(persist.getId());
        data.setCategory(persist.getCategory());
        data.setLabel(persist.getLabel());
        data.setPlaceholder(persist.getPlaceholder());
        data.setDescription(persist.getDescription());
        data.setSemantics(persist.getSemantics());
        data.setOrdinal(persist.getOrdinal());
        data.setRequired(persist.getRequired());

        return data;
    }

    private @NotNull UploadFieldEntity buildUploadDataEntity(UploadFieldPersist persist) {
        UploadFieldEntity data = new UploadFieldEntity();
        if (persist == null) return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getTypes())){
            data.setTypes(new ArrayList<>());
            for (UploadFieldPersist.UploadOptionPersist uploadOptionPersist: persist.getTypes()) {
                data.getTypes().add(this.buildOptionEntity(uploadOptionPersist));
            }
        }
        data.setMaxFileSizeInMB(persist.getMaxFileSizeInMB());
        return data;
    }

    private @NotNull UploadFieldEntity.UploadOptionEntity buildOptionEntity(UploadFieldPersist.UploadOptionPersist persist){
        UploadFieldEntity.UploadOptionEntity data = new UploadFieldEntity.UploadOptionEntity();
        if (persist == null) return data;

        data.setLabel(persist.getLabel());
        data.setValue(persist.getValue());

        return data;
    }

    //endregion

    //region Delete

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeletePlanBlueprint);

        PlanBlueprintEntity data = this.entityManager.find(PlanBlueprintEntity.class, id);
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
                this.entityManager.merge(previousPlanBlueprint);
                data.setVersionStatus(PlanBlueprintVersionStatus.NotFinalized);
            }
            this.entityManager.merge(data);
            this.entityManager.flush();
        }

        this.deleterFactory.deleter(PlanBlueprintDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    //endregion

    //region FieldInBlueprint

    public boolean fieldInBlueprint(PlanBlueprintEntity planBlueprintEntity, PlanBlueprintSystemFieldType type) {

        DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, planBlueprintEntity.getDefinition());
        if (definition == null || definition.getSections() == null)
            return false;

        for (SectionEntity section : definition.getSections()) {
            if (section.getFields() == null)
                continue;
            for (FieldEntity field : section.getFields()) {
                if (field.getCategory().equals(PlanBlueprintFieldCategory.System)) {
                    SystemFieldEntity systemField = (SystemFieldEntity) field;
                    if (systemField.getType().equals(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean fieldInBlueprint(UUID id, PlanBlueprintSystemFieldType type) throws InvalidApplicationException {
        PlanBlueprintEntity data = this.entityManager.find(PlanBlueprintEntity.class, id, true);
        if (data == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        return this.fieldInBlueprint(data, type);
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

        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                this.reassignSection(section);
            }
        }

        if (model.getPluginConfigurations() != null) {
            for (PluginConfiguration pluginConfiguration : model.getPluginConfigurations()) {
                this.pluginConfigurationService.reassignPluginConfiguration(pluginConfiguration);
            }
        }
    }



    private void reassignSection(Section model) {
        if (model == null)
            return;
        model.setId(UUID.randomUUID());

        if (model.getFields() != null) {
            for (Field field : model.getFields()) {
                this.reassignField(field);
            }
        }
    }

    private void reassignField(Field model) {
        if (model == null)
            return;
        model.setId(UUID.randomUUID());
    }

    //endregion

    //region NewVersion

    @Override
    public PlanBlueprint createNewVersion(NewVersionPlanBlueprintPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data planBlueprint").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.CreateNewVersionPlanBlueprint);
        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.BLUEPRINT_COUNT);

        PlanBlueprintEntity oldPlanBlueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, model.getId(), true);
        if (oldPlanBlueprintEntity == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(oldPlanBlueprintEntity.getUpdatedAt()).equals(model.getHash()))
            throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        if (!this.tenantScope.isSet() || !Objects.equals(oldPlanBlueprintEntity.getTenantId(), this.tenantScope.getTenant())) throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());

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
        data.setStatus(PlanBlueprintStatus.Draft);

        if (!this.conventionService.isListNullOrEmpty(model.getDefinition().getPluginConfigurations())) {
            DefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(oldPlanBlueprintEntity.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, oldPlanBlueprintEntity.getDefinition());
            if (oldDefinition != null && !this.conventionService.isListNullOrEmpty(oldDefinition.getPluginConfigurations())) {
                // reassign new storage files if equals with old
                this.pluginConfigurationService.reassignNewStorageFilesIfEqualsWithOld(model.getDefinition().getPluginConfigurations(), oldDefinition.getPluginConfigurations());
            }
        }
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), null)));

        data.setGroupId(oldPlanBlueprintEntity.getGroupId());
        data.setCode(oldPlanBlueprintEntity.getCode());
        data.setVersion((short) (oldPlanBlueprintEntity.getVersion() + 1));
        data.setVersionStatus(PlanBlueprintVersionStatus.NotFinalized);
        data.setCreatedAt(Instant.now());
        data.setUpdatedAt(Instant.now());
        data.setIsActive(IsActive.Active);

        this.entityManager.persist(data);

        this.entityManager.flush();

        this.updateVersionStatusAndSave(data, PlanBlueprintStatus.Draft, data.getStatus());

        this.entityManager.flush();

        this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_COUNT.getValue());
        if (data.getStatus().equals(PlanBlueprintStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_DRAFT_COUNT.getValue());
        if (data.getStatus().equals(PlanBlueprintStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.BLUEPRINT_FINALIZED_COUNT.getValue());

        return this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, PlanBlueprint._id), data);
    }

    //endregion

    //region Export


    @Override
    public BlueprintImportExport getExportXmlEntity(UUID id, boolean ignoreAuthorize) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException {
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

    private BlueprintImportExport definitionXmlToExport(PlanBlueprintEntity entity) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        if (entity == null)
            return null;
        BlueprintImportExport xml = new BlueprintImportExport();
        xml.setId(entity.getId());
        xml.setLabel(entity.getLabel());
        xml.setDescription(entity.getDescription());
        xml.setCode(entity.getCode());
        xml.setGroupId(entity.getGroupId());
        DefinitionEntity planBlueprintDefinition = this.xmlHandlingService.fromXml(DefinitionEntity.class, entity.getDefinition());
        xml.setPlanBlueprintDefinition(this.definitionXmlToExport(planBlueprintDefinition));
        return xml;
    }

    private BlueprintDefinitionImportExport definitionXmlToExport(DefinitionEntity entity) {
        if (entity == null)
            return null;
        BlueprintDefinitionImportExport xml = new BlueprintDefinitionImportExport();
        List<BlueprintSectionImportExport> planBlueprintSections = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getSections())) {
            for (SectionEntity section : entity.getSections()) {
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


        return xml;
    }

    private BlueprintSectionImportExport sectionXmlToExport(SectionEntity entity) {
        BlueprintSectionImportExport xml = new BlueprintSectionImportExport();
        xml.setId(entity.getId());
        xml.setLabel(entity.getLabel());
        xml.setDescription(entity.getDescription());
        xml.setOrdinal(entity.getOrdinal());
        xml.setHasTemplates(entity.getHasTemplates());
        xml.setPrefillingSourcesEnabled(entity.getPrefillingSourcesEnabled());
        xml.setCanEditDescriptionTemplates(entity.getCanEditDescriptionTemplates());

        List<BlueprintSystemFieldImportExport> planBlueprintSystemFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFields())) {
            for (SystemFieldEntity systemField : entity.getFields().stream().filter(x -> x.getCategory() == PlanBlueprintFieldCategory.System).map(x -> (SystemFieldEntity) x).toList()) {
                planBlueprintSystemFieldModels.add(this.systemFieldXmlToExport(systemField));
            }
        }
        xml.setSystemFields(planBlueprintSystemFieldModels);
        
        List<BlueprintExtraFieldImportExport> planBlueprintExtraFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFields())) {
            for (ExtraFieldEntity systemField : entity.getFields().stream().filter(x -> x.getCategory() == PlanBlueprintFieldCategory.Extra).map(x -> (ExtraFieldEntity) x).toList()) {
                planBlueprintExtraFieldModels.add(this.extraFieldXmlToExport(systemField));
            }
        }
        xml.setExtraFields(planBlueprintExtraFieldModels);

        List<BlueprintUploadFieldImportExport> planBlueprintUploadFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFields())) {
            for (UploadFieldEntity uploadField : entity.getFields().stream().filter(x -> x.getCategory() == PlanBlueprintFieldCategory.Upload).map(x -> (UploadFieldEntity) x).toList()) {
                planBlueprintUploadFieldModels.add(this.uploadFieldXmlToExport(uploadField));
            }
        }
        xml.setUploadFields(planBlueprintUploadFieldModels);

        List<BlueprintReferenceTypeFieldImportExport> planBlueprintReferenceFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFields())) {
            for (ReferenceTypeFieldEntity referenceTypeFieldEntity : entity.getFields().stream().filter(x -> x.getCategory() == PlanBlueprintFieldCategory.ReferenceType).map(x -> (ReferenceTypeFieldEntity) x).toList()) {
                planBlueprintReferenceFieldModels.add(this.referenceFieldXmlToExport(referenceTypeFieldEntity));
            }
        }
        xml.setReferenceFields(planBlueprintReferenceFieldModels);

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

    private BlueprintExtraFieldImportExport extraFieldXmlToExport(ExtraFieldEntity entity) {
        BlueprintExtraFieldImportExport xml = new BlueprintExtraFieldImportExport();
        xml.setId(entity.getId());
        xml.setType(entity.getType());
        xml.setLabel(entity.getLabel());
        xml.setPlaceholder(entity.getPlaceholder());
        xml.setDescription(entity.getDescription());
        xml.setOrdinal(entity.getOrdinal());
        xml.setRequired(entity.isRequired());
        xml.setSemantics(entity.getSemantics());
        return xml;
    }

    private BlueprintReferenceTypeFieldImportExport referenceFieldXmlToExport(ReferenceTypeFieldEntity entity) {
        BlueprintReferenceTypeFieldImportExport xml = new BlueprintReferenceTypeFieldImportExport();
        ReferenceTypeEntity data = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(entity.getReferenceTypeId()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._code));
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{entity.getReferenceTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        xml.setId(entity.getId());
        xml.setReferenceTypeId(entity.getReferenceTypeId());
        xml.setReferenceTypeCode(data.getCode());
        xml.setLabel(entity.getLabel());
        xml.setPlaceholder(entity.getPlaceholder());
        xml.setDescription(entity.getDescription());
        xml.setOrdinal(entity.getOrdinal());
        xml.setRequired(entity.isRequired());
        xml.setMultipleSelect(entity.getMultipleSelect());
        xml.setSemantics(entity.getSemantics());
        return xml;
    }

    private BlueprintSystemFieldImportExport systemFieldXmlToExport(SystemFieldEntity entity) {
        BlueprintSystemFieldImportExport xml = new BlueprintSystemFieldImportExport();
        xml.setId(entity.getId());
        xml.setType(entity.getType());
        xml.setLabel(entity.getLabel());
        xml.setPlaceholder(entity.getPlaceholder());
        xml.setDescription(entity.getDescription());
        xml.setOrdinal(entity.getOrdinal());
        xml.setRequired(entity.isRequired());
        xml.setSemantics(entity.getSemantics());
        return xml;
    }

    private BlueprintUploadFieldImportExport uploadFieldXmlToExport(UploadFieldEntity entity) {
        BlueprintUploadFieldImportExport xml = new BlueprintUploadFieldImportExport();
        xml.setId(entity.getId());
        xml.setLabel(entity.getLabel());
        xml.setPlaceholder(entity.getPlaceholder());
        xml.setDescription(entity.getDescription());
        xml.setOrdinal(entity.getOrdinal());
        xml.setRequired(entity.isRequired());
        xml.setSemantics(entity.getSemantics());
        xml.setMaxFileSizeInMB(entity.getMaxFileSizeInMB());

        List<BlueprintUploadFieldImportExport.UploadOptionImportExport> types = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getTypes())) {
            for (UploadFieldEntity.UploadOptionEntity type : entity.getTypes()) {
                types.add(this.uploadOptionXmlToExport(type));
            }
        }
        xml.setTypes(types);
        return xml;
    }

    private BlueprintUploadFieldImportExport.UploadOptionImportExport uploadOptionXmlToExport(UploadFieldEntity.UploadOptionEntity entity) {
        BlueprintUploadFieldImportExport.UploadOptionImportExport xml = new BlueprintUploadFieldImportExport.UploadOptionImportExport();
        xml.setLabel(entity.getLabel());
        xml.setValue(entity.getValue());

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

    private DefinitionPersist xmlDefinitionToPersist(BlueprintDefinitionImportExport importXml) throws IOException {
        if (importXml == null)
            return null;
        DefinitionPersist persist = new DefinitionPersist();
        List<SectionPersist> planBlueprintSections = new ArrayList<>();
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

        return persist;
    }

    private SectionPersist xmlSectionToPersist(BlueprintSectionImportExport importXml) {
        SectionPersist persist = new SectionPersist();
        persist.setId(importXml.getId());
        persist.setLabel(importXml.getLabel());
        persist.setDescription(importXml.getDescription());
        persist.setOrdinal(importXml.getOrdinal());
        persist.setHasTemplates(importXml.isHasTemplates());
        persist.setPrefillingSourcesEnabled(importXml.getPrefillingSourcesEnabled());
        persist.setCanEditDescriptionTemplates(importXml.getCanEditDescriptionTemplates());

        List<FieldPersist> planBlueprintFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getSystemFields())) {
            for (BlueprintSystemFieldImportExport systemField : importXml.getSystemFields()) {
                planBlueprintFieldModels.add(this.xmlSystemFieldToPersist(systemField));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(importXml.getReferenceFields())) {
            for (BlueprintReferenceTypeFieldImportExport referenceField : importXml.getReferenceFields()) {
                planBlueprintFieldModels.add(this.xmlReferenceFieldToPersist(referenceField));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(importXml.getExtraFields())) {
            for (BlueprintExtraFieldImportExport extraField : importXml.getExtraFields()) {
                planBlueprintFieldModels.add(this.xmlExtraFieldToPersist(extraField));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(importXml.getUploadFields())) {
            for (BlueprintUploadFieldImportExport uploadField : importXml.getUploadFields()) {
                planBlueprintFieldModels.add(this.xmlUploadFieldToPersist(uploadField));
            }
        }
        persist.setFields(planBlueprintFieldModels);
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

    private FieldPersist xmlExtraFieldToPersist(BlueprintExtraFieldImportExport importXml) {
        ExtraFieldPersist persist = new ExtraFieldPersist();
        persist.setId(importXml.getId());
        persist.setCategory(PlanBlueprintFieldCategory.Extra);
        persist.setDataType(importXml.getType());
        persist.setLabel(importXml.getLabel());
        persist.setPlaceholder(importXml.getPlaceholder());
        persist.setDescription(importXml.getDescription());
        persist.setOrdinal(importXml.getOrdinal());
        persist.setRequired(importXml.isRequired());
        persist.setSemantics(importXml.getSemantics());
        return persist;
    }

    private FieldPersist xmlSystemFieldToPersist(BlueprintSystemFieldImportExport importXml) {
        SystemFieldPersist persist = new SystemFieldPersist();
        persist.setId(importXml.getId());
        persist.setCategory(PlanBlueprintFieldCategory.System);
        persist.setSystemFieldType(importXml.getType());
        persist.setLabel(importXml.getLabel());
        persist.setPlaceholder(importXml.getPlaceholder());
        persist.setDescription(importXml.getDescription());
        persist.setOrdinal(importXml.getOrdinal());
        persist.setRequired(importXml.isRequired());
        persist.setSemantics(importXml.getSemantics());
        return persist;
    }

    private FieldPersist xmlReferenceFieldToPersist(BlueprintReferenceTypeFieldImportExport importXml) {

        ReferenceTypeEntity data = importXml.getReferenceTypeId() != null ? this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(importXml.getReferenceTypeId()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id)) : null;
        if (data == null){
            if (!this.conventionService.isNullOrEmpty(importXml.getReferenceTypeCode())) data = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().codes(importXml.getReferenceTypeCode()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id));
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{importXml.getReferenceTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

        ReferenceTypeFieldPersist persist = new ReferenceTypeFieldPersist();
        persist.setId(importXml.getId());
        persist.setCategory(PlanBlueprintFieldCategory.ReferenceType);
        persist.setReferenceTypeId(data.getId());
        persist.setLabel(importXml.getLabel());
        persist.setPlaceholder(importXml.getPlaceholder());
        persist.setDescription(importXml.getDescription());
        persist.setOrdinal(importXml.getOrdinal());
        persist.setRequired(importXml.isRequired());
        persist.setMultipleSelect(importXml.getMultipleSelect());
        persist.setSemantics(importXml.getSemantics());
        return persist;
    }

    private UploadFieldPersist xmlUploadFieldToPersist(BlueprintUploadFieldImportExport importXml) {
        UploadFieldPersist persist = new UploadFieldPersist();
        persist.setId(importXml.getId());
        persist.setCategory(PlanBlueprintFieldCategory.Upload);
        persist.setLabel(importXml.getLabel());
        persist.setPlaceholder(importXml.getPlaceholder());
        persist.setDescription(importXml.getDescription());
        persist.setOrdinal(importXml.getOrdinal());
        persist.setRequired(importXml.isRequired());
        persist.setSemantics(importXml.getSemantics());
        persist.setMaxFileSizeInMB(importXml.getMaxFileSizeInMB());

        List<UploadFieldPersist.UploadOptionPersist> types = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importXml.getTypes())) {
            for (BlueprintUploadFieldImportExport.UploadOptionImportExport type : importXml.getTypes()) {
                types.add(this.xmlUploadOptionToPersist(type));
            }
        }
        persist.setTypes(types);
        return persist;
    }

    private UploadFieldPersist.UploadOptionPersist xmlUploadOptionToPersist(BlueprintUploadFieldImportExport.UploadOptionImportExport importXml) {
        UploadFieldPersist.UploadOptionPersist persist = new UploadFieldPersist.UploadOptionPersist();
        persist.setLabel(importXml.getLabel());
        persist.setValue(importXml.getValue());

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
        List<SectionPersist> planBlueprintSections = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            for (SectionModel section : commonModel.getSections()) {
                planBlueprintSections.add(this.commonModelSectionToPersist(section));
            }
        }
        persist.setSections(planBlueprintSections);
        return persist;
    }

    private SectionPersist commonModelSectionToPersist(SectionModel commonModel) {
        SectionPersist persist = new SectionPersist();
        persist.setId(commonModel.getId());
        persist.setLabel(commonModel.getLabel());
        persist.setDescription(commonModel.getDescription());
        persist.setOrdinal(commonModel.getOrdinal());
        persist.setHasTemplates(commonModel.getHasTemplates());
        List<FieldPersist> planBlueprintFieldModels = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getFields())) {
            for (SystemFieldModel systemField : commonModel.getFields().stream().filter(x-> org.opencdmp.commonmodels.enums.PlanBlueprintFieldCategory.System.equals(x.getCategory())).map(x-> (SystemFieldModel)x).toList()) {
                planBlueprintFieldModels.add(this.commonModelSystemFieldToPersist(systemField));
            }
            for (ReferenceTypeFieldModel referenceField : commonModel.getFields().stream().filter(x-> org.opencdmp.commonmodels.enums.PlanBlueprintFieldCategory.ReferenceType.equals(x.getCategory())).map(x-> (ReferenceTypeFieldModel)x).toList()) {
                planBlueprintFieldModels.add(this.commonModelReferenceFieldToPersist(referenceField));
            }
            for (ExtraFieldModel extraField : commonModel.getFields().stream().filter(x-> org.opencdmp.commonmodels.enums.PlanBlueprintFieldCategory.Extra.equals(x.getCategory())).map(x-> (ExtraFieldModel)x).toList()) {
                planBlueprintFieldModels.add(this.commonExtraFieldToPersist(extraField));
            }
        }
        persist.setFields(planBlueprintFieldModels);
//        List<DescriptionTemplatePersist> dmpBlueprintDescriptionTemplates = new LinkedList<>();
//        if (!this.conventionService.isListNullOrEmpty(importXml.getDescriptionTemplates())) {
//            for (BlueprintDescriptionTemplateImportExport descriptionTemplate : importXml.getDescriptionTemplates()) {
//                dmpBlueprintDescriptionTemplates.add(this.xmlDescriptionTemplateToPersist(descriptionTemplate));
//            }
//        }
//        persist.setDescriptionTemplates(dmpBlueprintDescriptionTemplates);
//
//        List<UUID> prefillingSources = new LinkedList<>();
//        if (!this.conventionService.isListNullOrEmpty(importXml.getPrefillingSources())) {
//            for (BlueprintPrefillingSourceImportExport prefillingSource : importXml.getPrefillingSources()) {
//                prefillingSources.add(this.xmlPrefillingSourceToPersist(prefillingSource));
//            }
//        }
//        persist.setPrefillingSourcesIds(prefillingSources);
        return persist;
    }

//    private DescriptionTemplatePersist xmlDescriptionTemplateToPersist(BlueprintDescriptionTemplateImportExport importXml) {
//        org.opencdmp.data.DescriptionTemplateEntity data = importXml.getDescriptionTemplateGroupId() != null ? this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(importXml.getDescriptionTemplateGroupId()).disableTracking().firstAs(new BaseFieldSet().ensure(DescriptionTemplate._groupId)) : null;
//        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{importXml.getDescriptionTemplateGroupId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
//
//        DescriptionTemplatePersist persist = new DescriptionTemplatePersist();
//        persist.setDescriptionTemplateGroupId(importXml.getDescriptionTemplateGroupId());
//        persist.setLabel(importXml.getLabel());
//        persist.setMinMultiplicity(importXml.getMinMultiplicity());
//        persist.setMaxMultiplicity(importXml.getMaxMultiplicity());
//        return persist;
//    }
//
//    private UUID xmlPrefillingSourceToPersist(BlueprintPrefillingSourceImportExport importXml) {
//        org.opencdmp.data.PrefillingSourceEntity data = importXml.getId() != null ? this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().ids(importXml.getId()).disableTracking().firstAs(new BaseFieldSet().ensure(PrefillingSource._id)) : null;
//        if (data == null) {
//            if (!this.conventionService.isNullOrEmpty(importXml.getCode())) data = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().codes(importXml.getCode()).disableTracking().firstAs(new BaseFieldSet().ensure(PrefillingSource._id));
//            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{importXml.getId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
//        }
//
//        return data.getId();
//    }

    private FieldPersist commonExtraFieldToPersist(ExtraFieldModel commonModel) {
        ExtraFieldPersist persist = new ExtraFieldPersist();
        persist.setId(commonModel.getId());
        persist.setCategory(PlanBlueprintFieldCategory.Extra);
        switch (commonModel.getDataType()){
            case Text -> persist.setDataType(PlanBlueprintExtraFieldDataType.Text);
            case Date -> persist.setDataType(PlanBlueprintExtraFieldDataType.Date);
            case Number -> persist.setDataType(PlanBlueprintExtraFieldDataType.Number);
            case RichTex -> persist.setDataType(PlanBlueprintExtraFieldDataType.RichTex);
            default -> throw new  InternalError("unknown type: " + commonModel.getDataType());
        }
        persist.setLabel(commonModel.getLabel());
        persist.setPlaceholder(commonModel.getPlaceholder());
        persist.setDescription(commonModel.getDescription());
        persist.setOrdinal(commonModel.getOrdinal());
        persist.setRequired(commonModel.getRequired());
        persist.setSemantics(commonModel.getSemantics());
        return persist;
    }

    private FieldPersist commonModelSystemFieldToPersist(SystemFieldModel commonModel) {
        SystemFieldPersist persist = new SystemFieldPersist();
        persist.setId(commonModel.getId());
        persist.setCategory(PlanBlueprintFieldCategory.System);
        switch (commonModel.getSystemFieldType()){
            case User -> persist.setSystemFieldType(PlanBlueprintSystemFieldType.User);
            case AccessRights -> persist.setSystemFieldType(PlanBlueprintSystemFieldType.AccessRights);
            case Contact -> persist.setSystemFieldType(PlanBlueprintSystemFieldType.Contact);
            case Description -> persist.setSystemFieldType(PlanBlueprintSystemFieldType.Description);
            case Language -> persist.setSystemFieldType(PlanBlueprintSystemFieldType.Language);
            case Title -> persist.setSystemFieldType(PlanBlueprintSystemFieldType.Title);
            default -> throw new  InternalError("unknown type: " + commonModel.getSystemFieldType());
        }
        persist.setLabel(commonModel.getLabel());
        persist.setPlaceholder(commonModel.getPlaceholder());
        persist.setDescription(commonModel.getDescription());
        persist.setOrdinal(commonModel.getOrdinal());
        persist.setRequired(commonModel.getRequired());
        persist.setSemantics(commonModel.getSemantics());
        return persist;
    }

    private FieldPersist commonModelReferenceFieldToPersist(ReferenceTypeFieldModel commonModel) {

        ReferenceTypeEntity data = commonModel.getReferenceType() != null && commonModel.getReferenceType().getId() != null ? this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(commonModel.getReferenceType().getId()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id)) : null;
        if (data == null){
            if (commonModel.getReferenceType() != null && !this.conventionService.isNullOrEmpty(commonModel.getReferenceType().getCode())) data = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().codes(commonModel.getReferenceType().getCode()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id));
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{commonModel.getReferenceType().getCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

        ReferenceTypeFieldPersist persist = new ReferenceTypeFieldPersist();
        persist.setId(commonModel.getId());
        persist.setCategory(PlanBlueprintFieldCategory.ReferenceType);
        persist.setReferenceTypeId(data.getId());
        persist.setLabel(commonModel.getLabel());
        persist.setPlaceholder(commonModel.getPlaceholder());
        persist.setDescription(commonModel.getDescription());
        persist.setOrdinal(commonModel.getOrdinal());
        persist.setRequired(commonModel.getRequired());
        persist.setMultipleSelect(commonModel.getMultipleSelect());
        persist.setSemantics(commonModel.getSemantics());
        return persist;
    }

    //endregion
}

