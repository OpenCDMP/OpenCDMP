package org.opencdmp.service.tenantconfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.opencdmp.commons.types.featured.DescriptionTemplateEntity;
import org.opencdmp.commons.types.featured.PlanBlueprintEntity;
import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import org.opencdmp.commons.types.tenantconfiguration.*;
import org.opencdmp.commons.types.viewpreference.ViewPreferenceEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.EventBroker;
import org.opencdmp.event.TenantConfigurationTouchedEvent;
import org.opencdmp.integrationevent.outbox.tenantdefaultlocaleremoval.TenantDefaultLocaleRemovalIntegrationEvent;
import org.opencdmp.integrationevent.outbox.tenantdefaultlocaleremoval.TenantDefaultLocaleRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.tenantdefaultlocaletouched.TenantDefaultLocaleTouchedIntegrationEvent;
import org.opencdmp.integrationevent.outbox.tenantdefaultlocaletouched.TenantDefaultLocaleTouchedIntegrationEventHandler;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.tenantconfiguration.TenantConfigurationBuilder;
import org.opencdmp.model.deleter.TenantConfigurationDeleter;
import org.opencdmp.model.persist.deposit.DepositSourcePersist;
import org.opencdmp.model.persist.evaluator.EvaluatorSourcePersist;
import org.opencdmp.model.persist.featured.DescriptionTemplatePersist;
import org.opencdmp.model.persist.featured.PlanBlueprintPersist;
import org.opencdmp.model.persist.filetransformer.FileTransformerSourcePersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.opencdmp.model.persist.tenantconfiguration.*;
import org.opencdmp.model.persist.viewpreference.ViewPreferencePersist;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.pluginconfiguration.PluginConfigurationService;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TenantConfigurationServiceImpl implements TenantConfigurationService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantConfigurationServiceImpl.class));

    private final TenantEntityManager entityManager;

    private final AuthorizationService authorizationService;

    private final DeleterFactory deleterFactory;

    private final BuilderFactory builderFactory;

    private final ConventionService conventionService;

    private final ErrorThesaurusProperties errors;

    private final MessageSource messageSource;

    private final JsonHandlingService jsonHandlingService;

    private final EncryptionService encryptionService;

    private final TenantProperties tenantProperties;
    private final StorageFileService storageFileService;
    private final QueryFactory queryFactory;
    private final EventBroker eventBroker;
    private final TenantScope tenantScope;

    private final TenantDefaultLocaleTouchedIntegrationEventHandler tenantDefaultLocaleTouchedIntegrationEventHandler;
    private final TenantDefaultLocaleRemovalIntegrationEventHandler tenantDefaultLocaleRemovalIntegrationEventHandler;

    private final PluginConfigurationService pluginConfigurationService;

    @Autowired
    public TenantConfigurationServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            DeleterFactory deleterFactory,
            BuilderFactory builderFactory,
            ConventionService conventionService,
            ErrorThesaurusProperties errors,
            MessageSource messageSource, JsonHandlingService jsonHandlingService, EncryptionService encryptionService, TenantProperties tenantProperties, StorageFileService storageFileService, QueryFactory queryFactory, EventBroker eventBroker, TenantScope tenantScope, TenantDefaultLocaleTouchedIntegrationEventHandler tenantDefaultLocaleTouchedIntegrationEventHandler, TenantDefaultLocaleRemovalIntegrationEventHandler tenantDefaultLocaleRemovalIntegrationEventHandler, PluginConfigurationService pluginConfigurationService) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.errors = errors;
        this.messageSource = messageSource;
	    this.jsonHandlingService = jsonHandlingService;
	    this.encryptionService = encryptionService;
	    this.tenantProperties = tenantProperties;
	    this.storageFileService = storageFileService;
	    this.queryFactory = queryFactory;
	    this.eventBroker = eventBroker;
	    this.tenantScope = tenantScope;
	    this.tenantDefaultLocaleTouchedIntegrationEventHandler = tenantDefaultLocaleTouchedIntegrationEventHandler;
	    this.tenantDefaultLocaleRemovalIntegrationEventHandler = tenantDefaultLocaleRemovalIntegrationEventHandler;
        this.pluginConfigurationService = pluginConfigurationService;
    }

    public TenantConfiguration persist(TenantConfigurationPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, JsonProcessingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("persisting data TenantConfiguration").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditTenantConfiguration);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        TenantConfigurationEntity data;
        if (isUpdate) {
            data = this.entityManager.find(TenantConfigurationEntity.class, model.getId());
            if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), TenantConfiguration.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
            if (!data.getType().equals(model.getType())) throw new MyValidationException(this.errors.getTenantConfigurationTypeCanNotChange().getCode(), this.errors.getTenantConfigurationTypeCanNotChange().getMessage());
        } else {
            data = new TenantConfigurationEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            data.setType(model.getType());
        }

        TenantConfigurationQuery tenantConfigurationQuery = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().excludedIds(data.getId()).isActive(IsActive.Active).types(data.getType());
        if (this.tenantScope.isDefaultTenant()) tenantConfigurationQuery.tenantIsSet(false);
        else tenantConfigurationQuery.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());
        if (tenantConfigurationQuery.count() > 0)throw new MyValidationException(this.errors.getMultipleTenantConfigurationTypeNotAllowed().getCode(), this.errors.getMultipleTenantConfigurationTypeNotAllowed().getMessage());
        
        switch (data.getType()){
            case CssColors -> data.setValue(this.jsonHandlingService.toJson(this.buildCssColorsTenantConfigurationEntity(model.getCssColors())));
            case DefaultUserLocale -> data.setValue(this.jsonHandlingService.toJson(this.buildDefaultUserLocaleTenantConfigurationEntity(model.getDefaultUserLocale())));
            case DepositPlugins -> data.setValue(this.jsonHandlingService.toJson(this.buildDepositTenantConfigurationEntity(model.getDepositPlugins())));
            case EvaluatorPlugins -> data.setValue(this.jsonHandlingService.toJson(this.buildEvaluatorTenantConfigurationEntity(model.getEvaluatorPlugins())));
            case FileTransformerPlugins -> data.setValue(this.jsonHandlingService.toJson(this.buildFileTransformerTenantConfigurationEntity(model.getFileTransformerPlugins())));
            case Logo -> {
                LogoTenantConfigurationEntity oldValue = this.conventionService.isNullOrEmpty(data.getValue()) ? null : this.jsonHandlingService.fromJsonSafe(LogoTenantConfigurationEntity.class, data.getValue());
                data.setValue(this.jsonHandlingService.toJson(this.buildLogoTenantConfigurationEntity(model.getLogo(), oldValue)));
            }
            case PluginConfiguration -> {
                PluginTenantConfigurationEntity oldValue = this.conventionService.isNullOrEmpty(data.getValue()) ? null : this.jsonHandlingService.fromJsonSafe(PluginTenantConfigurationEntity.class, data.getValue());
                data.setValue(this.jsonHandlingService.toJson(this.buildPluginConfigEntity(model.getPluginConfiguration(), oldValue)));
            }
            case FeaturedEntities -> data.setValue(this.jsonHandlingService.toJson(this.buildFeaturedEntitiesEntity(model.getFeaturedEntities())));
            case DefaultPlanBlueprint -> data.setValue(this.jsonHandlingService.toJson(this.buildDefaultPlanBlueprintConfigurationEntity(model.getDefaultPlanBlueprint())));
            case ViewPreferences -> data.setValue(this.jsonHandlingService.toJson(this.buildViewPreferencesConfigurationEntity(model.getViewPreferences())));
            default -> throw new InternalError("unknown type: " + data.getType());
        }
        data.setUpdatedAt(Instant.now());
        if (isUpdate)
            this.entityManager.merge(data);
        else
            this.entityManager.persist(data);

        this.entityManager.flush();

        if (data.getTenantId() != null) {
            TenantEntity tenant = this.entityManager.find(TenantEntity.class, data.getTenantId(), true);
            if (tenant == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getTenantId(), TenantEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            this.eventBroker.emit(new TenantConfigurationTouchedEvent(tenant.getId(), tenant.getCode(), data.getType()));
        } else {
            this.eventBroker.emit(new TenantConfigurationTouchedEvent(data.getId(), this.tenantScope.getDefaultTenantCode(), data.getType()));
        }
        
        if (data.getType().equals(TenantConfigurationType.DefaultUserLocale)){
            TenantDefaultLocaleTouchedIntegrationEvent event = new TenantDefaultLocaleTouchedIntegrationEvent();
            DefaultUserLocaleTenantConfigurationEntity defaultUserLocaleTenantConfiguration = this.jsonHandlingService.fromJson(DefaultUserLocaleTenantConfigurationEntity.class, data.getValue());
            event.setTenantId(data.getTenantId());
            event.setLanguage(defaultUserLocaleTenantConfiguration.getLanguage());
            event.setCulture(defaultUserLocaleTenantConfiguration.getCulture());
            event.setTimezone(defaultUserLocaleTenantConfiguration.getTimezone());
            this.tenantDefaultLocaleTouchedIntegrationEventHandler.handle(event);
        }
        
        return this.builderFactory.builder(TenantConfigurationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, TenantConfiguration._id), data);
    }

    private @NotNull DepositTenantConfigurationEntity buildDepositTenantConfigurationEntity(DepositTenantConfigurationPersist persist) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DepositTenantConfigurationEntity data = new DepositTenantConfigurationEntity();
        if (persist == null || this.conventionService.isListNullOrEmpty(persist.getSources())) return data;
        data.setDisableSystemSources(persist.getDisableSystemSources());
        data.setSources(new ArrayList<>());
        for (DepositSourcePersist depositSourcePersist : persist.getSources()) {
            data.getSources().add(this.buildDepositSourceEntity(depositSourcePersist));
        }
        return data;
    }

    private DepositSourceEntity buildDepositSourceEntity(DepositSourcePersist depositSourcePersist) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DepositSourceEntity depositSourceEntity = new DepositSourceEntity();
        depositSourceEntity.setClientId(depositSourcePersist.getClientId());
        if (!this.conventionService.isNullOrEmpty(depositSourcePersist.getClientSecret())) depositSourceEntity.setClientSecret(this.encryptionService.encryptAES(depositSourcePersist.getClientSecret(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv()));
        depositSourceEntity.setRepositoryId(depositSourcePersist.getRepositoryId());
        depositSourceEntity.setUrl(depositSourcePersist.getUrl());
        depositSourceEntity.setIssuerUrl(depositSourcePersist.getIssuerUrl());
        depositSourceEntity.setScope(depositSourcePersist.getScope());
        depositSourceEntity.setPdfTransformerId(depositSourcePersist.getPdfTransformerId());
        depositSourceEntity.setRdaTransformerId(depositSourcePersist.getRdaTransformerId());
        return depositSourceEntity;
    }

    private @NotNull EvaluatorTenantConfigurationEntity buildEvaluatorTenantConfigurationEntity(EvaluatorTenantConfigurationPersist persist) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        EvaluatorTenantConfigurationEntity data = new EvaluatorTenantConfigurationEntity();
        if (persist == null || this.conventionService.isListNullOrEmpty(persist.getSources())) return data;
        data.setDisableSystemSources(persist.getDisableSystemSources());
        data.setSources(new ArrayList<>());
        for (EvaluatorSourcePersist sourcePersist : persist.getSources()) {
            data.getSources().add(this.buildEvaluatorSourceEntity(sourcePersist));
        }
        return data;
    }

    private EvaluatorSourceEntity buildEvaluatorSourceEntity(EvaluatorSourcePersist sourcePersist) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        EvaluatorSourceEntity sourceEntity = new EvaluatorSourceEntity();
        sourceEntity.setClientId(sourcePersist.getClientId());
        if (!this.conventionService.isNullOrEmpty(sourcePersist.getClientSecret())) sourceEntity.setClientSecret(this.encryptionService.encryptAES(sourcePersist.getClientSecret(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv()));
        sourceEntity.setUrl(sourcePersist.getUrl());
        sourceEntity.setIssuerUrl(sourcePersist.getIssuerUrl());
        sourceEntity.setScope(sourcePersist.getScope());
        sourceEntity.setEvaluatorId(sourcePersist.getEvaluatorId());
        return sourceEntity;
    }

    private @NotNull FileTransformerTenantConfigurationEntity buildFileTransformerTenantConfigurationEntity(FileTransformerTenantConfigurationPersist persist) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        FileTransformerTenantConfigurationEntity data = new FileTransformerTenantConfigurationEntity();
        if (persist == null || this.conventionService.isListNullOrEmpty(persist.getSources())) return data;
        data.setDisableSystemSources(persist.getDisableSystemSources());
        data.setSources(new ArrayList<>());
        for (FileTransformerSourcePersist depositSourcePersist : persist.getSources()) {
            data.getSources().add(this.buildFileTransformerSourceEntity(depositSourcePersist));
        }
        return data;
    }

    private FileTransformerSourceEntity buildFileTransformerSourceEntity(FileTransformerSourcePersist depositSourcePersist) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        FileTransformerSourceEntity depositSourceEntity = new FileTransformerSourceEntity();
        depositSourceEntity.setClientId(depositSourcePersist.getClientId());
        if (!this.conventionService.isNullOrEmpty(depositSourcePersist.getClientSecret())) depositSourceEntity.setClientSecret(this.encryptionService.encryptAES(depositSourcePersist.getClientSecret(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv()));
        depositSourceEntity.setUrl(depositSourcePersist.getUrl());
        depositSourceEntity.setIssuerUrl(depositSourcePersist.getIssuerUrl());
        depositSourceEntity.setScope(depositSourcePersist.getScope());
        depositSourceEntity.setTransformerId(depositSourcePersist.getTransformerId());
        return depositSourceEntity;
    }

    private @NotNull CssColorsTenantConfigurationEntity buildCssColorsTenantConfigurationEntity(CssColorsTenantConfigurationPersist persist){
        CssColorsTenantConfigurationEntity data = new CssColorsTenantConfigurationEntity();
        if (persist == null) return data;
        data.setPrimaryColor(persist.getPrimaryColor());
        data.setCssOverride(persist.getCssOverride());
        return data;
    }

    private @NotNull DefaultUserLocaleTenantConfigurationEntity buildDefaultUserLocaleTenantConfigurationEntity(DefaultUserLocaleTenantConfigurationPersist persist){
        DefaultUserLocaleTenantConfigurationEntity data = new DefaultUserLocaleTenantConfigurationEntity();
        if (persist == null) return data;
        data.setCulture(persist.getCulture());
        data.setLanguage(persist.getLanguage());
        data.setTimezone(persist.getTimezone());
        return data;
    }

    private @NotNull LogoTenantConfigurationEntity buildLogoTenantConfigurationEntity(LogoTenantConfigurationPersist persist, LogoTenantConfigurationEntity oldValue) throws InvalidApplicationException {
        LogoTenantConfigurationEntity data = new LogoTenantConfigurationEntity();
        if (persist == null) return data;
        data.setStorageFileId(persist.getStorageFileId());

        UUID existingFileId = oldValue != null ? oldValue.getStorageFileId() : null;
        if (persist.getStorageFileId() != null){
            if (!persist.getStorageFileId().equals(existingFileId)) {
                StorageFile storageFile = this.storageFileService.copyToStorage(persist.getStorageFileId(), StorageType.Main, true, new BaseFieldSet().ensure(StorageFile._id));
                this.storageFileService.updatePurgeAt(storageFile.getId(), null);
                if (existingFileId != null) this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().plusSeconds(60));
                data.setStorageFileId(storageFile.getId());
            } else {
                data.setStorageFileId(existingFileId);
            }
        } else {
            if (existingFileId != null) this.storageFileService.updatePurgeAt(existingFileId,  Instant.now().plusSeconds(60));
            data.setStorageFileId(null);
        }
        return data;
    }

    private @NotNull PluginTenantConfigurationEntity buildPluginConfigEntity (PluginTenantConfigurationPersist persist, PluginTenantConfigurationEntity oldValue) throws InvalidApplicationException {
        PluginTenantConfigurationEntity data = new PluginTenantConfigurationEntity();
        if (persist == null) return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getPluginConfigurations())) {
            data.setPluginConfigurations(new ArrayList<>());
            for (PluginConfigurationPersist pluginConfigurationPersist : persist.getPluginConfigurations()) {
                data.getPluginConfigurations().add(this.pluginConfigurationService.buildPluginConfigurationEntity(pluginConfigurationPersist, oldValue != null ? oldValue.getPluginConfigurations(): null));
            }
        }

        return data;
    }

    private @NotNull FeaturedEntitiesEntity buildFeaturedEntitiesEntity(FeaturedEntitiesPersist persist) {
        FeaturedEntitiesEntity data = new FeaturedEntitiesEntity();
        if (persist == null) return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getPlanBlueprints())) {
            data.setPlanBlueprints(new ArrayList<>());
            for (PlanBlueprintPersist planBlueprintPersist : persist.getPlanBlueprints()) {
                data.getPlanBlueprints().add(this.buildFeaturedPlanBlueprintEntity(planBlueprintPersist));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(persist.getDescriptionTemplates())) {
            data.setDescriptionTemplates(new ArrayList<>());
            for (DescriptionTemplatePersist descriptionTemplatePersist : persist.getDescriptionTemplates()) {
                data.getDescriptionTemplates().add(this.buildFeaturedDescriptionTemplateEntity(descriptionTemplatePersist));
            }
        }
        return data;
    }

    private @NotNull PlanBlueprintEntity buildFeaturedPlanBlueprintEntity(PlanBlueprintPersist persist) {
        PlanBlueprintEntity data = new PlanBlueprintEntity();
        if (persist == null) return data;

        data.setGroupId(persist.getGroupId());
        data.setOrdinal(persist.getOrdinal());

        return data;
    }

    private @NotNull DescriptionTemplateEntity buildFeaturedDescriptionTemplateEntity(DescriptionTemplatePersist persist) {
        DescriptionTemplateEntity data = new DescriptionTemplateEntity();
        if (persist == null) return data;

        data.setGroupId(persist.getGroupId());
        data.setOrdinal(persist.getOrdinal());

        return data;
    }

    private @NotNull DefaultPlanBlueprintConfigurationEntity buildDefaultPlanBlueprintConfigurationEntity(DefaultPlanBlueprintConfigurationPersist persist) {
        DefaultPlanBlueprintConfigurationEntity data = new DefaultPlanBlueprintConfigurationEntity();
        if (persist == null) return data;

        data.setGroupId(persist.getGroupId());
        return data;
    }

    private @NotNull ViewPreferencesConfigurationEntity buildViewPreferencesConfigurationEntity(ViewPreferencesConfigurationPersist persist) {
        ViewPreferencesConfigurationEntity data = new ViewPreferencesConfigurationEntity();
        if (persist == null) return data;

        if (!this.conventionService.isListNullOrEmpty(persist.getPlanPreferences())) {
            data.setPlanPreferences(new ArrayList<>());
            for (ViewPreferencePersist planPreferencePersist : persist.getPlanPreferences()) {
                data.getPlanPreferences().add(this.buildViewPreferenceEntity(planPreferencePersist));
            }
        }
        if (!this.conventionService.isListNullOrEmpty(persist.getDescriptionPreferences())) {
            data.setDescriptionPreferences(new ArrayList<>());
            for (ViewPreferencePersist descriptionPreferencePersist : persist.getDescriptionPreferences()) {
                data.getDescriptionPreferences().add(this.buildViewPreferenceEntity(descriptionPreferencePersist));
            }
        }
        return data;
    }

    private @NotNull ViewPreferenceEntity buildViewPreferenceEntity(ViewPreferencePersist persist) {
        ViewPreferenceEntity data = new ViewPreferenceEntity();
        if (persist == null) return data;

        data.setReferenceTypeId(persist.getReferenceTypeId());
        data.setOrdinal(persist.getOrdinal());

        return data;
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting dataset: {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteTenantConfiguration);

        TenantConfigurationEntity data = this.entityManager.find(TenantConfigurationEntity.class, id);
        if (data == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, TenantConfiguration.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (data.getType().equals(TenantConfigurationType.Logo)){
            LogoTenantConfigurationEntity oldValue = this.conventionService.isNullOrEmpty(data.getValue()) ? null : this.jsonHandlingService.fromJsonSafe(LogoTenantConfigurationEntity.class, data.getValue());
            if (oldValue != null && oldValue.getStorageFileId() != null) this.storageFileService.updatePurgeAt(oldValue.getStorageFileId(),  Instant.now().plusSeconds(60));
        }
        this.deleterFactory.deleter(TenantConfigurationDeleter.class).deleteAndSaveByIds(List.of(id));

        if (data.getTenantId() != null) {
            TenantEntity tenant = this.entityManager.find(TenantEntity.class, data.getTenantId(), true);
            if (tenant == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getTenantId(), TenantEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            this.eventBroker.emit(new TenantConfigurationTouchedEvent(tenant.getId(), tenant.getCode(), data.getType()));
        } else {
            this.eventBroker.emit(new TenantConfigurationTouchedEvent(data.getId(), this.tenantScope.getDefaultTenantCode(), data.getType()));
        }

        if (data.getType().equals(TenantConfigurationType.DefaultUserLocale)){
            TenantDefaultLocaleRemovalIntegrationEvent event = new TenantDefaultLocaleRemovalIntegrationEvent();
            event.setTenantId(data.getTenantId());
            this.tenantDefaultLocaleRemovalIntegrationEventHandler.handle(event);
        }
    }

    @Override
    public TenantConfigurationEntity getActiveType(TenantConfigurationType type, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {

        if (type == null) throw new MyApplicationException("tenant configuration type is required!");

        TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).isActive(IsActive.Active).types(type);
        if (this.tenantScope.isDefaultTenant()) query.tenantIsSet(false);
        else query.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());

        TenantConfigurationEntity entity = query.firstAs(BaseFieldSet.build(fieldSet, TenantConfiguration._id));
        if (entity == null && !this.tenantScope.isDefaultTenant()) {
            query.clearTenantIds().tenantIsSet(false);
            entity = query.firstAs(BaseFieldSet.build(fieldSet, TenantConfiguration._id));
        }

        return entity;
    }

}
