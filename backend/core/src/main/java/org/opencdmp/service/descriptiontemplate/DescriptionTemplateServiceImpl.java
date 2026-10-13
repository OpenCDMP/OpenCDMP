package org.opencdmp.service.descriptiontemplate;

import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.SectionModel;
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
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolverFactory;
import org.opencdmp.commonmodels.models.DescriptionTemplateTypeModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.*;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.enums.kpi.KpiDirectionType;
import org.opencdmp.commons.enums.kpi.KpiVersionType;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
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
import org.opencdmp.service.kpi.KpiService;
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

    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    private final UserScopeFactory userScopeFactory;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final MessageSource messageSource;

    private final XmlHandlingService xmlHandlingService;

    private final FieldDataHelperServiceProvider fieldDataHelperServiceProvider;

    private final QueryFactory queryFactory;

    private final ErrorThesaurusProperties errors;

    private final TenantScopeFactory tenantScopeFactory;

    private final ResponseUtilsService responseUtilsService;


    private final JsonHandlingService jsonHandlingService;

    private final NotifyIntegrationEventHandler eventHandler;

    private final NotificationProperties notificationProperties;

    private final ValidatorFactory validatorFactory;
    private final DescriptionTemplateTypeService descriptionTemplateTypeService;
    private final AuthorizationContentResolverFactory authorizationContentResolverFactory;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final LockService lockService;
    private final StorageFileService storageFileService;

    private final PluginConfigurationService pluginConfigurationService;

    private final KpiService kpiService;

    private final IconConfigurationService iconConfigurationService;

    private final PlanBlueprintDescriptionTemplateFieldsService fieldsService;

    @Autowired
    public DescriptionTemplateServiceImpl(
            TenantEntityManagerFactory tenantEntityManagerFactory,
            UserScopeFactory userScopeFactory, AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            MessageSource messageSource,
            XmlHandlingService xmlHandlingService,
            FieldDataHelperServiceProvider fieldDataHelperServiceProvider,
            QueryFactory queryFactory, ErrorThesaurusProperties errors,
            TenantScopeFactory tenantScopeFactory,
            ResponseUtilsService responseUtilsService,
            JsonHandlingService jsonHandlingService,
            NotifyIntegrationEventHandler eventHandler,
            NotificationProperties notificationProperties,
            ValidatorFactory validatorFactory, DescriptionTemplateTypeService descriptionTemplateTypeService, AuthorizationContentResolverFactory authorizationContentResolverFactory, UsageLimitService usageLimitService, AccountingService accountingService, LockService lockService, StorageFileService storageFileService, PluginConfigurationService pluginConfigurationService, KpiService kpiService, IconConfigurationService iconConfigurationService, PlanBlueprintDescriptionTemplateFieldsService fieldsService) {
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.userScopeFactory = userScopeFactory;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.fieldDataHelperServiceProvider = fieldDataHelperServiceProvider;
        this.queryFactory = queryFactory;
        this.errors = errors;
        this.tenantScopeFactory = tenantScopeFactory;
        this.responseUtilsService = responseUtilsService;
        this.jsonHandlingService = jsonHandlingService;
        this.eventHandler = eventHandler;
        this.notificationProperties = notificationProperties;
        this.validatorFactory = validatorFactory;
	    this.descriptionTemplateTypeService = descriptionTemplateTypeService;
	    this.authorizationContentResolverFactory = authorizationContentResolverFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.lockService = lockService;
        this.storageFileService = storageFileService;
        this.pluginConfigurationService = pluginConfigurationService;
        this.kpiService = kpiService;
        this.iconConfigurationService = iconConfigurationService;
        this.fieldsService = fieldsService;
    }

    //region Persist

    public DescriptionTemplate persist(DescriptionTemplatePersist model, UUID groupId, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JacksonException, TransformerException {
        logger.debug(new MapLogEntry("persisting data descriptionTemplate").And("model", model).And("fields", fields));

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());
        if (isUpdate) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().descriptionTemplateAffiliation(model.getId())), Permission.EditDescriptionTemplate);
        else this.authorizationService.authorizeForce(Permission.EditDescriptionTemplate);

        DescriptionTemplateEntity data;
        if (isUpdate) {
            data = this.tenantEntityManagerFactory.getInstance().find(DescriptionTemplateEntity.class, model.getId());
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
        DescriptionTemplateVersionStatus previousVersionStatus = data.getVersionStatus();
        UUID previousType = data.getTypeId();

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
            this.tenantEntityManagerFactory.getInstance().merge(data);
            if (previousStatus != null && previousStatus.equals(DescriptionTemplateStatus.Draft) && data.getStatus().equals(DescriptionTemplateStatus.Finalized)) {
                this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());
                this.accountingService.decrease(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
            }
            if (!previousType.equals(data.getTypeId())) {
                this.kpiService.sendIndicatorPointDescriptionTemplatePerTypeEntry(KpiDirectionType.Decrease, previousType);
                this.kpiService.sendIndicatorPointDescriptionTemplatePerTypeEntry(KpiDirectionType.Increase, data.getTypeId());
            }
        } else {
            this.tenantEntityManagerFactory.getInstance().persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue());
            if (data.getStatus().equals(DescriptionTemplateStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
            if (data.getStatus().equals(DescriptionTemplateStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());
            if(data.getTypeId() != null) this.kpiService.sendIndicatorPointDescriptionTemplatePerTypeEntry(KpiDirectionType.Increase, data.getTypeId());
            this.kpiService.sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType.Increase, KpiVersionType.TotalCount);
        }

        this.persistUsers(data.getId(), model.getUsers());

        this.tenantEntityManagerFactory.getInstance().flush();

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

        this.tenantEntityManagerFactory.getInstance().flush();

        if (previousVersionStatus != null && !previousVersionStatus.equals(data.getVersionStatus()) && data.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)) {
            this.kpiService.sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType.Increase, KpiVersionType.LatestVersion);
        }

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
                this.tenantEntityManagerFactory.getInstance().merge(oldDescriptionTemplateEntity);
                this.kpiService.sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType.Decrease, KpiVersionType.LatestVersion);
            } else {
                data.setVersion((short) 1);
            }
        }

    }

    /**
     * The template's editors, saved on their own like plan collaborators: the template itself (and its hash) is left
     * untouched, so a draft being edited elsewhere does not conflict with it.
     */
    public DescriptionTemplate assignUsers(UUID id, List<UserDescriptionTemplatePersist> users, FieldSet fields) throws MyForbiddenException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("assigning users to descriptionTemplate").And("id", id).And("users", users));

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().descriptionTemplateAffiliation(id)), Permission.EditDescriptionTemplate);

        DescriptionTemplateEntity data = this.tenantEntityManagerFactory.getInstance().find(DescriptionTemplateEntity.class, id);
        if (data == null || data.getIsActive() != IsActive.Active)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (this.lockService.isLocked(data.getId(), null).getStatus()) throw new MyApplicationException(this.errors.getLockedDescriptionTemplate().getCode(), this.errors.getLockedDescriptionTemplate().getMessage());

        this.persistUsers(data.getId(), users);
        this.tenantEntityManagerFactory.getInstance().flush();

        return this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, DescriptionTemplate._id), data);
    }

    /**
     * Every semantic key the tenant's other templates carry, with how many of them (counted by group, so versions of
     * one template count once). The editors suggest these first, so custom vocabularies stay consistent.
     */
    public Map<String, Integer> semanticsInUse(UUID excludeGroupId) {
        this.authorizationService.authorizeForce(Permission.BrowseDescriptionTemplate);
        List<DescriptionTemplateEntity> items = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).isActive(IsActive.Active).collect();
        Map<String, Set<UUID>> groups = new HashMap<>();
        for (DescriptionTemplateEntity item : items) {
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

    private void persistUsers(UUID id, List<UserDescriptionTemplatePersist> users) throws InvalidApplicationException {
        if (users == null)
            users = new ArrayList<>();
        List<UserDescriptionTemplateEntity> items = this.queryFactory.query(UserDescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).descriptionTemplateIds(id).collect();
        List<UUID> updatedCreatedIds = new ArrayList<>();
        for (UserDescriptionTemplatePersist user : users) {
            // one row per person: a changed role updates the row instead of adding a second one next to it
            UserDescriptionTemplateEntity data = items.stream().filter(x -> x.getUserId().equals(user.getUserId())).findFirst().orElse(null);
            if (data != null && !data.getRole().equals(user.getRole())) {
                data.setRole(user.getRole());
                data.setUpdatedAt(Instant.now());
                this.tenantEntityManagerFactory.getInstance().merge(data);
            }
            if (data == null) {
                data = new UserDescriptionTemplateEntity();
                data.setId(UUID.randomUUID());
                data.setIsActive(IsActive.Active);
                data.setCreatedAt(Instant.now());
                data.setUpdatedAt(Instant.now());
                data.setDescriptionTemplateId(id);
                data.setUserId(user.getUserId());
                data.setRole(user.getRole());
                this.tenantEntityManagerFactory.getInstance().persist(data);
                if (!this.userScopeFactory.getInstance().getUserId().equals(user.getUserId())) {
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

        UserEntity user = this.tenantEntityManagerFactory.getInstance().find(UserEntity.class, userDescriptionTemplate.getUserId(), true);
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
        if(this.tenantScopeFactory.getInstance().getTenantCode() != null && !this.tenantScopeFactory.getInstance().getTenantCode().equals(this.tenantScopeFactory.getInstance().getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScopeFactory.getInstance().getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));
	    this.eventHandler.handle(event);
    }

    private @NotNull DefinitionEntity buildDefinitionEntity(DefinitionPersist persist, DefinitionEntity oldValue) throws InvalidApplicationException {
        DefinitionEntity data = new DefinitionEntity();
        if (persist == null)
            return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getSections())) {
            data.setSections(new ArrayList<>());
            for (SectionPersist sectionPersist : persist.getSections()) {
                data.getSections().add(this.fieldsService.buildSectionEntity(sectionPersist));
            }
        }

        if (!this.conventionService.isListNullOrEmpty(persist.getPluginConfigurations())) {
            data.setPluginConfigurations(new ArrayList<>());
            for (PluginConfigurationPersist pluginConfigurationPersist : persist.getPluginConfigurations()) {
                data.getPluginConfigurations().add(this.pluginConfigurationService.buildPluginConfigurationEntity(pluginConfigurationPersist, oldValue != null ? oldValue.getPluginConfigurations(): null));
            }
        }

        UUID oldStorageFileId = null;
        if (oldValue != null && oldValue.getIconConfiguration() != null && oldValue.getIconConfiguration().getStorageFileId() != null) {
            oldStorageFileId = oldValue.getIconConfiguration().getStorageFileId();
        }
        data.setIconConfiguration(this.iconConfigurationService.buildIconConfigurationEntity(persist.getIconConfiguration(), oldStorageFileId));

        return data;
    }


    //endregion 

    //region Delete

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting dataset: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteDescriptionTemplate);

        DescriptionTemplateEntity data = this.tenantEntityManagerFactory.getInstance().find(DescriptionTemplateEntity.class, id);
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
                this.tenantEntityManagerFactory.getInstance().merge(previousFinalized);
                this.kpiService.sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType.Increase, KpiVersionType.LatestVersion);
            }
            this.tenantEntityManagerFactory.getInstance().merge(data);
            this.tenantEntityManagerFactory.getInstance().flush();
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
        if (model.getSections() != null) {
            for (Section section : model.getSections()) {
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

        DescriptionTemplateEntity oldDescriptionTemplateEntity = this.tenantEntityManagerFactory.getInstance().find(DescriptionTemplateEntity.class, model.getId(), true);
        if (oldDescriptionTemplateEntity == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.conventionService.hashValue(oldDescriptionTemplateEntity.getUpdatedAt()).equals(model.getHash()))
            throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());

        if (!this.tenantScopeFactory.getInstance().isSet() || !Objects.equals(oldDescriptionTemplateEntity.getTenantId(), this.tenantScopeFactory.getInstance().getTenant())) throw new MyForbiddenException(this.errors.getTenantTampering().getCode(), this.errors.getTenantTampering().getMessage());
        
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

        if (model.getDefinition().getIconConfiguration() != null) {
            // reassign new icon storage file if equals with old
            this.iconConfigurationService.reassignNewStorageFilesIfEqualsWithOld(model.getDefinition().getIconConfiguration());
        }

        data.setDefinition(this.xmlHandlingService.toXml(this.buildDefinitionEntity(model.getDefinition(), null)));

        this.tenantEntityManagerFactory.getInstance().persist(data);

        this.persistUsers(data.getId(), model.getUsers());
        //this.addOwner(data);

        this.tenantEntityManagerFactory.getInstance().flush();

        this.updateVersionStatusAndSave(data, DescriptionTemplateStatus.Draft, data.getStatus());

        this.tenantEntityManagerFactory.getInstance().flush();

        this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT.getValue());

        if (data.getStatus().equals(DescriptionTemplateStatus.Draft)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_DRAFT_COUNT.getValue());
        if (data.getStatus().equals(DescriptionTemplateStatus.Finalized)) this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_FINALIZED_COUNT.getValue());

        if (data.getVersionStatus().equals(DescriptionTemplateVersionStatus.Current)) {
            this.kpiService.sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType.Increase, KpiVersionType.LatestVersion);
        }

        this.kpiService.sendIndicatorPointDescriptionTemplateEntry(KpiDirectionType.Increase, KpiVersionType.TotalCount);
        this.kpiService.sendIndicatorPointDescriptionTemplatePerTypeEntry(KpiDirectionType.Increase,data.getTypeId());
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

        List<SectionPersist> sectionsDatasetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getSections())) {
            for (DescriptionTemplateSectionImportExport xmlSection : importExport.getSections()) {
                sectionsDatasetEntity.add(this.fieldsService.xmlSectionToPersist(xmlSection));
            }
        }
        definitionPersist.setSections(sectionsDatasetEntity);

        List<PluginConfigurationPersist> pluginConfigurations = new ArrayList<>();
        if (!this.conventionService.isListNullOrEmpty(importExport.getPluginConfigurations())) {
            for (PluginConfigurationImportExport pluginConfiguration : importExport.getPluginConfigurations()) {
                pluginConfigurations.add(this.pluginConfigurationService.xmlPluginConfigurationToPersist(pluginConfiguration));
            }
        }
        definitionPersist.setPluginConfigurations(pluginConfigurations);

        return definitionPersist;
    }

    //endregion

    //region Export

    @Override
    public DescriptionTemplateImportExport exportXmlEntity(UUID id, boolean ignoreAuthorize) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("exportXml").And("id", id));

        if (!ignoreAuthorize) this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().descriptionTemplateAffiliation(id)), Permission.ExportDescriptionTemplate);
        DescriptionTemplateEntity data = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(id).authorize(AuthorizationFlags.AllExceptPublic).first();
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DefinitionEntity definition = this.xmlHandlingService.fromXml(DefinitionEntity.class, data.getDefinition());
        return this.definitionXmlToExport(data, definition);
    }


    @Override
    public ResponseEntity<byte[]> exportXml(UUID id) throws MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("exportXml").And("id", id));

        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().descriptionTemplateAffiliation(id)), Permission.ExportDescriptionTemplate);

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

        List<DescriptionTemplateSectionImportExport> sectionsDatasetEntity = new LinkedList<>();
        for (SectionEntity xmlSection : entity.getSections()) {
            sectionsDatasetEntity.add(this.fieldsService.sectionXmlToExport(xmlSection));
        }
        xml.setSections(sectionsDatasetEntity);

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

        DescriptionTemplateTypeEntity data = this.tenantEntityManagerFactory.getInstance().find(DescriptionTemplateTypeEntity.class, typeId);
        if (data == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{typeId, DescriptionTemplateType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        xml.setId(typeId);
        xml.setName(data.getName());
        xml.setCode(data.getCode());

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

        List<SectionPersist> sectionsDatasetEntity = new LinkedList<>();
        if (!this.conventionService.isListNullOrEmpty(commonModel.getSections())) {
            for (SectionModel sectionModel : commonModel.getSections()) {
                sectionsDatasetEntity.add(this.fieldsService.commonModelSectionToPersist(sectionModel));
            }
        }
        definitionPersist.setSections(sectionsDatasetEntity);

        return definitionPersist;
    }

    //endregion
}

