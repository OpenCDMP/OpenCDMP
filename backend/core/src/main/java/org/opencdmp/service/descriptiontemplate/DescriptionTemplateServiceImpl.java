package org.opencdmp.service.descriptiontemplate;

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
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commonmodels.models.DescriptionTemplateTypeModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.*;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.descriptiontemplate.*;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.*;
import org.opencdmp.commons.types.descriptiontemplatetype.DescriptionTemplateTypeImportExport;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.importexport.PluginConfigurationImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.deleter.DescriptionTemplateDeleter;
import org.opencdmp.model.deleter.UserDescriptionTemplateDeleter;
import org.opencdmp.model.descriptiontemplate.*;
import org.opencdmp.model.persist.DescriptionTemplatePersist;
import org.opencdmp.model.persist.DescriptionTemplateTypePersist;
import org.opencdmp.model.persist.NewVersionDescriptionTemplatePersist;
import org.opencdmp.model.persist.UserDescriptionTemplatePersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.*;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.BaseFieldDataPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.opencdmp.model.pluginconfiguration.PluginConfiguration;
import org.opencdmp.model.user.User;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
import org.opencdmp.query.UserDescriptionTemplateQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.descriptiontemplatetype.DescriptionTemplateTypeService;
import org.opencdmp.service.fielddatahelper.FieldDataHelperService;
import org.opencdmp.service.fielddatahelper.FieldDataHelperServiceProvider;
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
import java.nio.charset.StandardCharsets;import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DescriptionTemplateServiceImpl implements DescriptionTemplateService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateServiceImpl.class));

    private final TenantEntityManager entityManager;

    private final UserScope userScope;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final MessageSource messageSource;

    private final XmlHandlingService xmlHandlingService;

    private final FieldDataHelperServiceProvider fieldDataHelperServiceProvider;

    private final QueryFactory queryFactory;

    private final ErrorThesaurusProperties errors;

    private final TenantScope tenantScope;

    private final ResponseUtilsService responseUtilsService;


    private final JsonHandlingService jsonHandlingService;

    private final NotifyIntegrationEventHandler eventHandler;

    private final NotificationProperties notificationProperties;

    private final ValidatorFactory validatorFactory;
    private final DescriptionTemplateTypeService descriptionTemplateTypeService;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final LockService lockService;
    private final StorageFileService storageFileService;

    private final PluginConfigurationService pluginConfigurationService;



    @Autowired
    public DescriptionTemplateServiceImpl(
            TenantEntityManager entityManager,
            UserScope userScope, AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            XmlHandlingService xmlHandlingService,
            FieldDataHelperServiceProvider fieldDataHelperServiceProvider,
            QueryFactory queryFactory, ErrorThesaurusProperties errors,
            TenantScope tenantScope,
            ResponseUtilsService responseUtilsService,
            JsonHandlingService jsonHandlingService,
            NotifyIntegrationEventHandler eventHandler,
            NotificationProperties notificationProperties,
            ValidatorFactory validatorFactory, DescriptionTemplateTypeService descriptionTemplateTypeService, AuthorizationContentResolver authorizationContentResolver, UsageLimitService usageLimitService, AccountingService accountingService, LockService lockService, StorageFileService storageFileService, PluginConfigurationService pluginConfigurationService) {
        this.entityManager = entityManager;
        this.userScope = userScope;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
        this.queryFactory = queryFactory;
        this.errors = errors;
        this.tenantScope = tenantScope;
        this.responseUtilsService = responseUtilsService;
        this.jsonHandlingService = jsonHandlingService;
        this.eventHandler = eventHandler;
        this.notificationProperties = notificationProperties;
        this.validatorFactory = validatorFactory;
	    this.descriptionTemplateTypeService = descriptionTemplateTypeService;
	    this.authorizationContentResolver = authorizationContentResolver;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.lockService = lockService;
        this.storageFileService = storageFileService;
        this.pluginConfigurationService = pluginConfigurationService;
    }

    //region Persist

    public DescriptionTemplate persist(DescriptionTemplatePersist model, UUID groupId, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting data descriptionTemplate").And("model", model).And("fields", fields));

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());
        if (isUpdate) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionTemplateAffiliation(model.getId())), Permission.EditDescriptionTemplate);
        else this.authorizationService.authorizeForce(Permission.EditDescriptionTemplate);

        DescriptionTemplateEntity data;
        if (isUpdate) {
            data = this.entityManager.find(DescriptionTemplateEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (this.lockService.isLocked(data.getId(), null).getStatus()) throw new MyApplicationException(this.errors.getLockedDescriptionTemplate().getCode(), this.errors.getLockedDescriptionTemplate().getMessage());
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash()))
                throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            if (data.getStatus().equals(DescriptionTemplateStatus.Finalized))
                throw new MyForbiddenException("Can not update finalized template");
            if (!data.getCode().equals(model.getCode()))
                throw new MyForbiddenException("Code can not change");
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT);

            data = new DescriptionTemplateEntity();
            data.setId(UUID.randomUUID());
            data.setStatus(DescriptionTemplateStatus.Draft);
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            data.setGroupId(groupId != null ? groupId : UUID.randomUUID());
            data.setVersionStatus(DescriptionTemplateVersionStatus.NotFinalized);
            data.setVersion((short) 1);
        }
        if (groupId != null && !data.getGroupId().equals(groupId)) throw new MyApplicationException("Can not change description template group id");

        DescriptionTemplateStatus previousStatus = data.getStatus();

        data.setDescription(model.getDescription());
        data.setLabel(model.getLabel());
        data.setCode(model.getCode());
        data.setTypeId(model.getType());
        data.setLanguage(model.getLanguage());
        data.setStatus(model.getStatus());
        data.setUpdatedAt(Instant.now());

        DefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(data.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(DefinitionEntity.class, data.getDefinition());
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), oldDefinition)));

        if (isUpdate) {
            this.entityManager.merge(data);
            if (previousStatus != null && previousStatus.equals(DescriptionTemplateStatus.Draft) && data.getStatus().equals(DescriptionTemplateStatus.Finalized)) {
                this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());
                this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
            }
        } else {
            this.entityManager.persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue());
            if (data.getStatus().equals(DescriptionTemplateStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
            if (data.getStatus().equals(DescriptionTemplateStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());
        }

        this.persistUsers(data.getId(), model.getUsers());

        this.entityManager.flush();

        if (!isUpdate) {
            long activeDescriptionTemplatesForTheGroup = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .excludedIds(data.getId())
                    .groupIds(data.getGroupId())
                    .count();
            if (activeDescriptionTemplatesForTheGroup > 0) throw new MyApplicationException("Description template group id is in use please use new version endpoint");

            Long descriptionTemplateCodes = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                    .codes(model.getCode())
                    .excludedIds(data.getId())
                    .isActive(IsActive.Active)
                    .count();
            if (descriptionTemplateCodes > 0) throw new MyValidationException(this.errors.getDescriptionTemplateCodeExists().getCode(), this.errors.getDescriptionTemplateCodeExists().getMessage());
        }

        this.updateVersionStatusAndSave(data, previousStatus, data.getStatus());

        this.entityManager.flush();

        return this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, DescriptionTemplate._id), data);
    }

    private void updateVersionStatusAndSave(DescriptionTemplateEntity data, DescriptionTemplateStatus previousStatus, DescriptionTemplateStatus newStatus) throws InvalidApplicationException {
        if (previousStatus.equals(newStatus))
            return;
        if (previousStatus.equals(DescriptionTemplateStatus.Finalized))
            throw new MyForbiddenException("Can not update finalized template");

        if (newStatus.equals(DescriptionTemplateStatus.Finalized)) {
            List<DescriptionTemplateEntity> latestVersionDescriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().versionStatuses(DescriptionTemplateVersionStatus.Current).isActive(IsActive.Active).groupIds(data.getGroupId()).collect();
            if (latestVersionDescriptionTemplates.size() > 1)
                throw new MyValidationException("Multiple previous template found");
            DescriptionTemplateEntity oldDescriptionTemplateEntity = latestVersionDescriptionTemplates.stream().findFirst().orElse(null);

            data.setVersionStatus(DescriptionTemplateVersionStatus.Current);

            if (oldDescriptionTemplateEntity != null) {
                data.setVersion((short) (oldDescriptionTemplateEntity.getVersion() + 1));

                oldDescriptionTemplateEntity.setVersionStatus(DescriptionTemplateVersionStatus.Previous);
                this.entityManager.merge(oldDescriptionTemplateEntity);
            } else {
                data.setVersion((short) 1);
            }
        }

    }

    private void persistUsers(UUID id, List<UserDescriptionTemplatePersist> users) throws InvalidApplicationException {
        if (users == null)
            users = new ArrayList<>();
        List<UserDescriptionTemplateEntity> items = this.queryFactory.query(UserDescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).descriptionTemplateIds(id).collect();
        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (UserDescriptionTemplatePersist user : users) {
            UserDescriptionTemplateEntity data = items.stream().filter(x -> x.getUserId().equals(user.getUserId()) && x.getRole().equals(user.getRole())).findFirst().orElse(null);
            if (data == null) {
                data = new UserDescriptionTemplateEntity();
                data.setId(UUID.randomUUID());
                data.setIsActive(IsActive.Active);
                data.setCreatedAt(Instant.now());
                data.setUpdatedAt(Instant.now());
                data.setDescriptionTemplateId(id);
                data.setUserId(user.getUserId());
                data.setRole(user.getRole());
                this.entityManager.persist(data);
                if (!this.userScope.getUserId().equals(user.getUserId())) {
                    DescriptionTemplateEntity descriptionTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).ids(data.getDescriptionTemplateId()).first();
                    if (descriptionTemplate == null){
                        throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getDescriptionTemplateId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                    }
                    this.sendDescriptionTemplateInvitationEvent(data, descriptionTemplate);
                }
            }
            updatedCreatedIds.add(data.getUserId());
        }
        List<UserDescriptionTemplateEntity> toDelete = items.stream().filter(x -> updatedCreatedIds.stream().noneMatch(y -> y.equals(x.getUserId()))).collect(Collectors.toList());

        this.deleterFactory.deleter(UserDescriptionTemplateDeleter.class).delete(toDelete);
    }

    private void sendDescriptionTemplateInvitationEvent(UserDescriptionTemplateEntity userDescriptionTemplate, DescriptionTemplateEntity descriptionTemplate) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();

        UserEntity user = this.entityManager.find(UserEntity.class, userDescriptionTemplate.getUserId(), true);
        if (user == null){
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{userDescriptionTemplate.getUserId(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        }

        if (user.getIsActive().equals(IsActive.Inactive)) throw new MyValidationException(this.errors.getDescriptionTemplateInactiveUser().getCode(), this.errors.getDescriptionTemplateInactiveUser().getMessage());

        event.setUserId(user.getId());
        event.setNotificationType(this.notificationProperties.getDescriptionTemplateInvitationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{templateName}", DataType.String, descriptionTemplate.getLabel()));
        fieldInfoList.add(new FieldInfo("{templateID}", DataType.String, descriptionTemplate.getId().toString()));
        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
	    this.eventHandler.handle(event);
    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist, DefinitionEntity oldValue) throws InvalidApplicationException {
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null)
            return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getPages())) {
            data.setPages(new ArrayList<>());
            for (PagePersist pagePersist : persist.getPages()) {
                data.getPages().add(this.buildPageEntity(pagePersist));
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
        data.setOrdinal(persist.getOrdinal());
        data.setTitle(persist.getTitle());

        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (SectionPersist sectionPersist : persist.getSections()) {
                data.getSections().add(this.buildSectionEntity(sectionPersist));
            }
        }

        if (!this.conventionService.isListNullOrEmpty(persist.getFieldSets())) {
            data.setFieldSets(new ArrayList<>());
            for (FieldSetPersist fieldSetPersist : persist.getFieldSets()) {
                data.getFieldSets().add(this.buildFieldSetEntity(fieldSetPersist));
            }
        }

        return data;
    }

    private @NotNull FieldSetEntity buildFieldSetEntity(FieldSetPersist persist) {
        FieldSetEntity data = new FieldSetEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setDescription(persist.getDescription());
        data.setOrdinal(persist.getOrdinal());
        data.setTitle(persist.getTitle());
        data.setHasMultiplicity(persist.getHasMultiplicity());
        data.setAdditionalInformation(persist.getAdditionalInformation());
        data.setExtendedDescription(persist.getExtendedDescription());
        if (persist.getMultiplicity() != null && persist.getHasMultiplicity()) {
            data.setMultiplicity(this.buildMultiplicityEntity(persist.getMultiplicity()));
        }
        data.setHasCommentField(persist.getHasCommentField());

        if (!this.conventionService.isListNullOrEmpty(persist.getFields())) {
            data.setFields(new ArrayList<>());
            for (FieldPersist fieldPersist : persist.getFields()) {
                data.getFields().add(this.buildFieldEntity(fieldPersist));
            }
        }
        return data;
    }

    private @NotNull FieldEntity buildFieldEntity(FieldPersist persist) {
        FieldEntity data = new FieldEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setOrdinal(persist.getOrdinal());
        data.setSemantics(persist.getSemantics());
        //data.setNumbering(persist.get()); //TODO
        data.setValidations(persist.getValidations());
        data.setIncludeInExport(persist.getIncludeInExport());
        if (persist.getData() != null)
            data.setData(this.buildFieldDataEntity(persist.getData()));

        data.setDefaultValue(this.buildDefaultValueEntity(persist.getDefaultValue(), data));
        if (!this.conventionService.isListNullOrEmpty(persist.getVisibilityRules())) {
            data.setVisibilityRules(new ArrayList<>());
            for (RulePersist fieldPersist : persist.getVisibilityRules()) {
                data.getVisibilityRules().add(this.buildRuleEntity(fieldPersist, data));
            }
        }
        return data;
    }

    private BaseFieldDataEntity buildFieldDataEntity(BaseFieldDataPersist persist) {
        if (persist == null)
            return null;
        return this.fieldDataHelperServiceProvider.get(persist.getFieldType()).applyPersist(persist);
    }

    private @NotNull RuleEntity buildRuleEntity(RulePersist persist, FieldEntity fieldEntity) {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        RuleEntity data = new RuleEntity();
        if (persist == null) return data;
        data.setTarget(persist.getTarget());

        if (FieldType.isTextType(fieldType) || FieldType.isTextListType(fieldType)) {
            if (FieldType.UPLOAD.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("Upload not supported");
            if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue()))  throw new NotImplementedException("plans not supported");
            if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("descriptions not supported");
            
            data.setTextValue(persist.getTextValue());
        }
        else if (FieldType.isReferenceType(fieldType) ) {
            throw new NotImplementedException("reference not supported");
        }  
        else if (FieldType.isTagType(fieldType) ) {
            throw new NotImplementedException("tags not supported");
        }
        else if (FieldType.isDateType(fieldType))  data.setDateValue(persist.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  data.setBooleanValue(persist.getBooleanValue());
        else if (FieldType.isExternalIdentifierType(fieldType)) throw new NotImplementedException("ExternalIdentifier not supported");

        return data;
    }

    private @NotNull DefaultValueEntity buildDefaultValueEntity(DefaultValuePersist persist, FieldEntity fieldEntity)  {
        FieldType fieldType = fieldEntity != null && fieldEntity.getData() != null ? fieldEntity.getData().getFieldType() :  FieldType.FREE_TEXT;
        DefaultValueEntity data = new DefaultValueEntity();
        if (persist == null || persist.isNullOrEmpty()) return data;

        if (FieldType.isTextType(fieldType) || FieldType.isTextListType(fieldType)) {
            if (FieldType.UPLOAD.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("Upload not supported");
            if (FieldType.INTERNAL_ENTRIES_PLANS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue()))  throw new NotImplementedException("plans not supported");
            if (FieldType.INTERNAL_ENTRIES_DESCRIPTIONS.equals(fieldType) && !this.conventionService.isNullOrEmpty(persist.getTextValue())) throw new NotImplementedException("descriptions not supported");

            data.setTextValue(persist.getTextValue());
        }
        else if (FieldType.isReferenceType(fieldType) ) {
            throw new NotImplementedException("reference not supported");
        }
        else if (FieldType.isTagType(fieldType) ) {
            throw new NotImplementedException("tags not supported");
        }
        else if (FieldType.isDateType(fieldType))  data.setDateValue(persist.getDateValue());
        else if (FieldType.isBooleanType(fieldType))  data.setBooleanValue(persist.getBooleanValue());
        else if (FieldType.isExternalIdentifierType(fieldType)){
            throw new NotImplementedException("external identifier not supported");
        }

        return data;
    }

    private @NotNull MultiplicityEntity buildMultiplicityEntity(MultiplicityPersist persist) {
        MultiplicityEntity data = new MultiplicityEntity();
        if (persist == null)
            return data;

        if (persist.getMax() != null)
            data.setMax(persist.getMax());
        if (persist.getMin() != null)
            data.setMin(persist.getMin());
        if (persist.getPlaceholder() != null)
            data.setPlaceholder(persist.getPlaceholder());
        if (persist.getTableView() != null)
            data.setTableView(persist.getTableView());
        return data;
    }

    private @NotNull PageEntity buildPageEntity(PagePersist persist) {
        PageEntity data = new PageEntity();
        if (persist == null)
            return data;

        data.setId(persist.getId());
        data.setOrdinal(persist.getOrdinal());
        data.setTitle(persist.getTitle());

        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (SectionPersist sectionPersist : persist.getSections()) {
                data.getSections().add(this.buildSectionEntity(sectionPersist));
            }
        }
        return data;
    }

    //endregion 

    //region Delete

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting dataset: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteDescriptionTemplate);

        DescriptionTemplateEntity data = this.entityManager.find(DescriptionTemplateEntity.class, id);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (data.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)){
            DescriptionTemplateQuery descriptionTemplateQuery = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                    .statuses(DescriptionTemplateStatus.Finalized)
                    .excludedIds(data.getId())
                    .isActive(IsActive.Active)
                    .groupIds(data.getGroupId());

            descriptionTemplateQuery.setOrder(new Ordering().addDescending(DescriptionTemplate._version));
            DescriptionTemplateEntity previousFinalized = descriptionTemplateQuery.first();
            if (previousFinalized != null){
                previousFinalized.setVersionStatus(DescriptionTemplateVersionStatus.Current);
                this.entityManager.merge(previousFinalized);
                data.setVersionStatus(DescriptionTemplateVersionStatus.NotFinalized);
            }
            this.entityManager.merge(data);
            this.entityManager.flush();
        }
        
        this.deleterFactory.deleter(DescriptionTemplateDeleter.class).deleteAndSaveByIds(List.of(id));
    }

    //endregion 

    //region Clone

    public DescriptionTemplate buildClone(UUID id, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException {
        logger.debug(new MapLogEntry("persisting data").And("id", id).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.CloneDescriptionTemplate);

        DescriptionTemplateQuery query = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        DescriptionTemplate model = this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fields, query.firstAs(fields));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        model.setLabel(model.getLabel() + " new ");
        model.setCode(model.getCode());
        model.setId(null);
        model.setHash(null);
        model.setStatus(DescriptionTemplateStatus.Draft);
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
        if (model.getPages() != null) {
            for (Page page : model.getPages()) {
                this.reassignPage(page, visibilityRulesMap);
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
    }

    private void reassignPage(Page model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;
        model.setId(UUID.randomUUID().toString());

        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                this.reassignSection(section, visibilityRulesMap);
            }
        }

    }

    private void reassignSection(Section model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;
        model.setId(UUID.randomUUID().toString());

        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
                this.reassignSection(section, visibilityRulesMap);
            }
        }
        if (model.getFieldSets() != null) {
            for (org.opencdmp.model.descriptiontemplate.FieldSet fieldSet : model.getFieldSets()) {
                this.reassignFieldSet(fieldSet, visibilityRulesMap);
            }
        }
    }

    private void reassignFieldSet(org.opencdmp.model.descriptiontemplate.FieldSet model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;

        String oldFieldSetId = model.getId();
        if (visibilityRulesMap != null && visibilityRulesMap.containsKey(oldFieldSetId)){
            model.setId(UUID.randomUUID().toString());
            visibilityRulesMap.put(oldFieldSetId, model.getId());
        } else {
            model.setId(UUID.randomUUID().toString());
        }

        if (model.getFields() != null) {
            for (Field field : model.getFields()) {
                this.reassignField(field, visibilityRulesMap);
            }
        }
    }

    private void reassignField(Field model, Map<String, String> visibilityRulesMap) {
        if (model == null)
            return;
        String oldFieldId = model.getId();
        if (visibilityRulesMap != null && visibilityRulesMap.containsKey(oldFieldId)){
            model.setId(UUID.randomUUID().toString());
            visibilityRulesMap.put(oldFieldId, model.getId());
        } else {
            model.setId(UUID.randomUUID().toString());
        }
    }

    private List<Field> getAllFieldByDefinition(Definition definition){
        List<Field> fields = new ArrayList<>();
        if (definition == null) return  fields;

        if (definition.getPages() != null){
            for (Page page: definition.getPages()) {
                fields.addAll(this.getAllFieldBySections(page.getSections()));
            }
        }
        return fields;
    }


    private List<Field> getAllFieldBySections(List<Section> sections){
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

    public DescriptionTemplate createNewVersion(NewVersionDescriptionTemplatePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting data descriptionTemplate").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.CreateNewVersionDescriptionTemplate);

        this.usageLimitService.checkIncrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT);

        DescriptionTemplateEntity oldDescriptionTemplateEntity = this.entityManager.find(DescriptionTemplateEntity.class, model.getId(), true);
        if (oldDescriptionTemplateEntity == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(oldDescriptionTemplateEntity.getUpdatedAt()).equals(model.getHash()))
            throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());

        if (!this.tenantScope.isSet() || !Objects.equals(oldDescriptionTemplateEntity.getTenantId(), this.tenantScope.getTenant())) throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
        
        List<DescriptionTemplateEntity> latestVersionDescriptionTemplates = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                .versionStatuses(DescriptionTemplateVersionStatus.Current)
                .isActive(IsActive.Active)
                .groupIds(oldDescriptionTemplateEntity.getGroupId())
                .collect();
        if (latestVersionDescriptionTemplates.isEmpty())
            throw new MyValidationException(this.errors.getDescriptionTemplateIsNotFinalized().getCode(), this.errors.getDescriptionTemplateIsNotFinalized().getMessage());
        if (latestVersionDescriptionTemplates.size() > 1)
            throw new MyValidationException(this.errors.getMultiplePlanVersionsNotSupported().getCode(), this.errors.getMultiplePlanVersionsNotSupported().getMessage());
        if (!latestVersionDescriptionTemplates.getFirst().getVersion().equals(oldDescriptionTemplateEntity.getVersion()))
            throw new MyValidationException(this.errors.getDescriptionTemplateNewVersionConflict().getCode(), this.errors.getDescriptionTemplateNewVersionConflict().getMessage());
        Long notFinalizedCount = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                .versionStatuses(DescriptionTemplateVersionStatus.NotFinalized)
                .groupIds(oldDescriptionTemplateEntity.getGroupId())
                .isActive(IsActive.Active)
                .count();
        if (notFinalizedCount > 0)
            throw new MyValidationException(this.errors.getDescriptionTemplateNewVersionAlreadyCreatedDraft().getCode(), this.errors.getDescriptionTemplateNewVersionAlreadyCreatedDraft().getMessage());

        DescriptionTemplateEntity data = new DescriptionTemplateEntity();
        data.setId(UUID.randomUUID());
        data.setIsActive(IsActive.Active);
        data.setCreatedAt(Instant.now());
        data.setUpdatedAt(Instant.now());
        data.setVersionStatus(DescriptionTemplateVersionStatus.NotFinalized);
        data.setGroupId(oldDescriptionTemplateEntity.getGroupId());
        data.setVersion((short) (oldDescriptionTemplateEntity.getVersion() + 1));
        data.setDescription(model.getDescription());
        data.setLabel(model.getLabel());
        data.setCode(oldDescriptionTemplateEntity.getCode());
        data.setTypeId(model.getType());
        data.setLanguage(model.getLanguage());
        data.setStatus(model.getStatus());

        if (!this.conventionService.isListNullOrEmpty(model.getDefinition().getPluginConfigurations())) {
            org.opencdmp.commons.types.planblueprint.DefinitionEntity oldDefinition = this.conventionService.isNullOrEmpty(oldDescriptionTemplateEntity.getDefinition()) ? null : this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, oldDescriptionTemplateEntity.getDefinition());
            if (oldDefinition != null && !this.conventionService.isListNullOrEmpty(oldDefinition.getPluginConfigurations())) {
                // reassign new storage files if equals with old
                this.pluginConfigurationService.reassignNewStorageFilesIfEqualsWithOld(model.getDefinition().getPluginConfigurations(), oldDefinition.getPluginConfigurations());
            }
        }
        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), null)));

        this.entityManager.persist(data);

        this.persistUsers(data.getId(), model.getUsers());
        //this.addOwner(data);

        this.entityManager.flush();

        this.updateVersionStatusAndSave(data, DescriptionTemplateStatus.Draft, data.getStatus());

        this.entityManager.flush();

        this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue());
        if (data.getStatus().equals(DescriptionTemplateStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
        if (data.getStatus().equals(DescriptionTemplateStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());

        return this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, DescriptionTemplate._id), data);
    }

    //endregion

    //region Import

    @Override
    public DescriptionTemplate importXml(DescriptionTemplateImportExport importXml, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("importXml", importXml).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ImportDescriptionTemplate);

        if (groupId == null) groupId = importXml.getGroupId();

        long activeDescriptionTemplatesForTheGroup = groupId != null ? this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                .isActive(IsActive.Active)
                .groupIds(groupId)
                .count() : 0;

        if (activeDescriptionTemplatesForTheGroup == 0 && !this.conventionService.isNullOrEmpty(importXml.getCode())) {
            activeDescriptionTemplatesForTheGroup = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                    .isActive(IsActive.Active)
                    .codes(importXml.getCode())
                    .count();
        }

        if (activeDescriptionTemplatesForTheGroup == 0) {
            DescriptionTemplatePersist persist = new DescriptionTemplatePersist();
            persist.setLabel(label);
            persist.setCode(importXml.getCode());
            persist.setStatus(DescriptionTemplateStatus.Draft);
            persist.setDescription(importXml.getDescription());
            persist.setLanguage(importXml.getLanguage());
            persist.setType(this.xmlDescriptionTemplateTypeToPersist(importXml.getDescriptionTemplateType(), label));
            persist.setDefinition(this.xmlDefinitionToPersist(importXml));
            this.validatorFactory.validator(DescriptionTemplatePersist.DescriptionTemplatePersistValidator.class).validateForce(persist);
            return this.persist(persist, groupId, fields);
        } else {
            DescriptionTemplateEntity latestVersionDescriptionTemplate = null;
            if (groupId != null) {
                latestVersionDescriptionTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                        .versionStatuses(DescriptionTemplateVersionStatus.Current)
                        .isActive(IsActive.Active)
                        .statuses(DescriptionTemplateStatus.Finalized)
                        .groupIds(groupId)
                        .first();
            }
            if (latestVersionDescriptionTemplate == null && !this.conventionService.isNullOrEmpty(importXml.getCode())) {
                latestVersionDescriptionTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                        .versionStatuses(DescriptionTemplateVersionStatus.Current)
                        .isActive(IsActive.Active)
                        .statuses(DescriptionTemplateStatus.Finalized)
                        .codes(importXml.getCode())
                        .first();
            }
            if (latestVersionDescriptionTemplate == null) throw new MyValidationException(this.errors.getDescriptionTemplateIsNotFinalized().getCode(), this.errors.getDescriptionTemplateIsNotFinalized().getMessage());

            NewVersionDescriptionTemplatePersist persist = new NewVersionDescriptionTemplatePersist();
            persist.setId(latestVersionDescriptionTemplate.getId());
            persist.setLabel(label);
            persist.setStatus(DescriptionTemplateStatus.Draft);
            persist.setDescription(importXml.getDescription());
            persist.setLanguage(importXml.getLanguage());
            persist.setDefinition(this.xmlDefinitionToPersist(importXml));
            persist.setType(this.xmlDescriptionTemplateTypeToPersist(importXml.getDescriptionTemplateType(), label));
            persist.setHash(this.conventionService.hashValue(latestVersionDescriptionTemplate.getUpdatedAt()));

            this.validatorFactory.validator(NewVersionDescriptionTemplatePersist.NewVersionDescriptionTemplatePersistValidator.class).validateForce(persist);
            this.accountingService.increase(UsageLimitTargetMetric.IMPORT_DESCRIPTION_TEMPLATE_XML_EXECUTION_COUNT.getValue());
            return this.createNewVersion(persist, fields);
        }
    }

    private UUID xmlDescriptionTemplateTypeToPersist(DescriptionTemplateTypeImportExport importXml, String label) {
        if (importXml == null) return null;

        DescriptionTemplateTypeQuery query = null;

        if (importXml.getId() != null) {
            // search by id
            DescriptionTemplateTypeEntity entity = this.queryFactory.query(DescriptionTemplateTypeQuery.class).ids(importXml.getId()).firstAs(new BaseFieldSet().ensure(DescriptionTemplateType._id).ensure(DescriptionTemplateType._code).ensure(DescriptionTemplateType._status));
            if (entity != null ) {
                if (!entity.getStatus().equals(DescriptionTemplateTypeStatus.Finalized)) throw new MyValidationException(this.errors.getDescriptionTemplateTypeImportDraft().getCode(), entity.getCode());
                return entity.getId();
            } else {
                if (!this.conventionService.isNullOrEmpty(importXml.getCode())){
                    // search by code
                    entity = this.queryFactory.query(DescriptionTemplateTypeQuery.class).codes(importXml.getCode()).isActive(IsActive.Active).firstAs(new BaseFieldSet().ensure(DescriptionTemplateType._id).ensure(DescriptionTemplateType._code).ensure(DescriptionTemplateType._status));
                    if (entity != null) {
                        if (!entity.getStatus().equals(DescriptionTemplateTypeStatus.Finalized)) throw new MyValidationException(this.errors.getDescriptionTemplateTypeImportDraft().getCode(), entity.getCode());
                        return entity.getId();
                    }
                }

            }
        }
        throw new MyValidationException(this.errors.getDescriptionTemplateTypeImportNotFound().getCode(), importXml.getCode());
    }

    @Override
    public DescriptionTemplate importXml(byte[] bytes, UUID groupId, String label, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import data").And("bytes", bytes).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ImportDescriptionTemplate);

        DescriptionTemplateImportExport importXml = this.xmlHandlingService.fromXmlSafe(DescriptionTemplateImportExport.class, new String(bytes, StandardCharsets.UTF_8));

        if (importXml == null) {
            logger.warn("Description template import xml failed. Input: " + new String(bytes, StandardCharsets.UTF_8));
            throw new MyApplicationException(this.errors.getInvalidDescriptionTemplateImportXml().getCode(), this.errors.getInvalidDescriptionTemplateImportXml().getMessage());
        }
        return this.importXml(importXml, groupId, label, fields);
    }

    private DefinitionPersist xmlDefinitionToPersist(DescriptionTemplateImportExport importExport) throws IOException {
        DefinitionPersist definitionPersist = new DefinitionPersist();
        if (importExport == null)
            return null;

        List<PagePersist> pagesDatasetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getPages())) {
            for (DescriptionTemplatePageImportExport xmlPage : importExport.getPages()) {
                pagesDatasetEntity.add(this.xmlPageToPersist(xmlPage));
            }
        }
        definitionPersist.setPages(pagesDatasetEntity);

        List<PluginConfigurationPersist> pluginConfigurations = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getPluginConfigurations())) {
            for (PluginConfigurationImportExport pluginConfiguration : importExport.getPluginConfigurations()) {
                pluginConfigurations.add(this.pluginConfigurationService.xmlPluginConfigurationToPersist(pluginConfiguration));
            }
        }
        definitionPersist.setPluginConfigurations(pluginConfigurations);

        return definitionPersist;
    }

    private PagePersist xmlPageToPersist(DescriptionTemplatePageImportExport importExport) {
        PagePersist pageEntity = new PagePersist();
        pageEntity.setId(importExport.getId());
        pageEntity.setOrdinal(importExport.getOrdinal());
        pageEntity.setTitle(importExport.getTitle());
        if (!this.conventionService.isListNullOrEmpty(importExport.getSections())) {
            List<SectionPersist> sectionsListEntity = new LinkedList<>();
            for (DescriptionTemplateSectionImportExport xmlSection : importExport.getSections()) {
                sectionsListEntity.add(this.xmlSectionToPersist(xmlSection));
            }
            pageEntity.setSections(sectionsListEntity);

        }
        return pageEntity;
    }

    private SectionPersist xmlSectionToPersist(DescriptionTemplateSectionImportExport importExport) {
        SectionPersist sectionEntity = new SectionPersist();
        List<SectionPersist> sectionsListEntity = new LinkedList<>();

        if (!this.conventionService.isListNullOrEmpty(importExport.getSections())) {
            for (DescriptionTemplateSectionImportExport xmlSection : importExport.getSections()) {
                sectionsListEntity.add(this.xmlSectionToPersist(xmlSection));
            }
        }
        sectionEntity.setId(importExport.getId());
        sectionEntity.setOrdinal(importExport.getOrdinal());
        sectionEntity.setTitle(importExport.getTitle());
        sectionEntity.setDescription(importExport.getDescription());
        List<FieldSetPersist> fieldSetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getFieldSets())) {
            for (DescriptionTemplateFieldSetImportExport xmlFieldSet : importExport.getFieldSets()) {
                fieldSetEntity.add(this.toFieldSetModel(xmlFieldSet));
            }
        }
        sectionEntity.setFieldSets(fieldSetEntity);

        sectionEntity.setSections(sectionsListEntity);
        return sectionEntity;
    }

    private FieldSetPersist toFieldSetModel(DescriptionTemplateFieldSetImportExport importExport) {
        FieldSetPersist fieldSet1Entity = new FieldSetPersist();
        fieldSet1Entity.setId(importExport.getId());
        fieldSet1Entity.setOrdinal(importExport.getOrdinal());
        fieldSet1Entity.setHasCommentField(importExport.getHasCommentField() != null ? importExport.getHasCommentField() : false);
        fieldSet1Entity.setMultiplicity(importExport.getMultiplicity() != null ? this.xmlMultiplicityToPersist(importExport.getMultiplicity()) : null);
        fieldSet1Entity.setHasMultiplicity(importExport.getHasMultiplicity());
        fieldSet1Entity.setTitle(importExport.getTitle());
        fieldSet1Entity.setDescription(importExport.getDescription());
        fieldSet1Entity.setExtendedDescription(importExport.getExtendedDescription());
        fieldSet1Entity.setAdditionalInformation(importExport.getAdditionalInformation());

        List<FieldPersist> fieldsEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getFields())) {
            for (DescriptionTemplateFieldImportExport xmlField : importExport.getFields()) {
                fieldsEntity.add(this.xmlFieldToPersist(xmlField));
            }
        }
        fieldSet1Entity.setFields(fieldsEntity);
        return fieldSet1Entity;
    }

    private FieldPersist xmlFieldToPersist(DescriptionTemplateFieldImportExport importExport) {
        FieldPersist fieldEntity = new FieldPersist();
        fieldEntity.setId(importExport.getId());
        fieldEntity.setOrdinal(importExport.getOrdinal());
        fieldEntity.setValidations(importExport.getValidations());
        fieldEntity.setIncludeInExport(importExport.getIncludeInExport());
        fieldEntity.setDefaultValue(this.toRuleModel(importExport.getDefaultValue()));
        List<RulePersist> rulePersists = new ArrayList<>();
        if (importExport.getVisibilityRules() != null) {
            for (DescriptionTemplateRuleImportExport xmlRule : importExport.getVisibilityRules()) {
                rulePersists.add(this.toRuleModel(xmlRule));
            }
        }
        fieldEntity.setVisibilityRules(rulePersists);
        FieldType fieldType = importExport.getData().getFieldType();

        if (importExport.getData() != null) {
            FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(fieldType);
            fieldEntity.setData(fieldDataHelperService.importExportMapDataToPersist(importExport.getData()));
        }
        fieldEntity.setSemantics(importExport.getSemantics());
        return fieldEntity;
    }

    private RulePersist toRuleModel(DescriptionTemplateRuleImportExport importExport) {
        RulePersist ruleEntity = new RulePersist();
        ruleEntity.setTarget(importExport.getTarget());
        ruleEntity.setDateValue(importExport.getDateValue());
        ruleEntity.setBooleanValue(importExport.getBooleanValue());
        //ruleEntity.setReferences(importExport.get()); //TODO
        ruleEntity.setTextValue(importExport.getTextValue());
        return ruleEntity;
    }

    private DefaultValuePersist toRuleModel(DescriptionTemplateDefaultValueImportExport importExport) {
        if (importExport == null) return null;
        DefaultValuePersist ruleEntity = new DefaultValuePersist();
        ruleEntity.setDateValue(importExport.getDateValue());
        ruleEntity.setBooleanValue(importExport.getBooleanValue());
        ruleEntity.setTextValue(importExport.getTextValue());
        return ruleEntity;
    }

    private MultiplicityPersist xmlMultiplicityToPersist(DescriptionTemplateMultiplicityImportExport importXml) {
        MultiplicityPersist multiplicityEntity = new MultiplicityPersist();
        multiplicityEntity.setMax(importXml.getMax());
        multiplicityEntity.setMin(importXml.getMin());
        multiplicityEntity.setPlaceholder(importXml.getPlaceholder());
        multiplicityEntity.setTableView(importXml.getTableView());
        return multiplicityEntity;
    }

    //endregion

    //region Export

    @Override
    public DescriptionTemplateImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("exportXml").And("id", id));

        if (!ignoreAuthorize) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionTemplateAffiliation(id)), Permission.ExportDescriptionTemplate);
        DescriptionTemplateEntity data = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.AllExceptPublic).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DefinitionEntity definition = this.xmlHandlingService.fromXml(DefinitionEntity.class, data.getDefinition());
        return this.definitionXmlToExport(data, definition);
    }


    @Override
    public ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("exportXml").And("id", id));

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionTemplateAffiliation(id)), Permission.ExportDescriptionTemplate);

        DescriptionTemplateEntity data = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.AllExceptPublic).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String xml = this.xmlHandlingService.toXml(this.exportXmlEntity(id, false));
        this.accountingService.increase(UsageLimitTargetMetric.EXPORT_DESCRIPTION_TEMPLATE_XML_EXECUTION_COUNT.getValue());
        return this.responseUtilsService.buildResponseFileFromText(xml, data.getLabel() + ".xml");
    }

    private DescriptionTemplateImportExport definitionXmlToExport(DescriptionTemplateEntity data, DefinitionEntity entity) throws InvalidApplicationException {
        DescriptionTemplateImportExport xml = new DescriptionTemplateImportExport();
        xml.setId(data.getId());
        xml.setLabel(data.getLabel());
        xml.setCode(data.getCode());
        xml.setDescriptionTemplateType(this.descriptionTemplateTypeXmlToExport(data.getTypeId()));
        xml.setLanguage(data.getLanguage());
        xml.setDescription(data.getDescription());
        xml.setGroupId(data.getGroupId());
        xml.setVersion(data.getVersion());

        List<DescriptionTemplatePageImportExport> pagesDatasetEntity = new LinkedList<>();
        for (PageEntity xmlPage : entity.getPages()) {
            pagesDatasetEntity.add(this.pageXmlToExport(xmlPage));
        }
        xml.setPages(pagesDatasetEntity);

        List<PluginConfigurationImportExport> pluginConfigurations = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getPluginConfigurations())) {
            for (PluginConfigurationEntity pluginConfiguration : entity.getPluginConfigurations()) {
                pluginConfigurations.add(this.pluginConfigurationService.pluginConfigurationXmlToExport(pluginConfiguration));
            }
            xml.setPluginConfigurations(pluginConfigurations);
        }
        return xml;
    }

    private DescriptionTemplateTypeImportExport descriptionTemplateTypeXmlToExport(UUID typeId) throws InvalidApplicationException {
        DescriptionTemplateTypeImportExport xml = new DescriptionTemplateTypeImportExport();

        if (typeId == null) return xml;

        DescriptionTemplateTypeEntity data = this.entityManager.find(DescriptionTemplateTypeEntity.class, typeId);
        if (data == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{typeId, DescriptionTemplateType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        xml.setId(typeId);
        xml.setName(data.getName());
        xml.setCode(data.getCode());

        return xml;
    }

    private DescriptionTemplatePageImportExport pageXmlToExport(PageEntity entity) {
        DescriptionTemplatePageImportExport xml = new DescriptionTemplatePageImportExport();
        xml.setId(entity.getId());
        xml.setOrdinal(entity.getOrdinal());
        xml.setTitle(entity.getTitle());
        List<DescriptionTemplateSectionImportExport> sectionsListEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getSections())) {
            for (SectionEntity section : entity.getSections()) {
                sectionsListEntity.add(this.sectionXmlToExport(section));
            }
        }
        xml.setSections(sectionsListEntity);

        return xml;
    }

    private DescriptionTemplateSectionImportExport sectionXmlToExport(SectionEntity entity) {
        DescriptionTemplateSectionImportExport xml = new DescriptionTemplateSectionImportExport();
        List<DescriptionTemplateSectionImportExport> sectionsListEntity = new LinkedList<>();

        if (!this.conventionService.isListNullOrEmpty(entity.getSections())) {
            for (SectionEntity xmlSection : entity.getSections()) {
                sectionsListEntity.add(this.sectionXmlToExport(xmlSection));
            }
        }
        xml.setSections(sectionsListEntity);

        xml.setId(entity.getId());
        xml.setOrdinal(entity.getOrdinal());
        xml.setTitle(entity.getTitle());
        xml.setDescription(entity.getDescription());
        List<DescriptionTemplateFieldSetImportExport> fieldSetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getFieldSets())) {
            for (FieldSetEntity xmlFieldSet : entity.getFieldSets()) {
                fieldSetEntity.add(this.fieldSetXmlToExport(xmlFieldSet));
            }
        }
        xml.setFieldSets(fieldSetEntity);

        return xml;
    }

    private DescriptionTemplateFieldSetImportExport fieldSetXmlToExport(FieldSetEntity entity) {
        DescriptionTemplateFieldSetImportExport fieldSet1Entity = new DescriptionTemplateFieldSetImportExport();
        fieldSet1Entity.setId(entity.getId());
        fieldSet1Entity.setOrdinal(entity.getOrdinal());
        fieldSet1Entity.setHasCommentField(entity.getHasCommentField());
        fieldSet1Entity.setHasMultiplicity(entity.getHasMultiplicity());
        fieldSet1Entity.setMultiplicity(entity.getMultiplicity() != null ? this.multiplicityXmlToExport(entity.getMultiplicity()) : null);
        fieldSet1Entity.setTitle(entity.getTitle());
        fieldSet1Entity.setDescription(entity.getDescription());
        fieldSet1Entity.setExtendedDescription(entity.getExtendedDescription());
        fieldSet1Entity.setAdditionalInformation(entity.getAdditionalInformation());

        List<DescriptionTemplateFieldImportExport> fieldsEntity = new LinkedList<>();
        if (entity.getFields() != null) {
            for (FieldEntity xmlField : entity.getFields()) {
                fieldsEntity.add(this.fieldXmlToExport(xmlField));
            }
        }
        fieldSet1Entity.setFields(fieldsEntity);
        return fieldSet1Entity;
    }

    private DescriptionTemplateFieldImportExport fieldXmlToExport(FieldEntity entity) {
        DescriptionTemplateFieldImportExport xml = new DescriptionTemplateFieldImportExport();
        xml.setId(entity.getId());
        xml.setOrdinal(entity.getOrdinal());
        xml.setValidations(entity.getValidations());
        xml.setDefaultValue(this.toDefaultValueModel(entity.getDefaultValue()));
        xml.setIncludeInExport(entity.getIncludeInExport());
        List<DescriptionTemplateRuleImportExport> rulePersists = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(entity.getVisibilityRules())) {
            for (RuleEntity xmlRule : entity.getVisibilityRules()) {
                rulePersists.add(this.toRuleModel(xmlRule));
            }
        }
        xml.setVisibilityRules(rulePersists);

        if (entity.getData() != null) {
            FieldType fieldType = entity.getData().getFieldType();
            FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(fieldType);
            xml.setData(fieldDataHelperService.dataToImportExportXml(entity.getData()));
        }
        xml.setSemantics(entity.getSemantics());
        return xml;
    }

    private DescriptionTemplateDefaultValueImportExport toDefaultValueModel(DefaultValueEntity entity) {
        DescriptionTemplateDefaultValueImportExport xml = new DescriptionTemplateDefaultValueImportExport();

        if (entity == null) return xml;

        xml.setDateValue(entity.getDateValue());
        xml.setBooleanValue(entity.getBooleanValue());
        xml.setTextValue(entity.getTextValue());
        return xml;
    }

    private DescriptionTemplateRuleImportExport toRuleModel(RuleEntity entity) {
        DescriptionTemplateRuleImportExport xml = new DescriptionTemplateRuleImportExport();

        if (entity == null) return xml;

        xml.setTarget(entity.getTarget());
        xml.setDateValue(entity.getDateValue());
        xml.setBooleanValue(entity.getBooleanValue());
        xml.setTextValue(entity.getTextValue());
        return xml;
    }

    private DescriptionTemplateMultiplicityImportExport multiplicityXmlToExport(MultiplicityEntity entity) {
        DescriptionTemplateMultiplicityImportExport xml = new DescriptionTemplateMultiplicityImportExport();

        if (entity == null) return xml;

        xml.setMax(entity.getMax());
        xml.setMin(entity.getMin());
        xml.setPlaceholder(entity.getPlaceholder());
        xml.setTableView(entity.getTableView());
        return xml;
    }

    //endregion

    //region Import Common Model 

    @Override
    public DescriptionTemplate importCommonModel(DescriptionTemplateModel commonModel, FieldSet fields) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, TransformerException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("import data").And("importCommonModel", commonModel).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.ImportDescriptionTemplate);

        long activeDescriptionTemplatesForTheGroup = commonModel.getGroupId() != null ? this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                .isActive(IsActive.Active)
                .groupIds(commonModel.getGroupId())
                .count() : 0;

        if (activeDescriptionTemplatesForTheGroup == 0) {
            DescriptionTemplatePersist persist = new DescriptionTemplatePersist();
            persist.setLabel(commonModel.getLabel());
            persist.setStatus(DescriptionTemplateStatus.Draft);
            persist.setDescription(commonModel.getDescription());
            persist.setLanguage(commonModel.getLanguage());
            persist.setType(this.commonModelDescriptionTemplateTypeToPersist(commonModel.getType()));
            persist.setDefinition(this.commonModelDefinitionToPersist(commonModel.getDefinition()));
            this.validatorFactory.validator(DescriptionTemplatePersist.DescriptionTemplatePersistValidator.class).validateForce(persist);
            return this.persist(persist, commonModel.getGroupId(), fields);
        } else {
            DescriptionTemplateEntity latestVersionDescriptionTemplate = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking()
                    .versionStatuses(DescriptionTemplateVersionStatus.Current)
                    .isActive(IsActive.Active)
                    .statuses(DescriptionTemplateStatus.Finalized)
                    .groupIds(commonModel.getGroupId())
                    .first();
            if (latestVersionDescriptionTemplate == null) throw new MyValidationException(this.errors.getDescriptionTemplateIsNotFinalized().getCode(), this.errors.getDescriptionTemplateIsNotFinalized().getMessage());

            NewVersionDescriptionTemplatePersist persist = new NewVersionDescriptionTemplatePersist();
            persist.setId(latestVersionDescriptionTemplate.getId());
            persist.setLabel(commonModel.getLabel());
            persist.setStatus(DescriptionTemplateStatus.Draft);
            persist.setDescription(commonModel.getDescription());
            persist.setLanguage(commonModel.getLanguage());
            persist.setDefinition(this.commonModelDefinitionToPersist(commonModel.getDefinition()));
            persist.setType(this.commonModelDescriptionTemplateTypeToPersist(commonModel.getType()));
            persist.setHash(this.conventionService.hashValue(latestVersionDescriptionTemplate.getUpdatedAt()));

            this.validatorFactory.validator(NewVersionDescriptionTemplatePersist.NewVersionDescriptionTemplatePersistValidator.class).validateForce(persist);
            return this.createNewVersion(persist, fields);
        }
    }
    
    private UUID commonModelDescriptionTemplateTypeToPersist(DescriptionTemplateTypeModel commonModel) throws InvalidApplicationException {
        long descriptionTemplateTypeCount = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking()
                .isActive(IsActive.Active)
                .ids(commonModel.getId())
                .count();
        
        if (descriptionTemplateTypeCount > 0) return commonModel.getId();
        
        DescriptionTemplateTypePersist descriptionTemplateType = new DescriptionTemplateTypePersist();
        descriptionTemplateType.setName(commonModel.getName());
        descriptionTemplateType.setStatus(DescriptionTemplateTypeStatus.Finalized);
        this.validatorFactory.validator(DescriptionTemplateTypePersist.DescriptionTemplateTypePersistValidator.class).validateForce(descriptionTemplateType);
        return this.descriptionTemplateTypeService.persist(descriptionTemplateType, new BaseFieldSet().ensure(DescriptionTemplateType._id)).getId();
    }
    

    private DefinitionPersist commonModelDefinitionToPersist(DefinitionModel commonModel) {
        DefinitionPersist definitionPersist = new DefinitionPersist();
        if (commonModel == null)
            return null;

        List<PagePersist> pagesDatasetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getPages())) {
            for (PageModel pageModel : commonModel.getPages()) {
                pagesDatasetEntity.add(this.commonModelPageToPersist(pageModel));
            }
        }
        definitionPersist.setPages(pagesDatasetEntity);

        return definitionPersist;
    }

    private PagePersist commonModelPageToPersist(PageModel commonModel) {
        PagePersist pageEntity = new PagePersist();
        pageEntity.setId(commonModel.getId());
        pageEntity.setOrdinal(commonModel.getOrdinal());
        pageEntity.setTitle(commonModel.getTitle());
        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            List<SectionPersist> sectionsListEntity = new LinkedList<>();
            for (SectionModel sectionModel : commonModel.getSections()) {
                sectionsListEntity.add(this.commonModelSectionToPersist(sectionModel));
            }
            pageEntity.setSections(sectionsListEntity);

        }
        return pageEntity;
    }

    private SectionPersist commonModelSectionToPersist(SectionModel commonModel) {
        SectionPersist sectionEntity = new SectionPersist();
        List<SectionPersist> sectionsListEntity = new LinkedList<>();

        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            for (SectionModel sectionModel : commonModel.getSections()) {
                sectionsListEntity.add(this.commonModelSectionToPersist(sectionModel));
            }
        }
        sectionEntity.setId(commonModel.getId());
        sectionEntity.setOrdinal(commonModel.getOrdinal());
        sectionEntity.setTitle(commonModel.getTitle());
        sectionEntity.setDescription(commonModel.getDescription());
        List<FieldSetPersist> fieldSetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getFieldSets())) {
            for (FieldSetModel fieldSetModel : commonModel.getFieldSets()) {
                fieldSetEntity.add(this.commonModelToFieldSetModel(fieldSetModel));
            }
        }
        sectionEntity.setFieldSets(fieldSetEntity);

        sectionEntity.setSections(sectionsListEntity);
        return sectionEntity;
    }

    private FieldSetPersist commonModelToFieldSetModel(FieldSetModel commonModel) {
        FieldSetPersist fieldSet1Entity = new FieldSetPersist();
        fieldSet1Entity.setId(commonModel.getId());
        fieldSet1Entity.setOrdinal(commonModel.getOrdinal());
//        fieldSet1Entity.setHasCommentField(commonModel.getHasCommentField() != null ? commonModel.getHasCommentField() : false);//TODO add to common model
        fieldSet1Entity.setMultiplicity(commonModel.getMultiplicity() != null ? this.commonModelMultiplicityToPersist(commonModel.getMultiplicity()) : null);
        fieldSet1Entity.setHasMultiplicity(commonModel.getMultiplicity() != null); //TODO add to common model
        fieldSet1Entity.setTitle(commonModel.getTitle());
        fieldSet1Entity.setDescription(commonModel.getDescription());
        fieldSet1Entity.setExtendedDescription(commonModel.getExtendedDescription());
        fieldSet1Entity.setAdditionalInformation(commonModel.getAdditionalInformation());

        List<FieldPersist> fieldsEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getFields())) {
            for (FieldModel fieldModel : commonModel.getFields()) {
                fieldsEntity.add(this.commonModelFieldToPersist(fieldModel));
            }
        }
        fieldSet1Entity.setFields(fieldsEntity);
        return fieldSet1Entity;
    }

    private FieldPersist commonModelFieldToPersist(FieldModel commonModel) {
        FieldPersist fieldEntity = new FieldPersist();
        fieldEntity.setId(commonModel.getId());
        fieldEntity.setOrdinal(commonModel.getOrdinal());
        fieldEntity.setValidations(this.commonModelValidationsToPersist(commonModel.getValidations()));
        fieldEntity.setIncludeInExport(commonModel.getIncludeInExport());
        fieldEntity.setDefaultValue(this.commonModelRuleToPersist(commonModel.getDefaultValue()));
        List<RulePersist> rulePersists = new ArrayList<>();
//        if (commonModel.getVisibilityRules() != null) {  //TODO add to common model
//            for (DescriptionTemplateRuleImportExport xmlRule : commonModel.getVisibilityRules()) {
//                rulePersists.add(this.toRuleModel(xmlRule));
//            }
//        }
        fieldEntity.setVisibilityRules(rulePersists);
        FieldType fieldType;
        switch (commonModel.getData().getFieldType()){
            case SELECT -> fieldType = FieldType.SELECT;
            case BOOLEAN_DECISION -> fieldType = FieldType.BOOLEAN_DECISION;
            case RADIO_BOX -> fieldType = FieldType.RADIO_BOX;
            case INTERNAL_ENTRIES_PlANS -> fieldType = FieldType.INTERNAL_ENTRIES_PLANS;
            case INTERNAL_ENTRIES_DESCRIPTIONS -> fieldType = FieldType.INTERNAL_ENTRIES_DESCRIPTIONS;
            case CHECK_BOX -> fieldType = FieldType.CHECK_BOX;
            case FREE_TEXT -> fieldType = FieldType.FREE_TEXT;
            case TEXT_AREA -> fieldType = FieldType.TEXT_AREA;
            case RICH_TEXT_AREA -> fieldType = FieldType.RICH_TEXT_AREA;
            case UPLOAD -> fieldType = FieldType.UPLOAD;
            case DATE_PICKER -> fieldType = FieldType.DATE_PICKER;
            case TAGS -> fieldType = FieldType.TAGS;
            case REFERENCE_TYPES -> fieldType = FieldType.REFERENCE_TYPES;
            case DATASET_IDENTIFIER -> fieldType = FieldType.DATASET_IDENTIFIER;
            case VALIDATION -> fieldType = FieldType.VALIDATION;
            default -> throw new InternalError("unknown type: " + commonModel.getData().getFieldType());
        }

        if (commonModel.getData() != null) {
            FieldDataHelperService fieldDataHelperService = this.fieldDataHelperServiceProvider.get(fieldType);
            fieldEntity.setData(fieldDataHelperService.commonModelMapDataToPersist(commonModel.getData()));
        }
        fieldEntity.setSemantics(commonModel.getSemantics());
        return fieldEntity;
    }
    
    private List<FieldValidationType> commonModelValidationsToPersist(List<org.opencdmp.commonmodels.enums.FieldValidationType> commonModel) {
        if (this.conventionService.isListNullOrEmpty(commonModel)) return null;
        List<FieldValidationType> validationTypes = new ArrayList<>();
        for (org.opencdmp.commonmodels.enums.FieldValidationType fieldValidationType : commonModel){
            switch (fieldValidationType){
                case Url -> validationTypes.add(FieldValidationType.Url);
                case Required -> validationTypes.add(FieldValidationType.Required);
                case None -> validationTypes.add(FieldValidationType.None);
                default -> throw new InternalError("unknown type: " + fieldValidationType);
            }
        }
        return validationTypes;
    }

    private DefaultValuePersist commonModelRuleToPersist(DefaultValueModel commonModel) {
        if (commonModel == null) return null;
        DefaultValuePersist ruleEntity = new DefaultValuePersist();
        ruleEntity.setDateValue(commonModel.getDateValue());
        ruleEntity.setBooleanValue(commonModel.getBooleanValue());
        ruleEntity.setTextValue(commonModel.getTextValue());
        return ruleEntity;
    }

    private MultiplicityPersist commonModelMultiplicityToPersist(MultiplicityModel commonModel) {
        MultiplicityPersist multiplicityEntity = new MultiplicityPersist();
        multiplicityEntity.setMax(commonModel.getMax());
        multiplicityEntity.setMin(commonModel.getMin());
        multiplicityEntity.setPlaceholder(commonModel.getPlaceholder());
        multiplicityEntity.setTableView(commonModel.getTableView());
        return multiplicityEntity;
    }

    //endregion
}

