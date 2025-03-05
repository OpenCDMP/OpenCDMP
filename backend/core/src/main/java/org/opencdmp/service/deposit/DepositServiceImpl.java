package org.opencdmp.service.deposit;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeCacheService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeFilterFunction;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeModel;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.commons.types.tenantconfiguration.DepositTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.data.PlanUserEntity;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.data.UserEntity;
import org.opencdmp.depositbase.repository.DepositClient;
import org.opencdmp.depositbase.repository.DepositConfiguration;
import org.opencdmp.event.TenantConfigurationTouchedEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.DepositConfigurationBuilder;
import org.opencdmp.model.builder.commonmodels.plan.PlanCommonModelBuilder;
import org.opencdmp.model.persist.EntityDoiPersist;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.model.persist.deposit.DepositAuthenticateRequest;
import org.opencdmp.model.persist.deposit.DepositRequest;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanUserQuery;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.query.UserQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.entitydoi.EntityDoiService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.storage.StorageFileProperties;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepositServiceImpl implements DepositService {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DepositServiceImpl.class));
    private static final Logger log = LoggerFactory.getLogger(DepositServiceImpl.class);

    private final DepositProperties depositProperties;
    private final Map<String, DepositClient> clients;
    private final TokenExchangeCacheService tokenExchangeCacheService;
    private final AuthorizationService authorizationService;
    private final EntityDoiService doiService;
    private final QueryFactory queryFactory;
    private final MessageSource messageSource;
    private final BuilderFactory builderFactory;
    private final DepositConfigurationCacheService depositConfigurationCacheService;
    private final FileTransformerService fileTransformerService;
    private final StorageFileService storageFileService;
    private final UserScope userScope;
    private final ValidatorFactory validatorFactory;
    private final StorageFileProperties storageFileProperties;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final ConventionService conventionService;
    private final JsonHandlingService jsonHandlingService;
    private final NotificationProperties notificationProperties;
    private final NotifyIntegrationEventHandler eventHandler;
    private final TenantScope tenantScope;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private final DepositSourcesCacheService depositSourcesCacheService;
    private final AccountingService accountingService;
    
    @Autowired
    public DepositServiceImpl(DepositProperties depositProperties,
                              TokenExchangeCacheService tokenExchangeCacheService,
                              AuthorizationService authorizationService,
                              EntityDoiService doiService,
                              QueryFactory queryFactory,
                              MessageSource messageSource,
                              BuilderFactory builderFactory, DepositConfigurationCacheService depositConfigurationCacheService, FileTransformerService fileTransformerService, StorageFileService storageFileService, UserScope userScope, ValidatorFactory validatorFactory, StorageFileProperties storageFileProperties, AuthorizationContentResolver authorizationContentResolver, ConventionService conventionService, JsonHandlingService jsonHandlingService, NotificationProperties notificationProperties, NotifyIntegrationEventHandler eventHandler, TenantScope tenantScope, EncryptionService encryptionService, TenantProperties tenantProperties, DepositSourcesCacheService depositSourcesCacheService, AccountingService accountingService) {
        this.depositProperties = depositProperties;
        this.tokenExchangeCacheService = tokenExchangeCacheService;
        this.authorizationService = authorizationService;
        this.doiService = doiService;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
	    this.builderFactory = builderFactory;
	    this.depositConfigurationCacheService = depositConfigurationCacheService;
	    this.fileTransformerService = fileTransformerService;
	    this.storageFileService = storageFileService;
	    this.userScope = userScope;
	    this.validatorFactory = validatorFactory;
	    this.storageFileProperties = storageFileProperties;
	    this.authorizationContentResolver = authorizationContentResolver;
        this.conventionService = conventionService;
        this.jsonHandlingService = jsonHandlingService;
        this.notificationProperties = notificationProperties;
        this.eventHandler = eventHandler;
	    this.tenantScope = tenantScope;
	    this.encryptionService = encryptionService;
	    this.tenantProperties = tenantProperties;
	    this.depositSourcesCacheService = depositSourcesCacheService;
        this.accountingService = accountingService;
        this.clients = new HashMap<>();
    }
    
    private DepositClient getDepositClient(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String repositoryIdByTenant = this.getRepositoryIdByTenant(repositoryId);
        if (this.clients.containsKey(repositoryIdByTenant)) return this.clients.get(repositoryIdByTenant);

        //GK: It's register time
        DepositSourceEntity source = this.getDepositSources().stream().filter(depositSource -> depositSource.getRepositoryId().equals(repositoryId)).findFirst().orElse(null);
        if (source != null) {
            TokenExchangeModel tokenExchangeModel = new TokenExchangeModel("deposit:" + repositoryIdByTenant, source.getIssuerUrl(), source.getClientId(), source.getClientSecret(), source.getScope());
            TokenExchangeFilterFunction apiKeyExchangeFilterFunction = new TokenExchangeFilterFunction(this.tokenExchangeCacheService, tokenExchangeModel);
            WebClient webClient = WebClient.builder().baseUrl(source.getUrl() + "/api/deposit")
                    .filters(exchangeFilterFunctions -> {
                        exchangeFilterFunctions.add(apiKeyExchangeFilterFunction);
                        exchangeFilterFunctions.add(logRequest());
                        exchangeFilterFunctions.add(logResponse());
                    }).codecs(codecs -> codecs
                            .defaultCodecs()
                            .maxInMemorySize(source.getMaxInMemorySizeInBytes())
                    ).build();
	        DepositClientImpl repository = new DepositClientImpl(webClient);
            this.clients.put(repositoryIdByTenant, repository);
            return repository;
        }
        return null;
    }

    private List<DepositSourceEntity> getDepositSources() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : "";
        DepositSourcesCacheService.DepositSourceCacheValue cacheValue = this.depositSourcesCacheService.lookup(this.depositSourcesCacheService.buildKey(tenantCode));
        if (cacheValue == null) {
            List<DepositSourceEntity> depositSourceEntities = new ArrayList<>(this.depositProperties.getSources());
            if (this.tenantScope.isSet() && this.tenantScope.isMultitenant()) {
                TenantConfigurationQuery tenantConfigurationQuery = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.DepositPlugins);
                if (this.tenantScope.isDefaultTenant()) tenantConfigurationQuery.tenantIsSet(false);
                else tenantConfigurationQuery.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());
                TenantConfigurationEntity tenantConfiguration = tenantConfigurationQuery.firstAs(new BaseFieldSet().ensure(TenantConfiguration._depositPlugins));

                if (tenantConfiguration != null && !this.conventionService.isNullOrEmpty(tenantConfiguration.getValue())) {
                    DepositTenantConfigurationEntity depositTenantConfiguration = this.jsonHandlingService.fromJsonSafe(DepositTenantConfigurationEntity.class, tenantConfiguration.getValue());
                    if (depositTenantConfiguration != null) {
                        if (depositTenantConfiguration.getDisableSystemSources()) depositSourceEntities = new ArrayList<>();
                        depositSourceEntities.addAll(this.buildDepositSourceItems(depositTenantConfiguration.getSources()));
                    }
                }
            }
            cacheValue = new DepositSourcesCacheService.DepositSourceCacheValue(tenantCode, depositSourceEntities);
            this.depositSourcesCacheService.put(cacheValue);
        }
        return cacheValue.getSources();
    }

    @EventListener
    public void handleTenantConfigurationTouchedEvent(TenantConfigurationTouchedEvent event) {
        if (!event.getType().equals(TenantConfigurationType.DepositPlugins)) return;
        DepositSourcesCacheService.DepositSourceCacheValue depositSourceCacheValue = this.depositSourcesCacheService.lookup(this.depositSourcesCacheService.buildKey(event.getTenantCode()));
        if (depositSourceCacheValue != null && depositSourceCacheValue.getSources() != null){
            for (DepositSourceEntity source : depositSourceCacheValue.getSources()){
                String repositoryIdByTenant = source.getRepositoryId() + "_" + event.getTenantCode();
                this.clients.remove(repositoryIdByTenant);
                this.depositConfigurationCacheService.evict(this.depositConfigurationCacheService.buildKey(source.getRepositoryId(), event.getTenantCode()));
            }
        }
        this.depositSourcesCacheService.evict(this.depositSourcesCacheService.buildKey(event.getTenantCode()));
    }

    private List<DepositSourceEntity> buildDepositSourceItems(List<DepositSourceEntity> sources) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        List<DepositSourceEntity> items = new ArrayList<>();
        if (this.conventionService.isListNullOrEmpty(sources)) return items;
        for (DepositSourceEntity source : sources){
            DepositSourceEntity item = new DepositSourceEntity();
            item.setRepositoryId(source.getRepositoryId());
            item.setUrl(source.getUrl());
            item.setIssuerUrl(source.getIssuerUrl());
            item.setClientId(source.getClientId());
            if (!this.conventionService.isNullOrEmpty(source.getClientSecret())) item.setClientSecret(this.encryptionService.decryptAES(source.getClientSecret(), this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv()));
            item.setScope(source.getScope());
            item.setRdaTransformerId(source.getRdaTransformerId());
            item.setPdfTransformerId(source.getPdfTransformerId());
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
    public List<org.opencdmp.model.deposit.DepositConfiguration> getAvailableConfigurations(FieldSet fieldSet) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.authorizationService.authorizeForce(Permission.BrowseDeposit, Permission.DeferredAffiliation);
        
        List<org.opencdmp.model.deposit.DepositConfiguration> configurations = new ArrayList<>();

	    for (DepositSourceEntity depositSource : this.getDepositSources()) {

            String tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : "";
            DepositConfigurationCacheService.DepositConfigurationCacheValue cacheValue = this.depositConfigurationCacheService.lookup(this.depositConfigurationCacheService.buildKey(depositSource.getRepositoryId(), tenantCode));
            if (cacheValue == null){
                try {
                    DepositClient depositClient = this.getDepositClient(depositSource.getRepositoryId());
                    if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{depositSource.getRepositoryId(), DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                    DepositConfiguration configuration = depositClient.getConfiguration();
                    cacheValue = new DepositConfigurationCacheService.DepositConfigurationCacheValue(depositSource.getRepositoryId(), tenantCode, configuration);
                    this.depositConfigurationCacheService.put(cacheValue);
                }catch (Exception e){
                    logger.error(e.getMessage(), e);
                }
            }
            if (cacheValue != null) {
                org.opencdmp.model.deposit.DepositConfiguration depositConfiguration = this.builderFactory.builder(DepositConfigurationBuilder.class).build(fieldSet, cacheValue.getConfiguration());
                configurations.add(depositConfiguration);
            }
	    }

        return configurations;
    }

    @Override
    public EntityDoi deposit(DepositRequest planDepositModel) throws Exception {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(planDepositModel.getPlanId())), Permission.DepositPlan);
        //GK: First get the right client
        DepositClient depositClient = this.getDepositClient(planDepositModel.getRepositoryId());
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDepositModel.getRepositoryId(), DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        //GK: Second get the Target Data Management Plan
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planDepositModel.getPlanId()).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDepositModel.getPlanId(), PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        //GK: Forth make the required files to be uploaded with the deposit
        //TODO: Properly create required files
        DepositSourceEntity source = this.getDepositSources().stream().filter(depositSource -> depositSource.getRepositoryId().equals(planDepositModel.getRepositoryId())).findFirst().orElse(null);
        if (source == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDepositModel.getRepositoryId(), DepositSourceEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String accessToken = null;
        if (!this.conventionService.isNullOrEmpty(planDepositModel.getAuthorizationCode())) {
            DepositAuthenticateRequest authenticateRequest = new DepositAuthenticateRequest();
            authenticateRequest.setRepositoryId(planDepositModel.getRepositoryId());
            authenticateRequest.setCode(planDepositModel.getAuthorizationCode());
            accessToken = this.authenticate(authenticateRequest);
        }
        
        org.opencdmp.model.file.FileEnvelope pdfFile = this.fileTransformerService.exportPlan(planEntity.getId(), source.getPdfTransformerId(),"pdf", false);
        org.opencdmp.model.file.FileEnvelope rda = this.fileTransformerService.exportPlan(planEntity.getId(), source.getRdaTransformerId(),"json", false);

        FileEnvelopeModel pdfEnvelope = new FileEnvelopeModel();
        FileEnvelopeModel jsonEnvelope = new FileEnvelopeModel();

        pdfEnvelope.setFilename(pdfFile.getFilename());
        jsonEnvelope.setMimeType("application/pdf");
        jsonEnvelope.setFilename(rda.getFilename());
        jsonEnvelope.setMimeType("application/json");
        if (!depositClient.getConfiguration().isUseSharedStorage()){
            pdfEnvelope.setFile(pdfFile.getFile());
            jsonEnvelope.setFile(rda.getFile());
        } else {
            pdfEnvelope.setFileRef(this.addFileToSharedStorage(pdfFile));
            jsonEnvelope.setFileRef(this.addFileToSharedStorage(rda));
        }

        //GK: Fifth Transform them to the DepositModel
        PlanModel depositModel = this.builderFactory.builder(PlanCommonModelBuilder.class).useSharedStorage(depositClient.getConfiguration().isUseSharedStorage()).authorize(AuthorizationFlags.All)
                .setRepositoryId(planDepositModel.getRepositoryId()).setPdfFile(pdfEnvelope).setRdaJsonFile(jsonEnvelope).build(planEntity);

        String doi;
        try {
            //GK: Sixth Perform the deposit
            doi = depositClient.deposit(depositModel, accessToken);
        } catch (Exception e) {
            log.error("deposit failed for plan model: " + this.jsonHandlingService.toJsonSafe(depositModel));
            throw e;
        }

        //GK: Something has gone wrong return null
        if (doi.isEmpty()) return null;
        //GK: doi is fine store it in database
        EntityDoiPersist doiPersist = new EntityDoiPersist();
        doiPersist.setRepositoryId(planDepositModel.getRepositoryId());
        doiPersist.setDoi(doi);
        doiPersist.setEntityId(planEntity.getId());
        this.sendNotification(planEntity);

        this.accountingService.increase(UsageLimitTargetMetric.DEPOSIT_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.DEPOSIT_EXECUTION_COUNT_FOR, planDepositModel.getRepositoryId());

        return this.doiService.persist(doiPersist, true, planDepositModel.getProject());
    }

    private void sendNotification(PlanEntity planEntity) throws InvalidApplicationException {
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(planEntity.getId()).isActives(IsActive.Active).collect();
        if (this.conventionService.isListNullOrEmpty(planUsers)){
            throw new MyNotFoundException("Plan does not have Users");
        }

        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).collect(Collectors.toList())).isActive(IsActive.Active).collect();

        for (UserEntity user: users) {
            if (!user.getId().equals(this.userScope.getUserIdSafe()) && !this.conventionService.isListNullOrEmpty(planUsers.stream().filter(x -> x.getUserId().equals(user.getId())).collect(Collectors.toList()))){
                this.createPlanDepositNotificationEvent(planEntity, user);
            }
        }
    }

    private void createPlanDepositNotificationEvent(PlanEntity plan, UserEntity user) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getPlanDepositType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));

        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

	    this.eventHandler.handle(event);
    }
    
    private String addFileToSharedStorage(org.opencdmp.model.file.FileEnvelope file) throws IOException {
        StorageFilePersist storageFilePersist = new StorageFilePersist();
        storageFilePersist.setName(FilenameUtils.removeExtension(file.getFilename()));
        storageFilePersist.setExtension(FilenameUtils.getExtension(file.getFilename()));
        storageFilePersist.setMimeType(URLConnection.guessContentTypeFromName(file.getFilename()));
        storageFilePersist.setOwnerId(this.userScope.getUserIdSafe());
        storageFilePersist.setStorageType(StorageType.Temp);
        storageFilePersist.setLifetime(Duration.ofSeconds(this.storageFileProperties.getTempStoreLifetimeSeconds())); //TODO
        this.validatorFactory.validator(StorageFilePersist.StorageFilePersistValidator.class).validateForce(storageFilePersist);
        StorageFile persisted = this.storageFileService.persistBytes(storageFilePersist, file.getFile(), new BaseFieldSet(StorageFile._id, StorageFile._fileRef));
        return persisted.getFileRef();
    }

    private void increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric metric, String repositoryId) throws InvalidApplicationException {
        this.accountingService.increase(metric.getValue() + repositoryId);
    }

    @Override
    public String getLogo(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.authorizationService.authorizeForce(Permission.BrowseDeposit, Permission.DeferredAffiliation);
        
        DepositClient depositClient = this.getDepositClient(repositoryId);
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        return depositClient.getLogo();
    }

    private String authenticate(DepositAuthenticateRequest model) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.authorizationService.authorizeForce(Permission.BrowseDeposit, Permission.DeferredAffiliation);
        
        DepositClient depositClient = this.getDepositClient(model.getRepositoryId());
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getRepositoryId(), DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        return depositClient.authenticate(model.getCode());
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

}
