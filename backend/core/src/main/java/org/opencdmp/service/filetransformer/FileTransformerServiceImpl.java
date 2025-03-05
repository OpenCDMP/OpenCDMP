package org.opencdmp.service.filetransformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeCacheService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeFilterFunction;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeModel;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.descriptiotemplate.DescriptionTemplateModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.planblueprint.PlanBlueprintModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import org.opencdmp.commons.types.tenantconfiguration.FileTransformerTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.TenantConfigurationTouchedEvent;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerConfiguration;
import org.opencdmp.filetransformerbase.models.misc.DescriptionImportModel;
import org.opencdmp.filetransformerbase.models.misc.PlanImportModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.model.PublicPlan;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.description.DescriptionCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.DescriptionTemplateCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.plan.PlanCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.planblueprint.PlanBlueprintCommonModelBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.file.RepositoryFileFormat;
import org.opencdmp.model.persist.DescriptionCommonModelConfig;
import org.opencdmp.model.persist.PlanCommonModelConfig;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@Service
public class FileTransformerServiceImpl implements FileTransformerService {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FileTransformerServiceImpl.class));
    private final FileTransformerProperties fileTransformerProperties;
    private final Map<String, FileTransformerRepository> clients;
    private final TokenExchangeCacheService tokenExchangeCacheService;
    private final FileTransformerConfigurationCacheService fileTransformerConfigurationCacheService;
    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final StorageFileService storageFileService;
    private final MessageSource messageSource;
    private final ConventionService conventionService;
    private final TenantScope tenantScope;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private final JsonHandlingService jsonHandlingService;
    private final FileTransformerSourcesCacheService fileTransformerSourcesCacheService;
    private final UserScope userScope;
    private final AccountingService accountingService;
    private final TenantEntityManager entityManager;
    private final ErrorThesaurusProperties errors;

    @Autowired
    public FileTransformerServiceImpl(FileTransformerProperties fileTransformerProperties, TokenExchangeCacheService tokenExchangeCacheService, FileTransformerConfigurationCacheService fileTransformerConfigurationCacheService, AuthorizationService authorizationService,
                                      QueryFactory queryFactory, BuilderFactory builderFactory, StorageFileService storageFileService, MessageSource messageSource, ConventionService conventionService, TenantScope tenantScope, EncryptionService encryptionService, TenantProperties tenantProperties, JsonHandlingService jsonHandlingService, FileTransformerSourcesCacheService fileTransformerSourcesCacheService, UserScope userScope, AccountingService accountingService, TenantEntityManager entityManager, ErrorThesaurusProperties errors) {
        this.fileTransformerProperties = fileTransformerProperties;
        this.tokenExchangeCacheService = tokenExchangeCacheService;
	    this.fileTransformerConfigurationCacheService = fileTransformerConfigurationCacheService;
	    this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
	    this.storageFileService = storageFileService;
	    this.messageSource = messageSource;
	    this.conventionService = conventionService;
	    this.tenantScope = tenantScope;
	    this.encryptionService = encryptionService;
	    this.tenantProperties = tenantProperties;
	    this.jsonHandlingService = jsonHandlingService;
	    this.fileTransformerSourcesCacheService = fileTransformerSourcesCacheService;
        this.userScope = userScope;
        this.accountingService = accountingService;
        this.entityManager = entityManager;
        this.errors = errors;
        this.clients = new HashMap<>();
    }
    
    private FileTransformerRepository getRepository(String repoId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String repositoryIdByTenant = this.getRepositoryIdByTenant(repoId);
        if (this.clients.containsKey(repositoryIdByTenant)) return this.clients.get(repositoryIdByTenant);

        //GK: It's register time
        FileTransformerSourceEntity source = this.getFileTransformerSources().stream().filter(fileTransformerSourceEntity -> fileTransformerSourceEntity.getTransformerId().equals(repoId)).findFirst().orElse(null);
        if (source != null) {
            TokenExchangeModel tokenExchangeModel = new TokenExchangeModel("file_transformer:" + repositoryIdByTenant, source.getIssuerUrl(), source.getClientId(), source.getClientSecret(), source.getScope());
            TokenExchangeFilterFunction tokenExchangeFilterFunction = new TokenExchangeFilterFunction(this.tokenExchangeCacheService, tokenExchangeModel);
            FileTransformerRepository repository = new FileTransformerRepository(WebClient.builder().baseUrl(source.getUrl() + "/api/file-transformer").filters(exchangeFilterFunctions -> {
                exchangeFilterFunctions.add(tokenExchangeFilterFunction);
                exchangeFilterFunctions.add(logRequest());
                exchangeFilterFunctions.add(logResponse());
            }).codecs(codecs -> {
                        codecs.defaultCodecs().maxInMemorySize(source.getMaxInMemorySizeInBytes());
                        codecs.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper().registerModule(new JavaTimeModule()), MediaType.APPLICATION_JSON));
                        codecs.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(new ObjectMapper().registerModule(new JavaTimeModule()), MediaType.APPLICATION_JSON));
                    }

            ).build());
            this.clients.put(repositoryIdByTenant, repository);
            return repository;
        }
        return null;
    }

    private List<FileTransformerSourceEntity> getFileTransformerSources() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : "";
        FileTransformerSourcesCacheService.FileTransformerSourceCacheValue cacheValue = this.fileTransformerSourcesCacheService.lookup(this.fileTransformerSourcesCacheService.buildKey(tenantCode));
        if (cacheValue == null) {
            List<FileTransformerSourceEntity> fileTransformerSourceEntities = new ArrayList<>(this.fileTransformerProperties.getSources());
            if (this.tenantScope.isSet() && this.tenantScope.isMultitenant()) {
                TenantConfigurationQuery tenantConfigurationQuery = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.FileTransformerPlugins);
                if (this.tenantScope.isDefaultTenant()) tenantConfigurationQuery.tenantIsSet(false);
                else tenantConfigurationQuery.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());
                TenantConfigurationEntity tenantConfiguration = tenantConfigurationQuery.firstAs(new BaseFieldSet().ensure(TenantConfiguration._fileTransformerPlugins));

                if (tenantConfiguration != null && !this.conventionService.isNullOrEmpty(tenantConfiguration.getValue())) {
                    FileTransformerTenantConfigurationEntity fileTransformerTenantConfigurationEntity = this.jsonHandlingService.fromJsonSafe(FileTransformerTenantConfigurationEntity.class, tenantConfiguration.getValue());
                    if (fileTransformerTenantConfigurationEntity != null) {
                        if (fileTransformerTenantConfigurationEntity.getDisableSystemSources()) fileTransformerSourceEntities = new ArrayList<>();
                        fileTransformerSourceEntities.addAll(this.buildFileTransformerSourceItems(fileTransformerTenantConfigurationEntity.getSources()));
                    }
                }
            }
            cacheValue = new FileTransformerSourcesCacheService.FileTransformerSourceCacheValue(tenantCode, fileTransformerSourceEntities);
            this.fileTransformerSourcesCacheService.put(cacheValue);
        }
        return cacheValue.getSources();
    }

    @EventListener
    public void handleTenantConfigurationTouchedEvent(TenantConfigurationTouchedEvent event) {
        if (!event.getType().equals(TenantConfigurationType.FileTransformerPlugins)) return;
        FileTransformerSourcesCacheService.FileTransformerSourceCacheValue fileTransformerSourceCacheValue = this.fileTransformerSourcesCacheService.lookup(this.fileTransformerSourcesCacheService.buildKey(event.getTenantCode()));
        if (fileTransformerSourceCacheValue != null && fileTransformerSourceCacheValue.getSources() != null){
            for (FileTransformerSourceEntity source : fileTransformerSourceCacheValue.getSources()){
                String repositoryIdByTenant = source.getTransformerId() + "_" + event.getTenantCode();
                this.clients.remove(repositoryIdByTenant);
                this.fileTransformerConfigurationCacheService.evict(this.fileTransformerConfigurationCacheService.buildKey(source.getTransformerId(), event.getTenantCode()));
            }
        }
        this.fileTransformerSourcesCacheService.evict(this.fileTransformerSourcesCacheService.buildKey(event.getTenantCode()));
    }

    private List<FileTransformerSourceEntity> buildFileTransformerSourceItems(List<FileTransformerSourceEntity> sources) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        List<FileTransformerSourceEntity> items = new ArrayList<>();
        if (this.conventionService.isListNullOrEmpty(sources)) return items;
        for (FileTransformerSourceEntity source : sources){
            FileTransformerSourceEntity item = new FileTransformerSourceEntity();
            item.setTransformerId(source.getTransformerId());
            item.setUrl(source.getUrl());
            item.setIssuerUrl(source.getIssuerUrl());
            item.setClientId(source.getClientId());
            if (!this.conventionService.isNullOrEmpty(source.getClientSecret())) item.setClientSecret(this.encryptionService.decryptAES(source.getClientSecret(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv()));
            item.setScope(source.getScope());
            items.add(item);
        }
        return items;
    }

    private String getRepositoryIdByTenant(String repositoryId) throws InvalidApplicationException {
        if (this.tenantScope.isSet() && this.tenantScope.isMultitenant()) {
            return repositoryId + "_" + this.tenantScope.getTenantCode();
        } else {
            return repositoryId;
        }
    }


    @Override
    public List<RepositoryFileFormat> getAvailableExportFileFormats() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        List<RepositoryFileFormat> formats = new ArrayList<>();
        List<FileTransformerConfiguration> configurations = this.getAvailableConfigurations();
        for (FileTransformerConfiguration configuration : configurations){
            if (configuration != null && configuration.getExportVariants() != null) formats.addAll(configuration.getExportVariants().stream().map(x-> new RepositoryFileFormat(configuration.getFileTransformerId(), x, configuration.getExportEntityTypes())).toList());
        }
        return formats;
    }

    private List<FileTransformerConfiguration> getAvailableConfigurations() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        
        List<FileTransformerConfiguration> configurations = new ArrayList<>();

        for (FileTransformerSourceEntity transformerSource : this.getFileTransformerSources()) {

            String tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : "";
            FileTransformerConfigurationCacheService.FileTransformerConfigurationCacheValue cacheValue = this.fileTransformerConfigurationCacheService.lookup(this.fileTransformerConfigurationCacheService.buildKey(transformerSource.getTransformerId(), tenantCode));
            if (cacheValue == null){
                try {
                    FileTransformerRepository fileTransformerRepository = this.getRepository(transformerSource.getTransformerId());
                    if (fileTransformerRepository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{transformerSource.getTransformerId(), FileTransformerRepository.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                    FileTransformerConfiguration configuration = fileTransformerRepository.getConfiguration();
                    cacheValue = new FileTransformerConfigurationCacheService.FileTransformerConfigurationCacheValue(transformerSource.getTransformerId(), tenantCode, configuration);
                    this.fileTransformerConfigurationCacheService.put(cacheValue);
                }catch (Exception e){
                    logger.error(e.getMessage(), e);
                }
            }
            if (cacheValue != null) {
                configurations.add(cacheValue.getConfiguration());
            }
        }

        return configurations;
    }

    @Override
    public org.opencdmp.model.file.FileEnvelope exportPlan(UUID planId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.authorizationService.authorizeForce(Permission.ExportPlan);
        //GK: First get the right client
        FileTransformerRepository repository = this.getRepository(repositoryId);
        if (repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{format, FileTransformerRepository.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        //GK: Second get the Target Data Management Plan
        PlanEntity entity = null;
        if (!isPublic) {
            entity = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.All).ids(planId).first();
        } else {
            try {
                this.entityManager.disableTenantFilters();
                PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
                entity = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(planId).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public).first();
                this.entityManager.reloadTenantFilters();
            } finally {
                this.entityManager.reloadTenantFilters();
            }
        }

        if (entity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanModel planFileTransformerModel = this.builderFactory.builder(PlanCommonModelBuilder.class).useSharedStorage(repository.getConfiguration().isUseSharedStorage()).setRepositoryId(repository.getConfiguration().getFileTransformerId()).isPublic(isPublic).authorize(isPublic ? AuthorizationFlags.All: AuthorizationFlags.AllExceptPublic).build(entity);
        if (planFileTransformerModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        FileEnvelopeModel fileEnvelope = repository.exportPlan(planFileTransformerModel, format);
        org.opencdmp.model.file.FileEnvelope result = new org.opencdmp.model.file.FileEnvelope();
        
        byte[] data = repository.getConfiguration().isUseSharedStorage() ? this.storageFileService.readByFileRefAsBytesSafe(fileEnvelope.getFileRef(), StorageType.Transformer) : fileEnvelope.getFile();
        result.setFile(data);
        result.setFilename(fileEnvelope.getFilename());

        this.accountingService.increase(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_PLAN_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_PLAN_EXECUTION_COUNT_FOR, repositoryId);

        return result;
    }

    @Override
    public org.opencdmp.model.file.FileEnvelope exportDescription(UUID descriptionId, String repositoryId, String format, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.authorizationService.authorizeForce(Permission.ExportDescription);
        //GK: First get the right client
        FileTransformerRepository repository = this.getRepository(repositoryId);
        if (repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{format, FileTransformerRepository.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        //GK: Second get the Target Data Management Plan
        DescriptionEntity entity = null;
        if (!isPublic){
            entity = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(AuthorizationFlags.All).ids(descriptionId).first();
        } else {
            try {
                this.entityManager.disableTenantFilters();
                PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
                entity = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(descriptionId).planSubQuery(this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public)).first();
                this.entityManager.reloadTenantFilters();
            } finally {
                this.entityManager.reloadTenantFilters();
            }
        }

        if (entity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DescriptionModel descriptionFileTransformerModel = this.builderFactory.builder(DescriptionCommonModelBuilder.class).setRepositoryId(repository.getConfiguration().getFileTransformerId()).useSharedStorage(repository.getConfiguration().isUseSharedStorage()).isPublic(isPublic).authorize(isPublic ? AuthorizationFlags.All: AuthorizationFlags.AllExceptPublic).build(entity);
        if (descriptionFileTransformerModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        
        FileEnvelopeModel fileEnvelope = repository.exportDescription(descriptionFileTransformerModel, format); 
        org.opencdmp.model.file.FileEnvelope result = new org.opencdmp.model.file.FileEnvelope();
        byte[] data = repository.getConfiguration().isUseSharedStorage() ? this.storageFileService.readByFileRefAsBytesSafe(fileEnvelope.getFileRef(), StorageType.Transformer) : fileEnvelope.getFile(); //TODO: shared storage should be per repository
        result.setFile(data);
        result.setFilename(fileEnvelope.getFilename());

        this.accountingService.increase(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT_FOR, repositoryId);

        return result;
    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.debug(new MapLogEntry("Request").And("method", clientRequest.method().toString()).And("url", clientRequest.url()));
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.mutate().build().bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error(new MapLogEntry("Response").And("method", response.request().getMethod().toString()).And("url", response.request().getURI()).And("status", response.statusCode().toString()).And("body", body));
                            return Mono.just(response);
                        });
            }
            return Mono.just(response);

        });
    }

    @Override
    public PlanModel importPlan(PlanCommonModelConfig planCommonModelConfig) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException, JAXBException {
        this.authorizationService.authorizeForce(Permission.NewPlan);

        StorageFileEntity tempFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().authorize(AuthorizationFlags.All).ids(planCommonModelConfig.getFileId()).first();

        if (tempFile == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planCommonModelConfig.getFileId(), StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        //GK: First get the right client
        FileTransformerRepository repository = this.getRepository(planCommonModelConfig.getRepositoryId());
        if (repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planCommonModelConfig.getRepositoryId(), FileTransformerRepository.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanBlueprintQuery planBlueprintQuery = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(AuthorizationFlags.All).ids(planCommonModelConfig.getBlueprintId());
        PlanBlueprintModel planBlueprintModel = this.builderFactory.builder(PlanBlueprintCommonModelBuilder.class).authorize(AuthorizationFlags.All).build(planBlueprintQuery.first());
        if (planBlueprintModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planCommonModelConfig.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanImportModel planImportModel = new PlanImportModel();
        planImportModel.setBlueprintModel(planBlueprintModel);

        if (!this.conventionService.isListNullOrEmpty(planCommonModelConfig.getDescriptions())){
            List<DescriptionTemplateEntity> descriptionTemplateEntities = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(AuthorizationFlags.All).ids(planCommonModelConfig.getDescriptions().stream().map(DescriptionCommonModelConfig::getTemplateId).distinct().collect(Collectors.toList())).collect();

            if (descriptionTemplateEntities == null) throw new MyApplicationException("Description Templates Not Exist!");

            List<DescriptionImportModel> descriptionImportModels = new ArrayList<>();
            for (DescriptionCommonModelConfig descriptionCommonModelConfig : planCommonModelConfig.getDescriptions()) {
                DescriptionTemplateEntity descriptionTemplateEntity = descriptionTemplateEntities.stream().filter(x -> x.getId().equals(descriptionCommonModelConfig.getTemplateId())).findFirst().orElse(null);
                if (descriptionTemplateEntity != null){
                    DescriptionTemplateModel descriptionTemplateModel = this.builderFactory.builder(DescriptionTemplateCommonModelBuilder.class).authorize(AuthorizationFlags.All).build(descriptionTemplateEntity);

                    DescriptionImportModel descriptionImportModel = new DescriptionImportModel();
                    descriptionImportModel.setId(descriptionCommonModelConfig.getId());
                    descriptionImportModel.setSectionId(descriptionCommonModelConfig.getSectionId());
                    descriptionImportModel.setDescriptionTemplate(descriptionTemplateModel);

                    descriptionImportModels.add(descriptionImportModel);
                }
            }

            planImportModel.setDescriptions(descriptionImportModels);
        }

        String originalFileName = tempFile.getName() + (tempFile.getExtension().startsWith(".") ? "" : ".") + tempFile.getExtension();
        String mimeType = URLConnection.guessContentTypeFromName(originalFileName);

        FileEnvelopeModel fileEnvelope = new FileEnvelopeModel();
        fileEnvelope.setFile(this.storageFileService.readAsBytesSafe(planCommonModelConfig.getFileId()));
        fileEnvelope.setMimeType(mimeType);
        fileEnvelope.setFilename(originalFileName);

        if (repository.getConfiguration() != null && repository.getConfiguration().isUseSharedStorage()){
            StorageFilePersist storageFilePersist = new StorageFilePersist();
            storageFilePersist.setName(tempFile.getName());
            storageFilePersist.setExtension(tempFile.getExtension());
            storageFilePersist.setMimeType(mimeType);
            storageFilePersist.setOwnerId(this.userScope.getUserIdSafe());
            storageFilePersist.setStorageType(StorageType.Transformer);

            StorageFile storageFile = this.storageFileService.persistBytes(storageFilePersist, fileEnvelope.getFile(), new BaseFieldSet(StorageFile._id, StorageFile._fileRef, StorageFile._mimeType, StorageFile._extension, StorageFile._name));
            fileEnvelope.setFileRef(storageFile.getFileRef());
        }

        planImportModel.setFile(fileEnvelope);

        this.accountingService.increase(UsageLimitTargetMetric.FILE_TRANSFORMER_IMPORT_PLAN_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.FILE_TRANSFORMER_IMPORT_PLAN_EXECUTION_COUNT_FOR, planCommonModelConfig.getRepositoryId());

        return repository.importPlan(planImportModel);
    }

    @Override
    public PreprocessingPlanModel preprocessingPlan(UUID fileId, String repositoryId) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        this.authorizationService.authorizeForce(Permission.NewPlan);

        StorageFileEntity tempFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().authorize(AuthorizationFlags.All).ids(fileId).first();

        if (tempFile == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{fileId, StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        //GK: First get the right client
        FileTransformerRepository repository = this.getRepository(repositoryId);
        if (repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, FileTransformerRepository.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String originalFileName = tempFile.getName() + (tempFile.getExtension().startsWith(".") ? "" : ".") + tempFile.getExtension();
        String mimeType = URLConnection.guessContentTypeFromName(originalFileName);

        FileEnvelopeModel fileEnvelope = new FileEnvelopeModel();
        fileEnvelope.setFile(this.storageFileService.readAsBytesSafe(fileId));
        fileEnvelope.setMimeType(mimeType);
        fileEnvelope.setFilename(originalFileName);

        if (repository.getConfiguration() != null && repository.getConfiguration().isUseSharedStorage()){
            StorageFilePersist storageFilePersist = new StorageFilePersist();
            storageFilePersist.setName(tempFile.getName());
            storageFilePersist.setExtension(tempFile.getExtension());
            storageFilePersist.setMimeType(mimeType);
            storageFilePersist.setOwnerId(this.userScope.getUserIdSafe());
            storageFilePersist.setStorageType(StorageType.Transformer);

            StorageFile storageFile = this.storageFileService.persistBytes(storageFilePersist, fileEnvelope.getFile(), new BaseFieldSet(StorageFile._id, StorageFile._fileRef, StorageFile._mimeType, StorageFile._extension, StorageFile._name));
            fileEnvelope.setFileRef(storageFile.getFileRef());
        }

        try {
            return repository.preprocessingPlan(fileEnvelope);
        } catch (Exception e) {
            logger.warn("Preprocessing plan failed. Input: " + new String(fileEnvelope.getFile(), StandardCharsets.UTF_8));
            throw new MyApplicationException(this.errors.getInvalidPlanImportRdaJson().getCode(), this.errors.getInvalidPlanImportRdaJson().getMessage());
        }
    }

    private void increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric metric, String repositoryId) throws InvalidApplicationException {
        this.accountingService.increase(metric.getValue() + repositoryId);
    }

}
