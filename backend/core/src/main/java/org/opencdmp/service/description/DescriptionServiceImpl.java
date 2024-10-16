package org.opencdmp.service.description;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.*;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commonmodels.models.description.*;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commonmodels.models.reference.ReferenceModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.description.*;
import org.opencdmp.commons.types.description.importexport.*;
import org.opencdmp.commons.types.descriptionreference.DescriptionReferenceDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldSetEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.ReferenceTypeDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.UploadDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateFieldImportExport;
import org.opencdmp.commons.types.descriptiontemplate.importexport.DescriptionTemplateImportExport;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.ReferenceTypeDataImportExport;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.commons.types.planblueprint.SectionEntity;
import org.opencdmp.commons.types.reference.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.DescriptionTouchedEvent;
import org.opencdmp.event.EventBroker;
import org.opencdmp.integrationevent.outbox.annotationentityremoval.AnnotationEntityRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.annotationentitytouch.AnnotationEntityTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.*;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.deleter.DescriptionDeleter;
import org.opencdmp.model.deleter.DescriptionReferenceDeleter;
import org.opencdmp.model.deleter.DescriptionTagDeleter;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.file.FileEnvelope;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.persist.descriptionproperties.*;
import org.opencdmp.model.persist.descriptionreference.DescriptionReferenceDataPersist;
import org.opencdmp.model.persist.referencedefinition.DefinitionPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.descriptiontemplate.DescriptionTemplateService;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.responseutils.ResponseUtilsService;
import org.opencdmp.service.storage.StorageFileProperties;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.tag.TagService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.opencdmp.service.visibility.VisibilityService;
import org.opencdmp.service.visibility.VisibilityServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@Service
public class DescriptionServiceImpl implements DescriptionService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final ErrorThesaurusProperties errors;
    private final MessageSource messageSource;
    private final EventBroker eventBroker;
    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final UserScope userScope;
    private final XmlHandlingService xmlHandlingService;
    private final FileTransformerService fileTransformerService;
    private final NotifyIntegrationEventHandler eventHandler;
    private final NotificationProperties notificationProperties;
    private final ElasticService elasticService;
    private final ValidatorFactory validatorFactory;
    private final StorageFileProperties storageFileConfig;
    private final StorageFileService storageFileService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler;
    private final AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler;
    private final TenantScope tenantScope;
    private final ResponseUtilsService responseUtilsService;
    private final DescriptionTemplateService descriptionTemplateService;
    private final TagService tagService;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    
    @Autowired
    public DescriptionServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource,
            EventBroker eventBroker,
            QueryFactory queryFactory,
            JsonHandlingService jsonHandlingService,
            UserScope userScope,
            XmlHandlingService xmlHandlingService, NotifyIntegrationEventHandler eventHandler, NotificationProperties notificationProperties, FileTransformerService fileTransformerService, ElasticService elasticService, ValidatorFactory validatorFactory, StorageFileProperties storageFileConfig, StorageFileService storageFileService, AuthorizationContentResolver authorizationContentResolver, AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler, AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler, TenantScope tenantScope, ResponseUtilsService responseUtilsService, DescriptionTemplateService descriptionTemplateService, TagService tagService, UsageLimitService usageLimitService, AccountingService accountingService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
        this.eventBroker = eventBroker;
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.userScope = userScope;
        this.xmlHandlingService = xmlHandlingService;
        this.eventHandler = eventHandler;
        this.notificationProperties = notificationProperties;
        this.fileTransformerService = fileTransformerService;
	    this.elasticService = elasticService;
	    this.validatorFactory = validatorFactory;
	    this.storageFileConfig = storageFileConfig;
	    this.storageFileService = storageFileService;
	    this.authorizationContentResolver = authorizationContentResolver;
	    this.annotationEntityTouchedIntegrationEventHandler = annotationEntityTouchedIntegrationEventHandler;
	    this.annotationEntityRemovalIntegrationEventHandler = annotationEntityRemovalIntegrationEventHandler;
	    this.tenantScope = tenantScope;
	    this.responseUtilsService = responseUtilsService;
	    this.descriptionTemplateService = descriptionTemplateService;
	    this.tagService = tagService;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
    }

    @Override
    public Map<UUID, List<String>> getDescriptionSectionPermissions(DescriptionSectionPermissionResolver model)  {
        logger.debug(new MapLogEntry("get description section permissions").And("model", model));

        Map<UUID, List<String>> response = new HashMap<>();
        
        Map<UUID, AffiliatedResource> affiliatedResourceMap = this.authorizationContentResolver.descriptionsAffiliationBySections(model.getPlanId(), model.getSectionIds());
        for (UUID sectionId : model.getSectionIds().stream().distinct().toList()){
            AffiliatedResource affiliatedResource = affiliatedResourceMap.getOrDefault(sectionId, null);
            response.put(sectionId, new ArrayList<>());
            for (String permission : model.getPermissions()){
                if (this.authorizationService.authorizeAtLeastOne(List.of(affiliatedResource), permission)) response.get(sectionId).add(permission);
            }
        }
        
        return response;
    }

    //region Persist
    
    @Override
    public Description persist(DescriptionPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("persisting data description").And("model", model).And("fields", fields));

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        PlanDescriptionTemplateEntity planDescriptionTemplate = this.entityManager.find(PlanDescriptionTemplateEntity.class, model.getPlanDescriptionTemplateId());
        if (planDescriptionTemplate == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPlanDescriptionTemplateId(), PlanDescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (isUpdate) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(model.getId())), Permission.EditDescription);
        else this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionsAffiliationBySection(model.getPlanId(), planDescriptionTemplate.getSectionId())), Permission.EditDescription);

        DescriptionEntity data;
        if (isUpdate) {
            data = this.entityManager.find(DescriptionEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            if (data.getStatus().equals(DescriptionStatus.Finalized)) throw new MyValidationException(this.errors.getDescriptionIsFinalized().getCode(), this.errors.getDescriptionIsFinalized().getMessage());
            if (!data.getPlanId().equals(model.getPlanId())) throw new MyValidationException(this.errors.getPlanCanNotChange().getCode(), this.errors.getPlanCanNotChange().getMessage());
            if (!data.getPlanDescriptionTemplateId().equals(model.getPlanDescriptionTemplateId())) throw new MyValidationException(this.errors.getPlanDescriptionTemplateCanNotChange().getCode(), this.errors.getPlanDescriptionTemplateCanNotChange().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_COUNT);

            PlanEntity planEntity = this.entityManager.find(PlanEntity.class, model.getPlanId(), true);
            if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getPlanId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            PlanBlueprintEntity planBlueprintEntity = this.entityManager.find(PlanBlueprintEntity.class, planEntity.getBlueprintId());
            if (planBlueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planEntity.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            if (!this.isDescriptionTemplateMaxMultiplicityValid(planBlueprintEntity, model.getPlanId(),model.getPlanDescriptionTemplateId(), false)) throw new MyValidationException(this.errors.getMaxDescriptionsExceeded().getCode(), this.errors.getMaxDescriptionsExceeded().getMessage());

            data = new DescriptionEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            data.setCreatedById(this.userScope.getUserId());
            data.setPlanId(model.getPlanId());
            data.setPlanDescriptionTemplateId(model.getPlanDescriptionTemplateId());
        }

        DescriptionTemplateEntity descriptionTemplateEntity = this.entityManager.find(DescriptionTemplateEntity.class, model.getDescriptionTemplateId(), true);
        if (descriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!planDescriptionTemplate.getDescriptionTemplateGroupId().equals(descriptionTemplateEntity.getGroupId())) throw new MyValidationException(this.errors.getInvalidDescriptionTemplate().getCode(), this.errors.getInvalidDescriptionTemplate().getMessage());

        PlanEntity plan = this.entityManager.find(PlanEntity.class, data.getPlanId(), true);
        if (plan == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getPlanId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (plan.getStatus().equals(PlanStatus.Finalized)) throw new MyValidationException(this.errors.getPlanIsFinalized().getCode(), this.errors.getPlanIsFinalized().getMessage());

        data.setLabel(model.getLabel());
        data.setStatus(model.getStatus());
        if (model.getStatus() == DescriptionStatus.Finalized) data.setFinalizedAt(Instant.now());
        data.setDescription(model.getDescription());
        data.setDescriptionTemplateId(model.getDescriptionTemplateId());
        data.setUpdatedAt(Instant.now());
        if (isUpdate) this.entityManager.merge(data);
        else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_COUNT.getValue());
        }

        this.entityManager.flush();

        org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, descriptionTemplateEntity.getDefinition());
        VisibilityService visibilityService = new VisibilityServiceImpl(definition, model.getProperties());
        Map<String, List<UUID>> fieldToReferenceMap = this.patchAndSaveReferences(this.buildDescriptionReferencePersists(visibilityService, model.getProperties()), data.getId(), definition);

        this.entityManager.flush();
        
        data.setProperties(this.jsonHandlingService.toJson(this.buildPropertyDefinitionEntity(visibilityService, model.getProperties(), definition, fieldToReferenceMap)));

        this.entityManager.merge(data);

        this.entityManager.flush();
        
        this.persistTags(data.getId(), model.getTags());
        
        this.sendNotification(data, isUpdate);

        //this.deleteOldFilesAndAddNew(datasetWizardModel, userInfo); //TODO
        this.eventBroker.emit(new DescriptionTouchedEvent(data.getId()));

        this.annotationEntityTouchedIntegrationEventHandler.handleDescription(data.getId());
        
        this.elasticService.persistDescription(data);
        return this.builderFactory.builder(DescriptionBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Description._id), data);
    }

    private boolean isDescriptionTemplateMaxMultiplicityValid(PlanBlueprintEntity planBlueprintEntity, UUID planId, UUID planDescriptionTemplateId, Boolean isUpdate){
        org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());
        if (definition == null || this.conventionService.isListNullOrEmpty(definition.getSections())) return true;

        PlanDescriptionTemplateEntity planDescriptionTemplateEntity = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(planDescriptionTemplateId).isActive(IsActive.Active).planIds(planId).first();
        if (planDescriptionTemplateEntity == null) return true;

        List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(planId).planDescriptionTemplateIds(planDescriptionTemplateId).isActive(IsActive.Active).collect();

        for (SectionEntity section: definition.getSections()) {
            if (planDescriptionTemplateEntity.getSectionId().equals(section.getId()) && section.getHasTemplates() && !this.conventionService.isListNullOrEmpty(section.getDescriptionTemplates())){
                int descriptionsCount;
                if (isUpdate) descriptionsCount = -1;
                else descriptionsCount = 0;

                for (org.opencdmp.commons.types.planblueprint.DescriptionTemplateEntity sectionDescriptionTemplate: section.getDescriptionTemplates()) {
                    if (sectionDescriptionTemplate.getDescriptionTemplateGroupId().equals(planDescriptionTemplateEntity.getDescriptionTemplateGroupId())){
                        for (DescriptionEntity description: descriptionEntities){
                            if (description.getPlanDescriptionTemplateId().equals(planDescriptionTemplateEntity.getId())) descriptionsCount++;
                        }
                        if (sectionDescriptionTemplate.getMaxMultiplicity() != null && sectionDescriptionTemplate.getMaxMultiplicity() <= descriptionsCount) return false;
                    }

                }

            }

        }
        return true;
    }
    @Override
    public void updateDescriptionTemplate(UpdateDescriptionTemplatePersist model) throws InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("update description template").And("model", model));

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(model.getId())), Permission.EditDescription);

        DescriptionEntity data =  this.entityManager.find(DescriptionEntity.class, model.getId());
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());

        DescriptionTemplateEntity oldDescriptionTemplateEntity = this.entityManager.find(DescriptionTemplateEntity.class, data.getDescriptionTemplateId(), true);
        if (oldDescriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanDescriptionTemplateEntity planDescriptionTemplateEntity = this.entityManager.find(PlanDescriptionTemplateEntity.class, data.getPlanDescriptionTemplateId(), true);
        if (planDescriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getPlanDescriptionTemplateId(), PlanDescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));


        List<DescriptionTemplateEntity> latestVersionDescriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                .versionStatuses(DescriptionTemplateVersionStatus.Current)
                .isActive(IsActive.Active)
                .groupIds(oldDescriptionTemplateEntity.getGroupId())
                .collect();
        if (latestVersionDescriptionTemplates.isEmpty())
            throw new MyValidationException("New version not found");
        if (latestVersionDescriptionTemplates.size() > 1)
            throw new MyValidationException("Multiple template found");
        if (latestVersionDescriptionTemplates.getFirst().getVersion().equals(oldDescriptionTemplateEntity.getVersion()))
            throw new MyValidationException("Description already upgraded");

        data.setDescriptionTemplateId(latestVersionDescriptionTemplates.getFirst().getId());

        org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, latestVersionDescriptionTemplates.getFirst().getDefinition());
        PropertyDefinitionEntity propertyDefinition =  this.jsonHandlingService.fromJson(PropertyDefinitionEntity.class, data.getProperties());

        data.setProperties(this.jsonHandlingService.toJson(this.cleanPropertyDefinitionEntity(propertyDefinition, definition)));
        data.setUpdatedAt(Instant.now());
        this.entityManager.merge(data);

        this.entityManager.flush();

       
        this.sendNotification(data, true);

        this.eventBroker.emit(new DescriptionTouchedEvent(data.getId()));

        this.annotationEntityTouchedIntegrationEventHandler.handleDescription(data.getId());

        this.elasticService.persistDescription(data);
    }

    private void sendNotification(DescriptionEntity description, Boolean isUpdate) throws InvalidApplicationException {
        List<PlanUserEntity> existingUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking()
                .planIds(description.getPlanId())
                .isActives(IsActive.Active)
                .collect();

        if (existingUsers == null || existingUsers.size() <= 1){
            return;
        }
        for (PlanUserEntity planUser : existingUsers) {
            if (!planUser.getUserId().equals(this.userScope.getUserIdSafe())){
                UserEntity user = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUser.getUserId()).first();
                if (user == null || user.getIsActive().equals(IsActive.Inactive)) throw new MyValidationException(this.errors.getPlanInactiveUser().getCode(), this.errors.getPlanInactiveUser().getMessage());
                this.createDescriptionNotificationEvent(description, user, isUpdate);
            }
        }
    }

    private @NotNull PropertyDefinitionEntity cleanPropertyDefinitionEntity(PropertyDefinitionEntity data, org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition) {
        PropertyDefinitionEntity cleanData = new PropertyDefinitionEntity();
        if (data == null) return cleanData;
        if (data.getFieldSets() != null && !data.getFieldSets().isEmpty()){
            cleanData.setFieldSets(new HashMap<>());
            for (String key: data.getFieldSets().keySet()) {
                FieldSetEntity fieldSetEntity = definition != null ? definition.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                if (fieldSetEntity != null){
                    cleanData.getFieldSets().put(key, this.cleanPropertyDefinitionFieldSetEntity(data.getFieldSets().get(key), fieldSetEntity));
                }
            }
        }
        return cleanData;
    }

    private @NotNull PropertyDefinitionFieldSetEntity cleanPropertyDefinitionFieldSetEntity(PropertyDefinitionFieldSetEntity persist, FieldSetEntity fieldSetEntity) {
        PropertyDefinitionFieldSetEntity cleanData = new PropertyDefinitionFieldSetEntity();
        if (persist == null) return cleanData;
        if (!this.conventionService.isListNullOrEmpty(persist.getItems())){
            cleanData.setItems(new ArrayList<>());
            for (PropertyDefinitionFieldSetItemEntity itemsPersist: persist.getItems()) {
                cleanData.getItems().add(this.cleanPropertyDefinitionFieldSetItemEntity(itemsPersist, fieldSetEntity));
            }
        }
        return cleanData;
    }

    private @NotNull PropertyDefinitionFieldSetItemEntity cleanPropertyDefinitionFieldSetItemEntity(PropertyDefinitionFieldSetItemEntity data, FieldSetEntity fieldSetEntity) {
        PropertyDefinitionFieldSetItemEntity cleanData = new PropertyDefinitionFieldSetItemEntity();
        if (data == null) return cleanData;
        if (data.getFields() != null && !data.getFields().isEmpty()){
            cleanData.setOrdinal(data.getOrdinal());
            cleanData.setFields(new HashMap<>());
            for (String key: data.getFields().keySet()) {
                org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                if (fieldEntity != null){
                    cleanData.getFields().put(key,data.getFields().get(key));
                }
            }
        }
        return cleanData;
    }
    
    private void createDescriptionNotificationEvent(DescriptionEntity description, UserEntity user, Boolean isUpdate) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        this.applyNotificationType(description.getStatus(), event, isUpdate);
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, description.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, description.getId().toString()));
        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

	    this.eventHandler.handle(event);
    }

    private void applyNotificationType(DescriptionStatus status, NotifyIntegrationEvent event, Boolean isUpdate) {
        if (!isUpdate) event.setNotificationType(this.notificationProperties.getDescriptionCreatedType());
        else {
            switch (status) {
                case Draft:
                    event.setNotificationType(this.notificationProperties.getDescriptionModifiedType());
                    break;
                case Finalized:
                    event.setNotificationType(this.notificationProperties.getDescriptionFinalisedType());
                    break;
                default:
                    throw new MyApplicationException("Unsupported Description Status.");
            }
        }
    }

    @Override
    public Description persistStatus(DescriptionStatusPersist model, FieldSet fields) throws IOException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(model.getId())), Permission.EditDescription);

        DescriptionEntity data = this.entityManager.find(DescriptionEntity.class, model.getId());
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        if (!data.getStatus().equals(model.getStatus())){
            if (data.getStatus().equals(DescriptionStatus.Finalized)){
                this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(model.getId())), Permission.FinalizeDescription);
                PlanEntity planEntity = this.entityManager.find(PlanEntity.class, data.getPlanId(), true);
                if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getPlanId(), PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                if(!planEntity.getStatus().equals(PlanStatus.Draft)) throw new MyValidationException(this.errors.getPlanIsFinalized().getCode(), this.errors.getPlanIsFinalized().getMessage());
            }

            data.setStatus(model.getStatus());
            if (model.getStatus() == DescriptionStatus.Finalized) data.setFinalizedAt(Instant.now());
            data.setUpdatedAt(Instant.now());
            this.entityManager.merge(data);

            this.entityManager.flush();

            this.elasticService.persistDescription(data);
            this.eventBroker.emit(new DescriptionTouchedEvent(data.getId()));

            this.annotationEntityTouchedIntegrationEventHandler.handleDescription(data.getId());
            if (data.getStatus().equals(DescriptionStatus.Finalized)) this.sendNotification(data, true);
        }
        return this.builderFactory.builder(DescriptionBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Description._id), data);
    }

    public List<DescriptionValidationResult> validate(List<UUID> descriptionIds) throws InvalidApplicationException {
        List<DescriptionValidationResult> descriptionValidationResults = new ArrayList<>();

        List<DescriptionEntity> descriptions = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionIds).isActive(IsActive.Active).collect();
        if (descriptions == null){
            return null;
        }

        for (DescriptionEntity description: descriptions) {
            DescriptionValidationResult descriptionValidationResult = new DescriptionValidationResult(description.getId(), DescriptionValidationOutput.Invalid);

            DescriptionPersist.DescriptionPersistValidator validator = this.validatorFactory.validator(DescriptionPersist.DescriptionPersistValidator.class);
            validator.validate(this.buildDescriptionPersist(description));
            if (validator.result().isValid()) descriptionValidationResult.setResult(DescriptionValidationOutput.Valid);
            
            descriptionValidationResults.add(descriptionValidationResult);

        }
        return descriptionValidationResults;
    }

    private @NotNull PropertyDefinitionEntity buildPropertyDefinitionEntity(VisibilityService visibilityService, PropertyDefinitionPersist persist, org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition,  Map<String, List<UUID>> fieldToReferenceMap) throws InvalidApplicationException {
        PropertyDefinitionEntity data = new PropertyDefinitionEntity();
        if (persist == null) return data;
        if (persist.getFieldSets() != null && !persist.getFieldSets().isEmpty()){
            data.setFieldSets(new HashMap<>());
            for (String key: persist.getFieldSets().keySet()) {
                FieldSetEntity fieldSetEntity = definition != null ? definition.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                data.getFieldSets().put(key, this.buildPropertyDefinitionFieldSetEntity(persist.getFieldSets().get(key), fieldSetEntity, fieldToReferenceMap, visibilityService));
            }
        }
        return data;
    }

    private @NotNull PropertyDefinitionFieldSetEntity buildPropertyDefinitionFieldSetEntity(PropertyDefinitionFieldSetPersist persist, FieldSetEntity fieldSetEntity,  Map<String, List<UUID>> fieldToReferenceMap, VisibilityService visibilityService) throws InvalidApplicationException {
        PropertyDefinitionFieldSetEntity data = new PropertyDefinitionFieldSetEntity();
        if (persist == null) return data;
        data.setComment(persist.getComment());
        if (!this.conventionService.isListNullOrEmpty(persist.getItems())){
            data.setItems(new ArrayList<>());
            for (PropertyDefinitionFieldSetItemPersist itemsPersist: persist.getItems()) {
                data.getItems().add(this.buildPropertyDefinitionFieldSetItemEntity(itemsPersist, fieldSetEntity, fieldToReferenceMap, visibilityService));
            }
        }
        return data;
    }

    private @NotNull PropertyDefinitionFieldSetItemEntity buildPropertyDefinitionFieldSetItemEntity(PropertyDefinitionFieldSetItemPersist persist, FieldSetEntity fieldSetEntity,  Map<String, List<UUID>> fieldToReferenceMap, VisibilityService visibilityService) throws InvalidApplicationException {
        PropertyDefinitionFieldSetItemEntity data = new PropertyDefinitionFieldSetItemEntity();
        if (persist == null) return data;
        if (persist.getFields() != null && !persist.getFields().isEmpty()){
            data.setOrdinal(persist.getOrdinal());
            data.setFields(new HashMap<>());
            for (String key: persist.getFields().keySet()) {
                org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                boolean isVisible = fieldEntity != null ? visibilityService.isVisible(fieldEntity.getId(), persist.getOrdinal()) : false;
                data.getFields().put(key, this.buildFieldEntity(persist.getFields().get(key), fieldEntity, fieldToReferenceMap, persist.getOrdinal(), isVisible));
            }
        }
        return data;
    }
    
    private @NotNull FieldEntity buildFieldEntity(FieldPersist persist, org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity,  Map<String, List<UUID>> fieldToReferenceMap, int ordinal, boolean isVisible) throws InvalidApplicationException {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        FieldEntity data = new FieldEntity();
        if (persist == null || !isVisible) return data;

        if (FieldType.isTextType(fieldType)) {
            if (FieldType.UPLOAD.equals(fieldType)){
                UUID newFileId = this.conventionService.isValidUUID(persist.getTextValue()) ? UUID.fromString(persist.getTextValue()) : null;
                if (newFileId != null){
                    StorageFileEntity storageFileEntity = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(newFileId).firstAs(new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._storageType));
                    if (storageFileEntity == null || storageFileEntity.getStorageType().equals(StorageType.Temp)){
                        StorageFile storageFile = this.storageFileService.copyToStorage(newFileId, StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                        this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                        data.setTextValue(storageFile.getId().toString());
                    } else {
                        if (storageFileEntity.getId() != null){
                            //DO NOT Remove we can not be sure uf the description is copied
                            //this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().minusSeconds(60));
                        }
                        data.setTextValue(newFileId.toString());
                    }
                } else {
                    data.setTextValue(null);
                }
            } else {
                data.setTextValue(persist.getTextValue());
            }
        }
        else if (FieldType.isTextListType(fieldType)) {
            List<UUID> ids = new ArrayList<>();
            if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType)) {
                if (!this.conventionService.isListNullOrEmpty(persist.getTextListValue())) ids = persist.getTextListValue().stream().map(UUID::fromString).toList();
                else if (!this.conventionService.isNullOrEmpty(persist.getTextValue())) ids.add(UUID.fromString(persist.getTextValue()));

                if (!ids.isEmpty()){
                    Set<UUID> existingIds = this.queryFactory.query(PlanQuery.class).disableTracking().ids(ids).collectAs(new BaseFieldSet().ensure(Plan._id)).stream().map(PlanEntity::getId).collect(Collectors.toSet());
                    for (UUID id : ids){
                        if (!existingIds.contains(id)) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                    }
                }

            } if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType)){
                if ( !this.conventionService.isListNullOrEmpty(persist.getTextListValue())) ids = persist.getTextListValue().stream().map(UUID::fromString).toList();
                else if (!this.conventionService.isNullOrEmpty(persist.getTextValue())) ids.add(UUID.fromString(persist.getTextValue()));

                if (!ids.isEmpty()) {
                    Set<UUID> existingIds = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(ids).collectAs(new BaseFieldSet().ensure(Description._id)).stream().map(DescriptionEntity::getId).collect(Collectors.toSet());
                    for (UUID id : ids){
                        if (!existingIds.contains(id)) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                    }
                }
            }
            if (!ids.isEmpty()){
                if (ids.size() > 1) data.setTextListValue(persist.getTextListValue());
                else data.setTextListValue(List.of(persist.getTextValue()));
            }else{
                data.setTextListValue(persist.getTextListValue());
            }
            if (fieldType.equals(FieldType.SELECT) && this.conventionService.isListNullOrEmpty(persist.getTextListValue()) && !this.conventionService.isNullOrEmpty(persist.getTextValue())){
                data.setTextListValue(List.of(persist.getTextValue()));
            } else if (fieldType.equals(FieldType.SELECT)){
                data.setTextListValue(persist.getTextListValue());
            }
        } else if (FieldType.isReferenceType(fieldType) && fieldEntity != null ) {
            String key = this.fieldToReferenceMapKey(fieldEntity.getId(), ordinal);
            List<UUID> referenceIds = fieldToReferenceMap.getOrDefault(key, null);
            if (referenceIds != null) data.setTextListValue(referenceIds.stream().map(UUID::toString).toList());
        } else if (FieldType.isTagType(fieldType)) {
            
            if (!this.conventionService.isListNullOrEmpty(persist.getTags())){
                List<TagEntity> existingTags = this.queryFactory.query(TagQuery.class).authorize(AuthorizationFlags.AllExceptPublic).disableTracking().tags(persist.getTags().stream().distinct().toList()).collectAs(new BaseFieldSet().ensure(Tag._id).ensure(Tag._label));
                
                List<String> values = new ArrayList<>();
                for (String tag : persist.getTags().stream().distinct().toList()){
                    TagEntity existingTag = existingTags.stream().filter(x-> x.getLabel().equalsIgnoreCase(tag)).findFirst().orElse(null);
                    if (existingTag == null) {
                        TagPersist tagPersist = new TagPersist();
                        tagPersist.setLabel(tag);
                        values.add(this.tagService.persist(tagPersist, new BaseFieldSet().ensure(Tag._id)).getId().toString());
                    } else values.add(existingTag.getId().toString());
                }
                if (!this.conventionService.isListNullOrEmpty(values)) data.setTextListValue(values);
            }
        }
        else if (FieldType.isDateType(fieldType))  data.setDateValue(persist.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  data.setBooleanValue(persist.getBooleanValue());
        else if (FieldType.isExternalIdentifierType(fieldType) && persist.getExternalIdentifier() != null)  data.setExternalIdentifier(this.buildExternalIdentifierEntity(persist.getExternalIdentifier()));
       
        return data;
    }

    private @NotNull ExternalIdentifierEntity buildExternalIdentifierEntity(ExternalIdentifierPersist persist){
        ExternalIdentifierEntity data = new ExternalIdentifierEntity();
        if (persist == null) return data;

        data.setIdentifier(persist.getIdentifier());
        data.setType(persist.getType());
        return data;
    }

    private @NotNull  List<DescriptionReferencePersist> buildDescriptionReferencePersists(VisibilityService visibilityService, PropertyDefinitionPersist persist){
        List<DescriptionReferencePersist> descriptionReferencePersists = new ArrayList<>();
        if (persist.getFieldSets() != null && !persist.getFieldSets().isEmpty()){
            for (PropertyDefinitionFieldSetPersist propertyDefinitionFieldSetPersist: persist.getFieldSets().values()) {
                if (!this.conventionService.isListNullOrEmpty( propertyDefinitionFieldSetPersist.getItems())) {
                    for (PropertyDefinitionFieldSetItemPersist definitionFieldSetItemPersist : propertyDefinitionFieldSetPersist.getItems()) {
                        if (definitionFieldSetItemPersist.getFields() != null && !definitionFieldSetItemPersist.getFields().isEmpty()) {
                            for (String key : definitionFieldSetItemPersist.getFields().keySet()) {
                                FieldPersist fieldPersist = definitionFieldSetItemPersist.getFields().get(key);
                                boolean isVisible = visibilityService.isVisible(key, definitionFieldSetItemPersist.getOrdinal());
	                            if (isVisible) this.BuildDescriptionReferencePersist(key, definitionFieldSetItemPersist.getOrdinal(), fieldPersist, descriptionReferencePersists);
                            }
                        }
                    }
                }
            }
        }
        return descriptionReferencePersists;
    }

    private void BuildDescriptionReferencePersist(String fieldId, int ordinal, FieldPersist fieldPersist, List<DescriptionReferencePersist> descriptionReferencePersists) {
        if (fieldPersist.getReference() != null) {
            if (fieldPersist.getReferences() == null) fieldPersist.setReferences(new ArrayList<>());
            fieldPersist.getReferences().add(fieldPersist.getReference());
        }
        if (!this.conventionService.isListNullOrEmpty(fieldPersist.getReferences())) {
            List<UUID> usedReferences = new ArrayList<>();
            for (ReferencePersist referencePersist : fieldPersist.getReferences()) {
                if (referencePersist.getId() != null && usedReferences.contains(referencePersist.getId())) continue;
                if (referencePersist.getId() != null) usedReferences.add(referencePersist.getId());
                DescriptionReferencePersist descriptionReferencePersist = new DescriptionReferencePersist();
                descriptionReferencePersist.setData(new DescriptionReferenceDataPersist());
                descriptionReferencePersist.getData().setFieldId(fieldId);
                descriptionReferencePersist.getData().setOrdinal(ordinal);
                descriptionReferencePersist.setReference(referencePersist);
                descriptionReferencePersists.add(descriptionReferencePersist);
            }
        }
    }

    private Map<String, List<UUID>> patchAndSaveReferences(List<DescriptionReferencePersist> models, UUID descriptionId, org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition) throws InvalidApplicationException {
        if (models == null) models = new ArrayList<>();
        
        Map<String, List<UUID>> fieldToReferenceMap = new HashMap<>();

        List<DescriptionReferenceEntity> descriptionReferences = this.queryFactory.query(DescriptionReferenceQuery.class).isActive(IsActive.Active).descriptionIds(descriptionId).collect();
        Map<UUID, List<DescriptionReferenceEntity>> descriptionReferenceEntityByReferenceId = new HashMap<>();
        for (DescriptionReferenceEntity descriptionReferenceEntity : descriptionReferences){
            List<DescriptionReferenceEntity> descriptionReferenceEntities = descriptionReferenceEntityByReferenceId.getOrDefault(descriptionReferenceEntity.getReferenceId(), null);
            if (descriptionReferenceEntities == null) {
                descriptionReferenceEntities = new ArrayList<>();
                descriptionReferenceEntityByReferenceId.put(descriptionReferenceEntity.getReferenceId(), descriptionReferenceEntities);
            }
            descriptionReferenceEntities.add(descriptionReferenceEntity);
        }

        Map<UUID, DescriptionReferenceDataEntity> descriptionReferenceDataEntityMap = new HashMap<>();
        for (DescriptionReferenceEntity descriptionReferenceEntity : descriptionReferences){
            descriptionReferenceDataEntityMap.put(descriptionReferenceEntity.getId(), this.jsonHandlingService.fromJsonSafe(DescriptionReferenceDataEntity.class, descriptionReferenceEntity.getData()));
        }

        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (DescriptionReferencePersist model : models) {
            ReferencePersist referencePersist = model.getReference();
            ReferenceEntity referenceEntity;
            if (this.conventionService.isValidGuid(referencePersist.getId())){
                referenceEntity = this.entityManager.find(ReferenceEntity.class, referencePersist.getId());
                if (referenceEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{referencePersist.getId(), Reference.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            } else {
                ReferenceTypeDataEntity fieldEntity = definition.getFieldById(model.getData().getFieldId()).stream().filter(x-> x.getData() != null && x.getData().getFieldType().equals(FieldType.REFERENCE_TYPES)).map(x-> (ReferenceTypeDataEntity)x.getData()).findFirst().orElse(null);
                if (fieldEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getData().getFieldId(), ReferenceTypeDataEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                referenceEntity = this.queryFactory.query(ReferenceQuery.class).sourceTypes(referencePersist.getSourceType()).typeIds(fieldEntity.getReferenceTypeId()).sources(referencePersist.getSource()).isActive(IsActive.Active).references(referencePersist.getReference()).first();
                if (referenceEntity == null){
                    referenceEntity = new ReferenceEntity();
                    referenceEntity.setId(UUID.randomUUID());
                    referenceEntity.setLabel(referencePersist.getLabel());
                    referenceEntity.setDescription(referencePersist.getDescription());
                    referenceEntity.setIsActive(IsActive.Active);
                    referenceEntity.setCreatedAt(Instant.now());
                    referenceEntity.setTypeId(fieldEntity.getReferenceTypeId());

                    referenceEntity.setDefinition(this.xmlHandlingService.toXmlSafe(this.buildDefinitionEntity(referencePersist.getDefinition())));
                    referenceEntity.setUpdatedAt(Instant.now());
                    referenceEntity.setReference(referencePersist.getReference());
                    referenceEntity.setAbbreviation(referencePersist.getAbbreviation());
                    referenceEntity.setSource(referencePersist.getSource());
                    referenceEntity.setSourceType(referencePersist.getSourceType());
                    try {
                        ReferenceTypeEntity referenceType = this.queryFactory.query(ReferenceTypeQuery.class).ids(fieldEntity.getReferenceTypeId()).firstAs(new BaseFieldSet().ensure(ReferenceType._id).ensure(ReferenceTypeEntity._tenantId));
                        if (referenceType == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{fieldEntity.getReferenceTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                        if (referenceEntity.getSourceType().equals(ReferenceSourceType.External) && !this.tenantScope.isDefaultTenant() && referenceType.getTenantId() == null){
                            this.tenantScope.setTempTenant(this.entityManager, null, this.tenantScope.getDefaultTenantCode());
                        }
                        this.entityManager.persist(referenceEntity);
                    } finally {
	                    this.tenantScope.removeTempTenant(this.entityManager);
                    }
                }
            }

            DescriptionReferenceEntity data = null;
            List<DescriptionReferenceEntity> descriptionReferenceEntities = descriptionReferenceEntityByReferenceId.getOrDefault(referenceEntity.getId(), new ArrayList<>());
            for (DescriptionReferenceEntity descriptionReferenceEntity : descriptionReferenceEntities){
                DescriptionReferenceDataEntity descriptionReferenceDataEntity = descriptionReferenceDataEntityMap.getOrDefault(descriptionReferenceEntity.getId(), new DescriptionReferenceDataEntity());
                if (Objects.equals(descriptionReferenceDataEntity.getFieldId(), model.getData().getFieldId()) && Objects.equals(descriptionReferenceDataEntity.getOrdinal(), model.getData().getOrdinal())){
                    data = descriptionReferenceEntity;
                    break;
                }
            }
            boolean isUpdate = data != null;

            if (!isUpdate) {
                data = new DescriptionReferenceEntity();
                data.setId(UUID.randomUUID());
                data.setReferenceId(referenceEntity.getId());
                data.setDescriptionId(descriptionId);
                data.setCreatedAt(Instant.now());
                data.setIsActive(IsActive.Active);
                data.setData(this.jsonHandlingService.toJsonSafe(this.buildDescriptionReferenceDataEntity(model.getData())));
            }
            updatedCreatedIds.add(data.getId());
            
            if (model.getData() != null){
                String key = this.fieldToReferenceMapKey(model.getData().getFieldId(), model.getData().getOrdinal());
                if (!fieldToReferenceMap.containsKey(key)) fieldToReferenceMap.put(key, new ArrayList<>());
                fieldToReferenceMap.get(key).add(referenceEntity.getId());
            }

            data.setUpdatedAt(Instant.now());

            if (isUpdate) this.entityManager.merge(data);
            else this.entityManager.persist(data);
        }
        List<DescriptionReferenceEntity> toDelete = descriptionReferences.stream().filter(x-> updatedCreatedIds.stream().noneMatch(y-> y.equals(x.getId()))).collect(Collectors.toList());
        this.deleterFactory.deleter(DescriptionReferenceDeleter.class).delete(toDelete);
        this.entityManager.flush();
        
        return fieldToReferenceMap;
    }
    
    private String fieldToReferenceMapKey(String fieldId, int ordinal){
        return fieldId + "_" + ordinal;
    }

    private @NotNull DescriptionReferenceDataEntity buildDescriptionReferenceDataEntity(DescriptionReferenceDataPersist persist){
        DescriptionReferenceDataEntity data = new DescriptionReferenceDataEntity();
        if (persist == null) return data;
        data.setFieldId(persist.getFieldId());
        data.setOrdinal(persist.getOrdinal());
        return data;
    }

    private void persistTags(UUID id, List<String> tagLabels) throws InvalidApplicationException {
        if (tagLabels == null) tagLabels = new ArrayList<>();
        tagLabels = tagLabels.stream().filter(x-> x != null && !x.isBlank()).toList();
        
        List<DescriptionTagEntity> items = this.queryFactory.query(DescriptionTagQuery.class).isActive(IsActive.Active).descriptionIds(id).collect();
        List<TagEntity> tagsAlreadyLinked = this.queryFactory.query(TagQuery.class).isActive(IsActive.Active).ids(items.stream().map(DescriptionTagEntity::getTagId).collect(Collectors.toList())).collect();
        List<String> tagLabelsToAdd = tagLabels.stream().filter(x-> tagsAlreadyLinked.stream().noneMatch(y-> y.getLabel() != null && y.getLabel().equalsIgnoreCase(x))).toList();
        List<TagEntity> existingTags = this.queryFactory.query(TagQuery.class).isActive(IsActive.Active).tags(tagLabelsToAdd).createdByIds(this.userScope.getUserId()).collect();
        
        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (String tagLabel : tagLabels) {
            TagEntity alreadyLinkedTag = tagsAlreadyLinked.stream().filter(x-> x.getLabel() != null && x.getLabel().equalsIgnoreCase(tagLabel)).findFirst().orElse(null);
            if (alreadyLinkedTag != null){
                updatedCreatedIds.addAll(items.stream().filter(x-> x.getTagId().equals(alreadyLinkedTag.getId())).map(DescriptionTagEntity::getId).toList());
            } else{
                TagEntity existingTag = existingTags.stream().filter(x-> x.getLabel() != null && x.getLabel().equalsIgnoreCase(tagLabel)).findFirst().orElse(null);
                if (existingTag == null){
                    existingTag = new TagEntity();
                    existingTag.setId(UUID.randomUUID());
                    existingTag.setLabel(tagLabel);
                    existingTag.setIsActive(IsActive.Active);
                    existingTag.setCreatedAt(Instant.now());
                    existingTag.setUpdatedAt(Instant.now());
                    this.entityManager.persist(existingTag);
                }

                DescriptionTagEntity link = new DescriptionTagEntity();
                link.setId(UUID.randomUUID());
                link.setTagId(existingTag.getId());
                link.setDescriptionId(id);
                link.setIsActive(IsActive.Active);
                link.setCreatedAt(Instant.now());
                link.setUpdatedAt(Instant.now());
                this.entityManager.persist(link);
                updatedCreatedIds.add(link.getId());
            }
        }
        List<DescriptionTagEntity> toDelete = items.stream().filter(x-> updatedCreatedIds.stream().noneMatch(y-> y.equals(x.getId()))).collect(Collectors.toList());

        this.deleterFactory.deleter(DescriptionTagDeleter.class).delete(toDelete);
    }
    
    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist){
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getFields())){
            data.setFields(new ArrayList<>());
            for (org.opencdmp.model.persist.referencedefinition.FieldPersist fieldPersist: persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }

        return data;
    }

    private @NotNull org.opencdmp.commons.types.reference.FieldEntity buildFieldEntity(org.opencdmp.model.persist.referencedefinition.FieldPersist persist){
        org.opencdmp.commons.types.reference.FieldEntity data = new org.opencdmp.commons.types.reference.FieldEntity();
        if (persist == null) return data;

        data.setCode(persist.getCode());
        data.setDataType(persist.getDataType());
        data.setValue(persist.getValue());

        return data;
    }

    //endregion
    
    //region delete
    
    @Override
    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException {
        logger.debug("deleting description: {}", id);

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(id)), Permission.DeleteDescription);

        this.deleterFactory.deleter(DescriptionDeleter.class).deleteAndSaveByIds(List.of(id), false);

        this.annotationEntityRemovalIntegrationEventHandler.handleDescription(id);
    }
    
    //endregion 

    //region clone
    
    //endregion

    //region file export

    @Override
    public ResponseEntity<byte[]> export(UUID id, String exportType, boolean isPublic) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportDescription(id, null, exportType, isPublic); //TODO get repo from config
        headers.add("Content-Disposition", "attachment;filename=" + fileEnvelope.getFilename());
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
    //endregion



    @Override
    public StorageFile uploadFieldFile(DescriptionFieldFilePersist model, MultipartFile file, FieldSet fields) throws IOException {
        //this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(descriptionId)), Permission.CloneDescription);
        this.authorizationService.authorizeForce(Permission.EditDescription);//TODO: Missing Description or plan for authz
        
        DescriptionTemplateEntity descriptionTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(model.getDescriptionTemplateId()).authorize(AuthorizationFlags.AllExceptPublic).first();
        if (descriptionTemplate == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, descriptionTemplate.getDefinition());
        if (definition == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getDescriptionTemplateId(), org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = definition.getFieldById(model.getFieldId()).stream().filter(x -> x != null && x.getData() != null && x.getData().getFieldType().equals(FieldType.UPLOAD)).findFirst().orElse(null);
        if (fieldEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getFieldId(), org.opencdmp.commons.types.descriptiontemplate.FieldEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        UploadDataEntity uploadDataEntity = (UploadDataEntity)fieldEntity.getData();
        if (DataSize.ofBytes(file.getSize()).equals(DataSize.ofMegabytes(uploadDataEntity.getMaxFileSizeInMB()))) {
            throw new MyValidationException("The uploaded file is too large");
        }
        if(!this.conventionService.isListNullOrEmpty(uploadDataEntity.getTypes())) {
            boolean isContentTypeAccepted = false;
            for (UploadDataEntity.UploadDataOptionEntity option: uploadDataEntity.getTypes()) {
                if(Objects.equals(file.getContentType(), option.getValue())) {
                    isContentTypeAccepted = true;
                    break;
                }
            }
            if (!isContentTypeAccepted){
                throw new MyValidationException("The uploaded file has an unaccepted type");
            }
        }
        StorageFilePersist storageFilePersist = new StorageFilePersist();
        storageFilePersist.setName(FilenameUtils.removeExtension(file.getName()));
        storageFilePersist.setExtension(FilenameUtils.getExtension(file.getName()));
        storageFilePersist.setMimeType(URLConnection.guessContentTypeFromName(file.getName()));
        storageFilePersist.setOwnerId(this.userScope.getUserIdSafe());
        storageFilePersist.setStorageType(StorageType.Temp);
        storageFilePersist.setLifetime(Duration.ofSeconds(this.storageFileConfig.getTempStoreLifetimeSeconds()));
        this.validatorFactory.validator(StorageFilePersist.StorageFilePersistValidator.class).validateForce(storageFilePersist);
        return this.storageFileService.persistBytes(storageFilePersist, file.getBytes(), BaseFieldSet.build(fields, StorageFile._id, StorageFile._name));
    }

    @Override
    public StorageFileEntity getFieldFile(UUID descriptionId, UUID storageFileId) {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(descriptionId)), Permission.BrowseDescription);
        
        DescriptionEntity descriptionEntity = this.queryFactory.query(DescriptionQuery.class).disableTracking().isActive(IsActive.Active).ids(descriptionId).first();
        if (descriptionEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        PlanDescriptionTemplateEntity planDescriptionTemplateEntity = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(descriptionEntity.getPlanDescriptionTemplateId()).isActive(IsActive.Active).first();
        if (planDescriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionEntity.getPlanDescriptionTemplateId(), PlanDescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).ids(planDescriptionTemplateEntity.getPlanId()).disableTracking().isActive(IsActive.Active).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDescriptionTemplateEntity.getPlanId(), Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!planEntity.getAccessType().equals(PlanAccessType.Public))
        {
            boolean isPlanUser = this.queryFactory.query(PlanUserQuery.class).planIds(planEntity.getId()).disableTracking().userIds(this.userScope.getUserIdSafe()).isActives(IsActive.Active).count() > 0;
            if (!isPlanUser) throw new MyUnauthorizedException();
        }
        
        StorageFileEntity storageFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(storageFileId).first();
        if (storageFile == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{storageFileId, StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        return storageFile;
    }


    //region build persist

    private @NotNull DescriptionPersist buildDescriptionPersist(DescriptionEntity data) throws InvalidApplicationException {
        DescriptionPersist persist = new DescriptionPersist();
        if (data == null) return persist;

        DescriptionTemplateEntity descriptionTemplateEntity = this.entityManager.find(DescriptionTemplateEntity.class, data.getDescriptionTemplateId(), true);
        if (descriptionTemplateEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        persist.setId(data.getId());
        persist.setLabel(data.getLabel());
        persist.setStatus(DescriptionStatus.Finalized);
        persist.setDescription(data.getDescription());
        persist.setDescriptionTemplateId(data.getDescriptionTemplateId());
        persist.setPlanId(data.getPlanId());
        persist.setPlanDescriptionTemplateId(data.getPlanDescriptionTemplateId());
        persist.setHash(this.conventionService.hashValue(data.getUpdatedAt()));

        org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition =  this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, descriptionTemplateEntity.getDefinition());

        persist.setProperties(this.buildPropertyDefinitionPersist( this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, data.getProperties()), definition));

        return persist;
    }

    private @NotNull PropertyDefinitionPersist buildPropertyDefinitionPersist(PropertyDefinitionEntity data, org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition){
        PropertyDefinitionPersist persist = new PropertyDefinitionPersist();
        if (data == null) return persist;

        List<UUID> referenceIds = this.calculateAllReferenceIdsFromData(data, definition);
        List<ReferenceEntity> references = null;
        if (!this.conventionService.isListNullOrEmpty(referenceIds)){
             references = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(referenceIds).collect();
        }
        if (data.getFieldSets() != null && !data.getFieldSets().isEmpty()){
            persist.setFieldSets(new HashMap<>());
            for (String key: data.getFieldSets().keySet()) {
                FieldSetEntity fieldSetEntity = definition != null ? definition.getFieldSetById(key).stream().findFirst().orElse(null) : null;
                persist.getFieldSets().put(key, this.buildPropertyDefinitionFieldSetPersist(data.getFieldSets().get(key), fieldSetEntity, references));
            }
        }
        return persist;
    }

    private List<UUID> calculateAllReferenceIdsFromData(PropertyDefinitionEntity data, org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition){
        List<String> referenceIds = new ArrayList<>();

        if (data.getFieldSets() != null && !data.getFieldSets().isEmpty()){
            for (PropertyDefinitionFieldSetEntity fieldSet: data.getFieldSets().values()) {
                if (!this.conventionService.isListNullOrEmpty(fieldSet.getItems())){
                    for (PropertyDefinitionFieldSetItemEntity item: fieldSet.getItems()) {
                        if (item.getFields() != null && !item.getFields().isEmpty()) {
                            for (String key : item.getFields().keySet()) {
                                if (definition.getFieldById(key).getFirst() != null && FieldType.isReferenceType(definition.getFieldById(key).getFirst().getData().getFieldType())) {
                                    if (!this.conventionService.isListNullOrEmpty(item.getFields().get(key).getTextListValue())) {
                                        referenceIds.addAll(item.getFields().get(key).getTextListValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!this.conventionService.isListNullOrEmpty(referenceIds)) {
            referenceIds = referenceIds.stream().distinct().collect(Collectors.toList());
            return referenceIds.stream().filter(x -> this.conventionService.isValidGuid(UUID.fromString(x))).toList().stream().map(UUID::fromString).collect(Collectors.toList());

        }

        return null;
    }

    private @NotNull PropertyDefinitionFieldSetPersist buildPropertyDefinitionFieldSetPersist(PropertyDefinitionFieldSetEntity data, FieldSetEntity fieldSetEntity, List<ReferenceEntity> references){
        PropertyDefinitionFieldSetPersist persist = new PropertyDefinitionFieldSetPersist();
        if (data == null) return persist;
        persist.setComment(data.getComment());
        if (!this.conventionService.isListNullOrEmpty(data.getItems())){
            persist.setItems(new ArrayList<>());
            for (PropertyDefinitionFieldSetItemEntity itemsPersist: data.getItems()) {
                persist.getItems().add(this.buildPropertyDefinitionFieldSetItemPersist(itemsPersist, fieldSetEntity, references));
            }
        }
        return persist;
    }

    private @NotNull PropertyDefinitionFieldSetItemPersist buildPropertyDefinitionFieldSetItemPersist(PropertyDefinitionFieldSetItemEntity data, FieldSetEntity fieldSetEntity, List<ReferenceEntity> references){
        PropertyDefinitionFieldSetItemPersist persist = new PropertyDefinitionFieldSetItemPersist();
        if (data == null) return persist;
        if (data.getFields() != null && !data.getFields().isEmpty()){
            persist.setOrdinal(data.getOrdinal());
            persist.setFields(new HashMap<>());
            for (String key: data.getFields().keySet()) {
                org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = fieldSetEntity != null ? fieldSetEntity.getFieldById(key).stream().findFirst().orElse(null) : null;
                persist.getFields().put(key, this.buildFieldPersist(data.getFields().get(key), fieldEntity, references));
            }
        }
        return persist;
    }

    private @NotNull FieldPersist buildFieldPersist(FieldEntity data, org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity, List<ReferenceEntity> references) {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;

        FieldPersist persist = new FieldPersist();
        if (data == null) return persist;

        if (FieldType.isTextType(fieldType)) persist.setTextValue(data.getTextValue());
        else if (FieldType.isTextListType(fieldType)) persist.setTextListValue(data.getTextListValue());
        else if (FieldType.isDateType(fieldType))  persist.setDateValue(data.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  persist.setBooleanValue(data.getBooleanValue());
        else if (FieldType.isExternalIdentifierType(fieldType) && data.getExternalIdentifier() != null)  persist.setExternalIdentifier(this.buildExternalIdentifierPersist(data.getExternalIdentifier()));
        else if (FieldType.isReferenceType(fieldType) && fieldEntity != null ) {
            if (!this.conventionService.isListNullOrEmpty(data.getTextListValue()) && !this.conventionService.isListNullOrEmpty(references)){
                List<UUID> referenceIdsInField = data.getTextListValue().stream().filter(x -> this.conventionService.isValidGuid(UUID.fromString(x))).toList().stream().map(UUID::fromString).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(referenceIdsInField)){
                    List<ReferenceEntity> referencesInField = references.stream().filter(x -> referenceIdsInField.contains(x.getId())).collect(Collectors.toList());
                    if (!this.conventionService.isListNullOrEmpty(referencesInField)){
                        persist.setReferences(this.buildReferencePersist(referencesInField));
                    }
                }
            }
        }
        else if (FieldType.isTagType(fieldType) && fieldEntity != null ) {
            if (!this.conventionService.isListNullOrEmpty(data.getTextListValue())){
                List<UUID> tagIdsInField = data.getTextListValue().stream().filter(x -> this.conventionService.isValidGuid(UUID.fromString(x))).toList().stream().map(UUID::fromString).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(tagIdsInField)){
                    List<TagEntity> tagsInField = this.queryFactory.query(TagQuery.class).isActive(IsActive.Active).ids(tagIdsInField).disableTracking().authorize(AuthorizationFlags.All).collect();
                    if (!this.conventionService.isListNullOrEmpty(tagsInField)){
                        persist.setTags(tagsInField.stream().map(TagEntity::getLabel).toList());
                    }
                }
            }
        }
        

        return persist;
    }

    private @NotNull ExternalIdentifierPersist buildExternalIdentifierPersist(ExternalIdentifierEntity data){
        ExternalIdentifierPersist persist = new ExternalIdentifierPersist();
        if (data == null) return persist;

        persist.setIdentifier(data.getIdentifier());
        persist.setType(data.getType());
        return persist;
    }

    private @NotNull List<ReferencePersist> buildReferencePersist(List<ReferenceEntity> referenceEntities){
        List<ReferencePersist> referencesPersist = new ArrayList<>();
        if (this.conventionService.isListNullOrEmpty(referenceEntities)) return referencesPersist;

        for (ReferenceEntity entity: referenceEntities) {
            ReferencePersist persist = new ReferencePersist();
            persist.setId(entity.getId());
            persist.setLabel(entity.getLabel());
            persist.setDescription(entity.getDescription());
            persist.setReference(entity.getReference());
            persist.setSource(entity.getSource());
            persist.setSourceType(entity.getSourceType());
            persist.setTypeId(entity.getTypeId());
            persist.setAbbreviation(entity.getAbbreviation());

            referencesPersist.add(persist);
        }

        return referencesPersist;
    }

    //region Export

    @Override
    public DescriptionImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize, boolean isPublic) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));

        if (!ignoreAuthorize) this.authorizationService.authorizeForce(Permission.ExportDescription);
        DescriptionEntity data = null;
        if (!isPublic) {
            data = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.All).isActive(IsActive.Active).first();
        } else {
            try {
                this.entityManager.disableTenantFilters();
                data = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(id).planSubQuery(this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).statuses(PlanStatus.Finalized).accessTypes(PlanAccessType.Public)).first();
                this.entityManager.reloadTenantFilters();
            } finally {
                this.entityManager.reloadTenantFilters();
            }
        }
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PropertyDefinitionEntity definition = this.jsonHandlingService.fromJson(PropertyDefinitionEntity.class, data.getProperties());
        return this.toExport(data, definition);
    }

    @Override
    public ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));

        this.authorizationService.authorizeForce(Permission.ExportDescription);
        DescriptionEntity data = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.All).isActive(IsActive.Active).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String xml = this.xmlHandlingService.toXml(this.exportXmlEntity(data.getId(), false, false));
        this.accountingService.increase(UsageLimitTargetMetric.EXPORT_DESCRIPTION_XML_EXECUTION_COUNT.getValue());
        return this.responseUtilsService.buildResponseFileFromText(xml, data.getLabel() + ".xml");
    }

    @Override
    public ResponseEntity<byte[]> exportPublicXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export xml").And("id", id));

        this.authorizationService.authorizeForce(Permission.ExportDescription);
        DescriptionEntity data = null;
        try {
            this.entityManager.disableTenantFilters();
            data = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(id).planSubQuery(this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).statuses(PlanStatus.Finalized).accessTypes(PlanAccessType.Public)).first();
            this.entityManager.reloadTenantFilters();
        } finally {
            this.entityManager.reloadTenantFilters();
        }

        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PublicDescription.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String xml = this.xmlHandlingService.toXml(this.exportXmlEntity(data.getId(), false, true));
        this.accountingService.increase(UsageLimitTargetMetric.EXPORT_DESCRIPTION_XML_EXECUTION_COUNT.getValue());
        return this.responseUtilsService.buildResponseFileFromText(xml, data.getLabel() + ".xml");
    }

    private DescriptionImportExport toExport(DescriptionEntity data, PropertyDefinitionEntity propertiesEntity) throws InvalidApplicationException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        DescriptionImportExport xml = new DescriptionImportExport();
        xml.setId(data.getId());
        xml.setDescription(data.getDescription());
        xml.setLabel(data.getLabel());
        xml.setFinalizedAt(data.getFinalizedAt());

        PlanDescriptionTemplateEntity planDescriptionTemplateEntity = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(data.getPlanDescriptionTemplateId()).authorize(AuthorizationFlags.All).isActive(IsActive.Active).first();
        if (planDescriptionTemplateEntity != null) xml.setSectionId(planDescriptionTemplateEntity.getSectionId());

        DescriptionTagQuery descriptionTagQuery = this.queryFactory.query(DescriptionTagQuery.class);
        descriptionTagQuery.descriptionIds(data.getId());
        descriptionTagQuery.isActive(IsActive.Active);

        List<TagEntity> tagsEntities = this.queryFactory.query(TagQuery.class).disableTracking().descriptionTagSubQuery(descriptionTagQuery).authorize(AuthorizationFlags.All).isActive(IsActive.Active).collect();
        if (!this.conventionService.isListNullOrEmpty(tagsEntities)) xml.setTags(tagsEntities.stream().map(TagEntity::getLabel).collect(Collectors.toList()));

        DescriptionTemplateEntity descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(data.getDescriptionTemplateId()).authorize(AuthorizationFlags.All).first();
        if (descriptionTemplateEntity != null) {
            xml.setDescriptionTemplate(this.descriptionTemplateService.exportXmlEntity(descriptionTemplateEntity.getId(), true));
        }

        if (propertiesEntity != null) {
            xml.setProperties(this.descriptionPropertyDefinitionToExport(propertiesEntity));
        }

        List<DescriptionReferenceEntity> planReferences = this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().descriptionIds(data.getId()).authorize(AuthorizationFlags.All).isActive(IsActive.Active).collect();
        if (!this.conventionService.isListNullOrEmpty(planReferences)) {
            List<ReferenceEntity> references = this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(planReferences.stream().map(DescriptionReferenceEntity::getReferenceId).distinct().toList()).authorize(AuthorizationFlags.All).isActive(IsActive.Active).collect();
            Map<UUID, ReferenceEntity> referenceEntityMap = references == null ? new HashMap<>() : references.stream().collect(Collectors.toMap(ReferenceEntity::getId, x-> x));
            List<ReferenceTypeEntity> referenceTypes = references == null ? new ArrayList<>() : this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(references.stream().map(ReferenceEntity::getTypeId).distinct().toList()).authorize(AuthorizationFlags.All).isActive(IsActive.Active).collect();
            Map<UUID, ReferenceTypeEntity> referenceTypeEntityMap = referenceTypes == null ? new HashMap<>() : referenceTypes.stream().collect(Collectors.toMap(ReferenceTypeEntity::getId, x-> x));
            List<DescriptionReferenceImportExport> descriptionReferenceImportExports = new LinkedList<>();
            for (DescriptionReferenceEntity descriptionReferenceEntity : planReferences) {
                descriptionReferenceImportExports.add(this.descriptionReferenceToExport(descriptionReferenceEntity, referenceEntityMap, referenceTypeEntityMap));
            }
            xml.setReferences(descriptionReferenceImportExports);
        }
        return xml;
    }

    private DescriptionReferenceImportExport descriptionReferenceToExport(DescriptionReferenceEntity entity, Map<UUID, ReferenceEntity> referenceEntityMap, Map<UUID, ReferenceTypeEntity> referenceTypeEntityMap) {
        DescriptionReferenceImportExport xml = new DescriptionReferenceImportExport();
        if (entity == null) return xml;
        DescriptionReferenceDataEntity referenceData = this.jsonHandlingService.fromJsonSafe(DescriptionReferenceDataEntity.class, entity.getData());
        if (referenceData != null){
            xml.setFieldId(referenceData.getFieldId());
            xml.setOrdinal(referenceData.getOrdinal());
        }
        ReferenceEntity reference = referenceEntityMap.getOrDefault(entity.getReferenceId(), null);

        if (reference != null){
            xml.setId(reference.getId());
            xml.setLabel(reference.getLabel());
            xml.setReference(reference.getReference());
            xml.setSource(reference.getSource());
            ReferenceTypeEntity referenceType = referenceTypeEntityMap.getOrDefault(reference.getTypeId(), null);
            if (referenceType != null) xml.setType(this.descriptionReferenceTypeToExport(referenceType));
        }

        return xml;
    }

    private DescriptionReferenceTypeImportExport descriptionReferenceTypeToExport(ReferenceTypeEntity entity) {
        DescriptionReferenceTypeImportExport xml = new DescriptionReferenceTypeImportExport();
        if (entity == null) return xml;
        xml.setId(entity.getId());
        xml.setCode(entity.getCode());
        xml.setName(entity.getName());

        return xml;
    }


    private DescriptionPropertyDefinitionImportExport descriptionPropertyDefinitionToExport(PropertyDefinitionEntity entity) {
        DescriptionPropertyDefinitionImportExport xml = new DescriptionPropertyDefinitionImportExport();
        if (entity == null) return xml;

        if (entity.getFieldSets() != null && !entity.getFieldSets().isEmpty()) {
            List<DescriptionPropertyDefinitionFieldSetImportExport> exports = new LinkedList<>();
            for (Map.Entry<String, PropertyDefinitionFieldSetEntity> fieldSetEntityEntry : entity.getFieldSets().entrySet()) {
                exports.add(this.descriptionPropertyDefinitionFieldSetToExport(fieldSetEntityEntry.getValue(), fieldSetEntityEntry.getKey()));
            }
            xml.setFieldSets(exports);
        }

        return xml;
    }

    private DescriptionPropertyDefinitionFieldSetImportExport descriptionPropertyDefinitionFieldSetToExport(PropertyDefinitionFieldSetEntity entity, String fieldSetId) {
        DescriptionPropertyDefinitionFieldSetImportExport xml = new DescriptionPropertyDefinitionFieldSetImportExport();
        xml.setFieldSetId(fieldSetId);
        if (entity == null) return xml;

        xml.setComment(entity.getComment());
        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            List<DescriptionPropertyDefinitionFieldSetItemImportExport> exports = new LinkedList<>();
            for (PropertyDefinitionFieldSetItemEntity propertyDefinitionFieldSetItemEntity : entity.getItems()) {
                exports.add(this.descriptionPropertyDefinitionFieldSetItemToExport(propertyDefinitionFieldSetItemEntity));
            }
            xml.setItems(exports);
        }

        return xml;
    }

    private DescriptionPropertyDefinitionFieldSetItemImportExport descriptionPropertyDefinitionFieldSetItemToExport(PropertyDefinitionFieldSetItemEntity entity) {
        DescriptionPropertyDefinitionFieldSetItemImportExport xml = new DescriptionPropertyDefinitionFieldSetItemImportExport();
        if (entity == null) return xml;

        xml.setOrdinal(entity.getOrdinal());
        if (entity.getFields() != null && !entity.getFields().isEmpty()) {
            List<DescriptionFieldImportExport> exports = new LinkedList<>();
            for (Map.Entry<String, FieldEntity> fieldSetEntityEntry : entity.getFields().entrySet()) {
                exports.add(this.descriptionFieldImportExportToExport(fieldSetEntityEntry.getValue(), fieldSetEntityEntry.getKey()));
            }
            xml.setFields(exports);
        }

        return xml;
    }

    private DescriptionFieldImportExport descriptionFieldImportExportToExport(FieldEntity entity, String fieldId) {
        DescriptionFieldImportExport xml = new DescriptionFieldImportExport();
        xml.setFieldId(fieldId);
        if (entity == null) return xml;

        xml.setDateValue(entity.getDateValue());
        xml.setBooleanValue(entity.getBooleanValue());
        xml.setTextValue(entity.getTextValue());
        xml.setTextListValue(entity.getTextListValue());
        if (entity.getExternalIdentifier() != null) {
            xml.setExternalIdentifier(this.descriptionExternalIdentifierToExport(entity.getExternalIdentifier()));
        }

        return xml;
    }

    private DescriptionExternalIdentifierImportExport descriptionExternalIdentifierToExport(ExternalIdentifierEntity entity) {
        DescriptionExternalIdentifierImportExport xml = new DescriptionExternalIdentifierImportExport();
        if (entity == null) return xml;

        xml.setIdentifier(entity.getIdentifier());
        xml.setType(entity.getType());

        return xml;
    }

    //endregion


    //region Import xml

    public Description importXml(DescriptionImportExport descriptionXml, UUID planId, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException{

        if (descriptionXml == null) throw new MyNotFoundException("Description xml not found");

       logger.debug(new MapLogEntry("import description").And("planId", planId).And("fields", fields));

        DescriptionPersist persist = new DescriptionPersist();
        persist.setLabel(descriptionXml.getLabel());
        persist.setDescription(descriptionXml.getDescription());
        persist.setStatus(DescriptionStatus.Draft);
        persist.setPlanId(planId);
        persist.setDescriptionTemplateId(this.xmlToDescriptionTemplatePersist(descriptionXml));
        persist.setPlanDescriptionTemplateId(this.xmlToPlanDescriptionTemplatePersist(descriptionXml, planId));
        persist.setTags(descriptionXml.getTags());

        persist.setProperties(this.xmlToPropertyDefinitionToPersist(descriptionXml));

        this.validatorFactory.validator(DescriptionPersist.DescriptionPersistValidator.class).validateForce(persist);

        return this.persist(persist, fields);
    }
    
    private UUID xmlToDescriptionTemplatePersist(DescriptionImportExport descriptionXml) throws JAXBException, InvalidApplicationException, ParserConfigurationException, IOException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        if (descriptionXml.getDescriptionTemplate() != null) {
           
            
            DescriptionTemplateEntity descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(descriptionXml.getDescriptionTemplate().getId()).first();
            if (descriptionTemplateEntity == null) descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(descriptionXml.getDescriptionTemplate().getGroupId()).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).statuses(DescriptionTemplateStatus.Finalized).first();
            UUID descriptionTemplateId;
            if (descriptionTemplateEntity != null){
                descriptionTemplateId = descriptionTemplateEntity.getId();
            } else {
                descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().codes(descriptionXml.getDescriptionTemplate().getCode()).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).statuses(DescriptionTemplateStatus.Finalized).first();
                if (descriptionTemplateEntity == null) {
                    throw new MyValidationException(this.errors.getDescriptionTemplateImportNotFound().getCode(), descriptionXml.getDescriptionTemplate().getCode());
                } else {
                    descriptionTemplateId =  descriptionTemplateEntity.getId();
                }
            }
            
            return descriptionTemplateId;
        }
        return null;
    }

    private UUID xmlToPlanDescriptionTemplatePersist(DescriptionImportExport descriptionXml, UUID planId) {
        if (descriptionXml.getDescriptionTemplate() != null) {
            List<PlanDescriptionTemplateEntity> planDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .planIds(planId)
                    .collect();


            if (!this.conventionService.isListNullOrEmpty(planDescriptionTemplates) && descriptionXml.getSectionId() != null && descriptionXml.getDescriptionTemplate().getGroupId() != null){
                PlanDescriptionTemplateEntity planDescriptionTemplate = planDescriptionTemplates.stream().filter(x ->
                        x.getDescriptionTemplateGroupId().equals(descriptionXml.getDescriptionTemplate().getGroupId()) &&
                                x.getSectionId().equals(descriptionXml.getSectionId())).findFirst().orElse(null);
                if (planDescriptionTemplate == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionXml.getDescriptionTemplate().getGroupId(), PlanDescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                return planDescriptionTemplate.getId();
            }
        }
        return null; 
    }

    private PropertyDefinitionPersist xmlToPropertyDefinitionToPersist(DescriptionImportExport descriptionXml) {
        if (descriptionXml == null)
            return null;

        PropertyDefinitionPersist persist = new PropertyDefinitionPersist();

        Map<String, PropertyDefinitionFieldSetPersist> fieldSetsMap = new HashMap<>();
        if (!this.conventionService.isListNullOrEmpty(descriptionXml.getDescriptionTemplate().getPages())) {
            if (descriptionXml.getProperties() != null && !this.conventionService.isListNullOrEmpty(descriptionXml.getProperties().getFieldSets())){
                for (DescriptionPropertyDefinitionFieldSetImportExport fieldSet: descriptionXml.getProperties().getFieldSets()){
                    fieldSetsMap.put(fieldSet.getFieldSetId(), this.xmlPropertyDefinitionFieldSetToPersist(fieldSet, descriptionXml.getReferences(), descriptionXml.getDescriptionTemplate()));
                }
            }
        }

        persist.setFieldSets(fieldSetsMap);

        return persist;
    }

    private PropertyDefinitionFieldSetPersist xmlPropertyDefinitionFieldSetToPersist(DescriptionPropertyDefinitionFieldSetImportExport importXml, List<DescriptionReferenceImportExport> references, DescriptionTemplateImportExport descriptionTemplate) {

        if (importXml == null)
            return null;

        PropertyDefinitionFieldSetPersist persist = new PropertyDefinitionFieldSetPersist();
        persist.setComment(importXml.getComment());
        if (!this.conventionService.isListNullOrEmpty(importXml.getItems())){
            List<PropertyDefinitionFieldSetItemPersist> items = new ArrayList<>();
            for (DescriptionPropertyDefinitionFieldSetItemImportExport fieldSetItem: importXml.getItems()) {
                items.add(this.xmlPropertyDefinitionFieldSetItemToPersist(fieldSetItem, references, descriptionTemplate));
            }
            persist.setItems(items);
            return persist;

        }
        return null;
    }

    private PropertyDefinitionFieldSetItemPersist xmlPropertyDefinitionFieldSetItemToPersist(DescriptionPropertyDefinitionFieldSetItemImportExport importXml, List<DescriptionReferenceImportExport> references, DescriptionTemplateImportExport descriptionTemplate) {
        if (importXml == null)
            return null;

        PropertyDefinitionFieldSetItemPersist persist = new PropertyDefinitionFieldSetItemPersist();
        persist.setOrdinal(importXml.getOrdinal());

        Map<String, FieldPersist> fields = new HashMap<>();

        if (!this.conventionService.isListNullOrEmpty(importXml.getFields())){
            for (DescriptionFieldImportExport field: importXml.getFields()) {
                fields.put(field.getFieldId(), this.xmlFieldToPersist(field, importXml.getOrdinal(), references, descriptionTemplate));
            }
        }

        persist.setFields(fields);

        return persist;
    }

    private FieldPersist xmlFieldToPersist(DescriptionFieldImportExport importXml, int ordinal, List<DescriptionReferenceImportExport> references, DescriptionTemplateImportExport descriptionTemplate) {
        if (importXml == null || descriptionTemplate == null)
            return null;

        DescriptionTemplateFieldImportExport descriptionTemplateField = descriptionTemplate.getFieldById(importXml.getFieldId()).stream().findFirst().orElse(null);
        if (descriptionTemplateField == null){
            return null;
        }

        FieldPersist persist = new FieldPersist();

        if (!this.conventionService.isListNullOrEmpty(references) && descriptionTemplateField.getData().getFieldType().equals(FieldType.REFERENCE_TYPES)){
            ReferenceTypeDataImportExport referenceTypeDataImportExport = (ReferenceTypeDataImportExport) descriptionTemplateField.getData();
            if (referenceTypeDataImportExport != null){
                ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(referenceTypeDataImportExport.getReferenceTypeId()).first();//TODO: optimize
                if (referenceTypeEntity == null) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).codes(referenceTypeDataImportExport.getReferenceTypeCode()).first();
                if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{referenceTypeDataImportExport.getReferenceTypeCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                List<DescriptionReferenceImportExport> referencesByField = references.stream().filter(x -> x.getOrdinal() == ordinal && x.getFieldId().equals(importXml.getFieldId())).collect(Collectors.toList());
                if (!this.conventionService.isListNullOrEmpty(referencesByField)){
                    if (referenceTypeDataImportExport.getMultipleSelect()){
                        List<ReferencePersist> referencePersists = new ArrayList<>();
                        for (DescriptionReferenceImportExport referenceImportExport: referencesByField) {
                            referencePersists.add(this.xmlDescriptionReferenceToPersist(referenceImportExport, referenceTypeEntity));
                        }
                        persist.setReferences(referencePersists);
                    }else {
                        persist.setReference(this.xmlDescriptionReferenceToPersist(referencesByField.stream().findFirst().orElse(null), referenceTypeEntity));
                    }
                }
            }
        } else {
            persist.setBooleanValue(importXml.getBooleanValue());
            persist.setDateValue(importXml.getDateValue());
            persist.setTextValue(importXml.getTextValue());
            persist.setTextListValue(importXml.getTextListValue());

            if (importXml.getExternalIdentifier() != null){
                persist.setExternalIdentifier(this.xmlExternalIdentifierToPersist(importXml.getExternalIdentifier()));
            }
        }

        return persist;
    }

    private ReferencePersist xmlDescriptionReferenceToPersist(DescriptionReferenceImportExport importXml, ReferenceTypeEntity referenceTypeEntity) {
        if (importXml == null)
            return null;

        if (!referenceTypeEntity.getCode().equals(importXml.getType().getCode())) throw new MyApplicationException("Invalid reference for field " + importXml.getId());

        ReferenceEntity referenceEntity = this.queryFactory.query(ReferenceQuery.class).ids(importXml.getId()).first(); //TODO: optimize
        if (referenceEntity == null) referenceEntity = this.queryFactory.query(ReferenceQuery.class).references(importXml.getReference()).typeIds(referenceTypeEntity.getId()).sources(importXml.getSource()).first();

        ReferencePersist persist = new ReferencePersist();

        persist.setTypeId(referenceTypeEntity.getId());
        if (referenceEntity == null) {
            persist.setLabel(importXml.getLabel());
            persist.setReference(importXml.getReference());
            persist.setSource(importXml.getSource());
            persist.setSourceType(ReferenceSourceType.External);
        } else {
            persist.setId(referenceEntity.getId());
            persist.setLabel(referenceEntity.getLabel());
            persist.setReference(referenceEntity.getReference());
            persist.setSource(referenceEntity.getSource());
            persist.setSourceType(referenceEntity.getSourceType());
            persist.setAbbreviation(referenceEntity.getAbbreviation());
            persist.setDescription(referenceEntity.getDescription());
            persist.setHash(this.conventionService.hashValue(referenceEntity.getUpdatedAt()));
        }
        return persist;
    }

    private ExternalIdentifierPersist xmlExternalIdentifierToPersist(DescriptionExternalIdentifierImportExport importXml) {
        if (importXml == null)
            return null;

        ExternalIdentifierPersist persist = new ExternalIdentifierPersist();

        persist.setType(importXml.getType());
        persist.setIdentifier(importXml.getIdentifier());

        return persist;
    }

    //endregion

    //region Import Common Model 

    @Override
    public Description importCommonModel(DescriptionModel model, UUID planId, FieldSet fields) throws MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException, JAXBException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import description").And("planId", planId).And("fields", fields));
        if (model == null) throw new IllegalArgumentException ("model");
        if (planId == null) throw new IllegalArgumentException ("planId");

        DescriptionPersist persist = new DescriptionPersist();
        persist.setLabel(model.getLabel());
        persist.setDescription(model.getDescription());
        persist.setStatus(DescriptionStatus.Draft);
        persist.setPlanId(planId);
        persist.setDescriptionTemplateId(this.commonModelToDescriptionTemplatePersist(model));
        persist.setPlanDescriptionTemplateId(this.commonModelTToPlanDescriptionTemplatePersist(model, planId));
        persist.setTags(model.getTags());

        persist.setProperties(this.commonModelPropertyDefinitionToPersist(model));

        this.validatorFactory.validator(DescriptionPersist.DescriptionPersistValidator.class).validateForce(persist);

        return this.persist(persist, fields);
    }

    private UUID commonModelToDescriptionTemplatePersist(DescriptionModel commonModel) throws JAXBException, InvalidApplicationException, ParserConfigurationException, IOException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        if (commonModel.getDescriptionTemplate() != null) {
            DescriptionTemplateEntity descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(commonModel.getDescriptionTemplate().getId()).first();
            if (descriptionTemplateEntity == null) descriptionTemplateEntity = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(commonModel.getDescriptionTemplate().getGroupId()).versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).statuses(DescriptionTemplateStatus.Finalized).first();
            UUID descriptionTemplateId;
            if (descriptionTemplateEntity != null){
                descriptionTemplateId = descriptionTemplateEntity.getId();
            } else {
                DescriptionTemplate persisted = this.descriptionTemplateService.importCommonModel(commonModel.getDescriptionTemplate(), new BaseFieldSet().ensure(PlanBlueprint._label).ensure(PlanBlueprint._hash));
                descriptionTemplateId =  persisted.getId();
            }

            return descriptionTemplateId;
        }
        return null;
    }

    private UUID commonModelTToPlanDescriptionTemplatePersist(DescriptionModel commonModel, UUID planId) {
        if (commonModel.getDescriptionTemplate() != null) {
            List<PlanDescriptionTemplateEntity> planDescriptionTemplates = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .planIds(planId)
                    .collect();


            if (!this.conventionService.isListNullOrEmpty(planDescriptionTemplates) && commonModel.getSectionId() != null && commonModel.getDescriptionTemplate().getGroupId() != null){
                PlanDescriptionTemplateEntity planDescriptionTemplate = planDescriptionTemplates.stream().filter(x ->
                        x.getDescriptionTemplateGroupId().equals(commonModel.getDescriptionTemplate().getGroupId()) &&
                                x.getSectionId().equals(commonModel.getSectionId())).findFirst().orElse(null);
                if (planDescriptionTemplate == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{commonModel.getDescriptionTemplate().getGroupId(), PlanDescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                return planDescriptionTemplate.getId();
            }
        }
        return null;
    }


    private PropertyDefinitionPersist commonModelPropertyDefinitionToPersist(DescriptionModel commonModel) {
        if (commonModel == null)
            return null;

        PropertyDefinitionPersist persist = new PropertyDefinitionPersist();

        Map<String, PropertyDefinitionFieldSetPersist> fieldSetsMap = new HashMap<>();
        if (commonModel.getDescriptionTemplate() != null && commonModel.getDescriptionTemplate().getDefinition() != null && !this.conventionService.isListNullOrEmpty(commonModel.getDescriptionTemplate().getDefinition().getPages())) {
            if (commonModel.getProperties() != null && commonModel.getProperties().getFieldSets() != null && !commonModel.getProperties().getFieldSets().isEmpty()){
                for (String fieldSetId: commonModel.getProperties().getFieldSets().keySet()){
                    PropertyDefinitionFieldSetPersist fieldSetPersist = this.commonModelPropertyDefinitionFieldSetToPersist(commonModel.getProperties().getFieldSets().get(fieldSetId), commonModel.getDescriptionTemplate());
                    if (fieldSetPersist != null && !this.conventionService.isListNullOrEmpty(fieldSetPersist.getItems())) fieldSetsMap.put(fieldSetId, fieldSetPersist);
                }
            }
        }

        persist.setFieldSets(fieldSetsMap);

        return persist;
    }

    private PropertyDefinitionFieldSetPersist commonModelPropertyDefinitionFieldSetToPersist(PropertyDefinitionFieldSetModel commonModel, DescriptionTemplateModel descriptionTemplate) {

        if (commonModel == null)
            return null;

        PropertyDefinitionFieldSetPersist persist = new PropertyDefinitionFieldSetPersist();
        persist.setComment(commonModel.getComment());
        if (!this.conventionService.isListNullOrEmpty(commonModel.getItems())){
            List<PropertyDefinitionFieldSetItemPersist> items = new ArrayList<>();
            for (PropertyDefinitionFieldSetItemModel fieldSetItem: commonModel.getItems()) {
                PropertyDefinitionFieldSetItemPersist fieldSetItemPersist = this.commonModelPropertyDefinitionFieldSetItemToPersist(fieldSetItem, descriptionTemplate);
                if (fieldSetItemPersist != null && !fieldSetItemPersist.getFields().isEmpty()) items.add(fieldSetItemPersist);
            }
            persist.setItems(items);
            return persist;

        }
        return null;
    }

    private PropertyDefinitionFieldSetItemPersist commonModelPropertyDefinitionFieldSetItemToPersist(PropertyDefinitionFieldSetItemModel commonModel, DescriptionTemplateModel descriptionTemplate) {
        if (commonModel == null)
            return null;

        PropertyDefinitionFieldSetItemPersist persist = new PropertyDefinitionFieldSetItemPersist();

        persist.setOrdinal(commonModel.getOrdinal());

        Map<String, FieldPersist> fields = new HashMap<>();

        if (commonModel.getFields() != null && !commonModel.getFields().isEmpty()){
            for (String fieldId: commonModel.getFields().keySet()) {
                fields.put(fieldId, this.commonModelFieldToPersist(commonModel.getFields().get(fieldId), descriptionTemplate));
            }
        }

        persist.setFields(fields);

        return persist;
    }

    private FieldPersist commonModelFieldToPersist(FieldModel model, DescriptionTemplateModel descriptionTemplate) {
        if (model == null || descriptionTemplate == null)
            return null;

        org.opencdmp.commonmodels.models.descriptiotemplate.FieldModel descriptionTemplateField = descriptionTemplate.getDefinition().getFieldById(model.getId()).stream().findFirst().orElse(null);
        if (descriptionTemplateField == null){
            return null;
        }

        FieldPersist persist = new FieldPersist();

        if (descriptionTemplateField.getData().getFieldType().equals(org.opencdmp.commonmodels.enums.FieldType.REFERENCE_TYPES)) {
            ReferenceTypeDataModel referenceTypeDataModel = (ReferenceTypeDataModel) descriptionTemplateField.getData();
            if (referenceTypeDataModel != null) {
                if (!this.conventionService.isListNullOrEmpty(model.getReferences())){
                    ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).ids(referenceTypeDataModel.getReferenceType().getId()).first();//TODO: optimize
                    if (referenceTypeEntity == null) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).codes(referenceTypeDataModel.getReferenceType().getCode()).first();
                    if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{referenceTypeDataModel.getReferenceType().getCode(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                    if (referenceTypeDataModel.getMultipleSelect()) {
                        List<ReferencePersist> referencePersists = new ArrayList<>();
                        for (ReferenceModel referenceModel : model.getReferences()) {
                            referencePersists.add(this.commonReferenceToPersist(referenceModel, referenceTypeEntity));
                        }
                        persist.setReferences(referencePersists);
                    } else {
                        persist.setReference(this.commonReferenceToPersist(model.getReferences().stream().findFirst().orElse(null), referenceTypeEntity));
                    }
            }
        }
        } else {
            persist.setBooleanValue(model.getBooleanValue());
            persist.setDateValue(model.getDateValue());
            persist.setTextValue(model.getTextValue());
            persist.setTextListValue(model.getTextListValue());

            if (model.getExternalIdentifier() != null){
                persist.setExternalIdentifier(this.commonModelExternalIdentifierToPersist(model.getExternalIdentifier()));
            }
        }

        return persist;
    }

    private ReferencePersist commonReferenceToPersist(ReferenceModel model, ReferenceTypeEntity referenceTypeEntity) {
        if (model == null)
            return null;

        if (!referenceTypeEntity.getCode().equals(model.getType().getCode())) throw new MyApplicationException("Invalid reference for field " + model.getId());

        ReferenceEntity referenceEntity = model.getId() != null ? this.queryFactory.query(ReferenceQuery.class).ids(model.getId()).first(): null; //TODO: optimize
        if (referenceEntity == null && !this.conventionService.isNullOrEmpty(model.getReference())) {
            List<ReferenceEntity> referenceEntities = this.queryFactory.query(ReferenceQuery.class).references(model.getReference()).typeIds(referenceTypeEntity.getId()).collect();
            if (referenceEntities != null && referenceEntities.size() == 1) referenceEntity = referenceEntities.getFirst();
        }

        ReferencePersist persist = new ReferencePersist();

        persist.setTypeId(referenceTypeEntity.getId());
        if (referenceEntity == null) {
            persist.setReference(!this.conventionService.isNullOrEmpty(model.getReference()) ? model.getReference() : UUID.randomUUID().toString());
            persist.setLabel(!this.conventionService.isNullOrEmpty(model.getLabel()) ? model.getLabel() : persist.getReference());
            persist.setSource("internal");
            persist.setSourceType(ReferenceSourceType.Internal);
        } else {
            persist.setId(referenceEntity.getId());
            persist.setLabel(referenceEntity.getLabel());
            persist.setReference(referenceEntity.getReference());
            persist.setSource(referenceEntity.getSource());
            persist.setSourceType(referenceEntity.getSourceType());
            persist.setAbbreviation(referenceEntity.getAbbreviation());
            persist.setDescription(referenceEntity.getDescription());
            persist.setHash(this.conventionService.hashValue(referenceEntity.getUpdatedAt()));
        }
        return persist;
    }

    private ExternalIdentifierPersist commonModelExternalIdentifierToPersist(ExternalIdentifierModel commonModel) {
        if (commonModel == null)
            return null;

        ExternalIdentifierPersist persist = new ExternalIdentifierPersist();

        persist.setType(commonModel.getType());
        persist.setIdentifier(commonModel.getIdentifier());

        return persist;
    }

    //endregion

}
