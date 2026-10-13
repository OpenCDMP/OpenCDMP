package org.opencdmp.service.deposit;

import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.service.semantic.PluginSemantics;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeCacheService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeFilterFunction;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeModel;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolverFactory;
import org.opencdmp.commonmodels.models.ConfigurationField;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.commonmodels.models.plugin.PluginUserFieldModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionEntity;
import org.opencdmp.commons.types.description.PropertyDefinitionFieldSetItemEntity;
import org.opencdmp.commons.types.descriptiontemplate.FieldEntity;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.commons.types.plan.PlanPropertiesEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationUserFieldEntity;
import org.opencdmp.commons.types.tenantconfiguration.DepositTenantConfigurationEntity;
import org.opencdmp.commons.types.tenantconfiguration.PluginTenantConfigurationEntity;
import org.opencdmp.commons.types.pluginconfiguration.PluginConfigurationEntity;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.commons.types.user.AdditionalInfoEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.*;
import org.opencdmp.commonmodels.models.deposit.DepositPreviewModel;
import org.opencdmp.commonmodels.models.deposit.PreviewFieldOption;
import org.opencdmp.commonmodels.models.deposit.PreviewGroupValuesModel;
import org.opencdmp.commonmodels.models.deposit.StoredPreviewFieldModel;
import org.opencdmp.commons.types.deposit.DepositMetadataPayloadEntity;
import org.opencdmp.depositbase.repository.AuthInfo;
import org.opencdmp.depositbase.repository.DepositClient;
import org.opencdmp.depositbase.repository.DepositConfiguration;
import org.opencdmp.depositbase.repository.PlanDepositModel;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.event.TenantConfigurationTouchedEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.DepositConfigurationBuilder;
import org.opencdmp.model.builder.commonmodels.plan.PlanCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.plugin.PluginUserFieldCommonModelBuilder;
import org.opencdmp.model.deposit.DepositAuthMethodResult;
import org.opencdmp.model.deposit.DepositFile;
import org.opencdmp.depositbase.enums.DepositFileKind;
import org.opencdmp.depositbase.repository.DepositPluginSettings;
import org.opencdmp.commonmodels.models.plugin.PluginFieldModel;
import org.opencdmp.commonmodels.models.plugin.PluginModel;
import org.opencdmp.model.persist.EntityDoiPersist;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.model.persist.deposit.DepositAuthenticateRequest;
import org.opencdmp.model.persist.deposit.DepositPreviewRequest;
import org.opencdmp.model.persist.deposit.DepositRequest;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.model.user.User;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.pluginconfiguration.PluginConfigurationService;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserFieldPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserPersist;
import org.opencdmp.service.entitydoi.EntityDoiService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.storage.StorageFileProperties;
import org.opencdmp.service.storage.StorageFileService;
import org.opencdmp.service.webhook.WebhookService;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DepositServiceImpl implements DepositService {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DepositServiceImpl.class));
    private static final Logger log = LoggerFactory.getLogger(DepositServiceImpl.class);

    /** Used when a source declares no response size limit, e.g. one a tenant added through the UI. */
    private static final int DEFAULT_MAX_IN_MEMORY_SIZE_IN_BYTES = 6554000;

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
    private final UserScopeFactory userScopeFactory;
    private final ValidatorFactory validatorFactory;
    private final StorageFileProperties storageFileProperties;
    private final AuthorizationContentResolverFactory authorizationContentResolverFactory;
    private final ConventionService conventionService;
    private final JsonHandlingService jsonHandlingService;
    private final NotificationProperties notificationProperties;
    private final NotifyIntegrationEventHandler eventHandler;
    private final TenantScopeFactory tenantScopeFactory;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private final DepositSourcesCacheService depositSourcesCacheService;
    private final AccountingService accountingService;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final XmlHandlingService xmlHandlingService;
    private final ErrorThesaurusProperties errors;
    private final WebhookService webhookService;
    private final PluginConfigurationService pluginConfigurationService;
    
    @Autowired
    public DepositServiceImpl(DepositProperties depositProperties,
                              TokenExchangeCacheService tokenExchangeCacheService,
                              AuthorizationService authorizationService,
                              EntityDoiService doiService,
                              QueryFactory queryFactory,
                              MessageSource messageSource,
                              BuilderFactory builderFactory, DepositConfigurationCacheService depositConfigurationCacheService, FileTransformerService fileTransformerService, StorageFileService storageFileService, UserScopeFactory userScopeFactory, ValidatorFactory validatorFactory, StorageFileProperties storageFileProperties, AuthorizationContentResolverFactory authorizationContentResolverFactory, ConventionService conventionService, JsonHandlingService jsonHandlingService, NotificationProperties notificationProperties, NotifyIntegrationEventHandler eventHandler, TenantScopeFactory tenantScopeFactory, EncryptionService encryptionService, TenantProperties tenantProperties, DepositSourcesCacheService depositSourcesCacheService, AccountingService accountingService, TenantEntityManagerFactory tenantEntityManagerFactory, XmlHandlingService xmlHandlingService, ErrorThesaurusProperties errors, WebhookService webhookService, PluginConfigurationService pluginConfigurationService) {
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
	    this.userScopeFactory = userScopeFactory;
	    this.validatorFactory = validatorFactory;
	    this.storageFileProperties = storageFileProperties;
	    this.authorizationContentResolverFactory = authorizationContentResolverFactory;
        this.conventionService = conventionService;
        this.jsonHandlingService = jsonHandlingService;
        this.notificationProperties = notificationProperties;
        this.eventHandler = eventHandler;
	    this.tenantScopeFactory = tenantScopeFactory;
	    this.encryptionService = encryptionService;
	    this.tenantProperties = tenantProperties;
	    this.depositSourcesCacheService = depositSourcesCacheService;
        this.accountingService = accountingService;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.xmlHandlingService = xmlHandlingService;
        this.errors = errors;
        this.webhookService = webhookService;
        this.pluginConfigurationService = pluginConfigurationService;
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
            WebClient webClient = WebClient.builder().baseUrl(StringUtils.removeEnd(source.getUrl(), "/") + "/api/deposit")
                    .filters(exchangeFilterFunctions -> {
                        exchangeFilterFunctions.add(apiKeyExchangeFilterFunction);
                        exchangeFilterFunctions.add(logRequest());
                        exchangeFilterFunctions.add(logResponse());
                    }).codecs(codecs -> codecs
                            .defaultCodecs()
                            .maxInMemorySize(source.getMaxInMemorySizeInBytes() > 0 ? source.getMaxInMemorySizeInBytes() : DEFAULT_MAX_IN_MEMORY_SIZE_IN_BYTES)
                    ).build();
	        DepositClientImpl repository = new DepositClientImpl(webClient);
            this.clients.put(repositoryIdByTenant, repository);
            return repository;
        }
        return null;
    }

    private List<DepositSourceEntity> getDepositSources() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String tenantCode = this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant() ? this.tenantScopeFactory.getInstance().getTenantCode() : "";
        DepositSourcesCacheService.DepositSourceCacheValue cacheValue = this.depositSourcesCacheService.lookup(this.depositSourcesCacheService.buildKey(tenantCode));
        if (cacheValue == null) {
            List<DepositSourceEntity> depositSourceEntities = new ArrayList<>(this.depositProperties.getSources());
            if (this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant()) {
                TenantConfigurationQuery tenantConfigurationQuery = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.DepositPlugins);
                if (this.tenantScopeFactory.getInstance().isDefaultTenant()) tenantConfigurationQuery.tenantIsSet(false);
                else tenantConfigurationQuery.tenantIsSet(true).tenantIds(this.tenantScopeFactory.getInstance().getTenant());
                TenantConfigurationEntity tenantConfiguration = tenantConfigurationQuery.firstAs(new BaseFieldSet().ensure(TenantConfiguration._depositPlugins));

                if (tenantConfiguration != null && !this.conventionService.isNullOrEmpty(tenantConfiguration.getValue())) {
                    DepositTenantConfigurationEntity depositTenantConfiguration = this.jsonHandlingService.fromJsonSafe(DepositTenantConfigurationEntity.class, tenantConfiguration.getValue());
                    if (depositTenantConfiguration != null) {
                        if (depositTenantConfiguration.getDisableSystemSources()) depositSourceEntities = new ArrayList<>();
                        List<DepositSourceEntity> tenantSources = this.buildDepositSourceItems(depositTenantConfiguration.getSources());
                        // a tenant's source replaces the installation's one with the same repository id
                        depositSourceEntities.removeIf(x -> tenantSources.stream().anyMatch(y -> y.getRepositoryId().equals(x.getRepositoryId())));
                        depositSourceEntities.addAll(tenantSources);
                        if (depositTenantConfiguration.getDisabledSourceIds() != null) depositSourceEntities.removeIf(x -> depositTenantConfiguration.getDisabledSourceIds().contains(x.getRepositoryId()));
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
            item.setMaxInMemorySizeInBytes(source.getMaxInMemorySizeInBytes());
            items.add(item);
        }
        return items;
    }

    private String getRepositoryIdByTenant(String repositoryId) throws InvalidApplicationException {
        if (this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant()) {
            return repositoryId + "_" + this.tenantScopeFactory.getInstance().getTenantCode();
        } else {
            return repositoryId;
        }
    }


    @Override
    public List<org.opencdmp.model.deposit.DepositConfiguration> getAvailableConfigurations(FieldSet fieldSet) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        this.authorizationService.authorizeForce(Permission.BrowseDeposit, Permission.DeferredAffiliation);

        List<org.opencdmp.model.deposit.DepositConfiguration> configurations = new ArrayList<>();
        for (DepositConfiguration configuration : this.getAvailableRawConfigurations()) {
            configurations.add(this.builderFactory.builder(DepositConfigurationBuilder.class).build(fieldSet, configuration));
        }
        return configurations;
    }

    @Override
    public List<PluginSemantics> getSemanticsManifests() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return this.getAvailableRawConfigurations().stream()
                .filter(x -> x != null && x.getSemantics() != null)
                .map(x -> new PluginSemantics(PluginType.Deposit, x.getRepositoryId(), x.getSemantics()))
                .toList();
    }

    private List<DepositConfiguration> getAvailableRawConfigurations() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        List<DepositConfiguration> configurations = new ArrayList<>();

	    for (DepositSourceEntity depositSource : this.getDepositSources()) {

            String tenantCode = this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant() ? this.tenantScopeFactory.getInstance().getTenantCode() : "";
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
            if (cacheValue != null && cacheValue.getConfiguration() != null) {
                configurations.add(cacheValue.getConfiguration());
            }
	    }

        return configurations;
    }

    @Override
    public EntityDoi deposit(DepositRequest planDepositModel) throws Exception {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().planAffiliation(planDepositModel.getPlanId())), Permission.DepositPlan);
        //GK: First get the right client
        DepositClient depositClient = this.getDepositClient(planDepositModel.getRepositoryId());
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDepositModel.getRepositoryId(), DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        //GK: Second get the Target Data Management Plan
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planDepositModel.getPlanId()).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDepositModel.getPlanId(), PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().types(EntityType.Plan).entityIds(planEntity.getId()).repositoryIds(planDepositModel.getRepositoryId()).isActive(IsActive.Active);
        if (entityDoiQuery.count() > 0) throw new MyValidationException(this.errors.getCannotPlanDeposit().getCode(), planDepositModel.getRepositoryId());

        //GK: Forth make the required files to be uploaded with the deposit
        //TODO: Properly create required files
        DepositSourceEntity source = this.getDepositSources().stream().filter(depositSource -> depositSource.getRepositoryId().equals(planDepositModel.getRepositoryId())).findFirst().orElse(null);
        if (source == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planDepositModel.getRepositoryId(), DepositSourceEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        if (!this.isAllowed(this.tenantDepositSettings(depositClient.getConfiguration()), planDepositModel.getDepositAuthInfoType())) throw new MyValidationException(this.errors.getDepositWayNotAllowed().getCode(), this.errors.getDepositWayNotAllowed().getMessage());

        PlanDepositModel model = new PlanDepositModel();
        AuthInfo authInfo = new AuthInfo();
        if (!this.conventionService.isNullOrEmpty(planDepositModel.getAuthorizationCode()) && planDepositModel.getDepositAuthInfoType().equals(DepositAuthMethod.oAuth2Flow)) {
            DepositAuthenticateRequest authenticateRequest = new DepositAuthenticateRequest();
            authenticateRequest.setRepositoryId(planDepositModel.getRepositoryId());
            authenticateRequest.setCode(planDepositModel.getAuthorizationCode());
            authInfo.setAuthToken(this.authenticate(authenticateRequest));
            if (this.conventionService.isNullOrEmpty(authInfo.getAuthToken())) throw new MyValidationException(this.errors.getDepositSignInFailed().getCode(), this.errors.getDepositSignInFailed().getMessage());
        } else if (planDepositModel.getDepositAuthInfoType().equals(DepositAuthMethod.oAuth2Flow)) {
            throw new MyValidationException(this.errors.getDepositSignInFailed().getCode(), this.errors.getDepositSignInFailed().getMessage());
        } else if (planDepositModel.getDepositAuthInfoType().equals(DepositAuthMethod.AuthInfoFromUserProfile) && !this.conventionService.isListNullOrEmpty(planDepositModel.getAuthFields())) {
            List<String> configurationCodes = this.authInfoCodes(depositClient);
            authInfo.setAuthFields(planDepositModel.getAuthFields().stream().filter(x -> configurationCodes.contains(x.getCode()) && !this.conventionService.isNullOrEmpty(x.getTextValue())).map(x -> {
                PluginUserFieldModel field = new PluginUserFieldModel();
                field.setCode(x.getCode());
                field.setTextValue(x.getTextValue());
                return field;
            }).toList());
        } else if (planDepositModel.getDepositAuthInfoType().equals(DepositAuthMethod.AuthInfoFromUserProfile)){
            //Get User Plugin Configurations
            UserEntity user = this.queryFactory.query(UserQuery.class).ids(this.userScopeFactory.getInstance().getUserIdSafe()).disableTracking().firstAs(new BaseFieldSet().ensure(User._name).ensure(User._additionalInfo));
            if (user == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{this.userScopeFactory.getInstance().getUserIdSafe(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            AdditionalInfoEntity additionalInfo = this.conventionService.isNullOrEmpty(user.getAdditionalInfo()) ? null : this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, user.getAdditionalInfo());
            if (additionalInfo != null && !this.conventionService.isListNullOrEmpty(additionalInfo.getPluginConfigurations())) {
                PluginConfigurationUserEntity pluginConfigurationUserEntity = additionalInfo.getPluginConfigurations().stream().filter(x -> x.getPluginCode().equals(planDepositModel.getRepositoryId()) && x.getPluginType().equals(PluginType.Deposit)).findFirst().orElse(null);
                if(pluginConfigurationUserEntity != null && depositClient.getConfiguration() != null && !this.conventionService.isListNullOrEmpty(depositClient.getConfiguration().getUserConfigurationFields())) {
                    List<PluginUserFieldModel> authFields = this.builderFactory.builder(PluginUserFieldCommonModelBuilder.class).useSharedStorage(depositClient.getConfiguration().isUseSharedStorage()).authorize(AuthorizationFlags.All).build(pluginConfigurationUserEntity.getUserFields());
                    List<String> configurationCodes = depositClient.getConfiguration().getUserConfigurationFields().stream().filter(ConfigurationField::isAuthInfo).map(ConfigurationField::getCode).toList();
                    authInfo.setAuthFields(authFields.stream().filter(x ->  configurationCodes.contains(x.getCode())).toList());
                }
            }
        }
        if (planDepositModel.getDepositAuthInfoType().equals(DepositAuthMethod.AuthInfoFromUserProfile)) {
            List<String> configurationCodes = this.authInfoCodes(depositClient);
            List<String> provided = authInfo.getAuthFields() == null ? List.of() : authInfo.getAuthFields().stream().filter(x -> !this.conventionService.isNullOrEmpty(x.getTextValue())).map(PluginUserFieldModel::getCode).toList();
            if (configurationCodes.isEmpty() || !provided.containsAll(configurationCodes)) throw new MyValidationException(this.errors.getDepositCredentialsMissing().getCode(), this.errors.getDepositCredentialsMissing().getMessage());
        }
        model.setAuthInfo(authInfo);
        
        List<DepositFileKind> uploadedFiles = this.uploadedFiles(depositClient);
        FileEnvelopeModel pdfEnvelope = null;
        FileEnvelopeModel jsonEnvelope = null;
        if (uploadedFiles.contains(DepositFileKind.PlanDocument)) {
            org.opencdmp.model.file.FileEnvelope pdfFile = this.fileTransformerService.exportPlanInternal(planEntity.getId(), source.getPdfTransformerId(),"pdf");
            pdfEnvelope = new FileEnvelopeModel();
            pdfEnvelope.setFilename(pdfFile.getFilename());
            pdfEnvelope.setMimeType("application/pdf");
            if (!depositClient.getConfiguration().isUseSharedStorage()) pdfEnvelope.setFile(pdfFile.getFile());
            else pdfEnvelope.setFileRef(this.addFileToSharedStorage(pdfFile));
        }
        if (uploadedFiles.contains(DepositFileKind.RdaJson)) {
            org.opencdmp.model.file.FileEnvelope rda = this.fileTransformerService.exportPlanInternal(planEntity.getId(), source.getRdaTransformerId(),"json");
            jsonEnvelope = new FileEnvelopeModel();
            jsonEnvelope.setFilename(rda.getFilename());
            jsonEnvelope.setMimeType("application/json");
            if (!depositClient.getConfiguration().isUseSharedStorage()) jsonEnvelope.setFile(rda.getFile());
            else jsonEnvelope.setFileRef(this.addFileToSharedStorage(rda));
        }

        //GK: Fifth Transform them to the DepositModel
        PlanModel depositModel = this.builderFactory.builder(PlanCommonModelBuilder.class).useSharedStorage(depositClient.getConfiguration().isUseSharedStorage()).authorize(AuthorizationFlags.All)
                .setRepositoryId(planDepositModel.getRepositoryId()).setPdfFile(pdfEnvelope).setRdaJsonFile(jsonEnvelope).setSupportingFilesZip(uploadedFiles.contains(DepositFileKind.SupportingFiles) ? this.buildSupportingFilesZip(planEntity) : null).build(planEntity);
        model.setPlanModel(depositModel);
        if (planDepositModel.getStoredFields() != null && !planDepositModel.getStoredFields().isEmpty()) {
            model.setEditedFields(planDepositModel.getStoredFields().stream().map(StoredPreviewFieldModel::getValue).filter(Objects::nonNull).toList());
        }
        model.setEditedGroups(planDepositModel.getStoredGroups());

        String doi;
        try {
            //GK: Sixth Perform the deposit
            doi = depositClient.deposit(model);
        } catch (Exception e) {
            // the model carries the plugin settings, decrypted credentials included: name the plan, never dump it
            log.error("deposit failed for plan " + planEntity.getId() + " to " + planDepositModel.getRepositoryId(), e);
            throw e;
        }

        //GK: Something has gone wrong return null
        if (doi.isEmpty()) return null;

        this.persistDepositMetadata(planEntity.getId(), planDepositModel.getRepositoryId(), planDepositModel.getStoredFields(), planDepositModel.getStoredGroups());
        if (Boolean.TRUE.equals(planDepositModel.getSaveAuthFields()) && planDepositModel.getDepositAuthInfoType().equals(DepositAuthMethod.AuthInfoFromUserProfile) && !this.conventionService.isListNullOrEmpty(planDepositModel.getAuthFields())) {
            this.saveUserAuthFields(planDepositModel.getRepositoryId(), planDepositModel.getAuthFields());
        }

        //GK: doi is fine store it in database
        EntityDoiPersist doiPersist = new EntityDoiPersist();
        doiPersist.setRepositoryId(planDepositModel.getRepositoryId());
        doiPersist.setDoi(doi);
        doiPersist.setEntityId(planEntity.getId());
        this.sendNotification(planEntity, planDepositModel.getRepositoryId());
        this.webhookService.enqueueEvent(WebhookEventType.PlanDeposited, Map.of(
                "planId", planEntity.getId(),
                "label", planEntity.getLabel() != null ? planEntity.getLabel() : "",
                "repositoryId", planDepositModel.getRepositoryId(),
                "doi", doi != null ? doi : ""));

        this.accountingService.increase(UsageLimitTargetMetric.DEPOSIT_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.DEPOSIT_EXECUTION_COUNT_FOR, planDepositModel.getRepositoryId());

        entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().types(EntityType.Plan).entityIds(planEntity.getId()).isActive(IsActive.Active);
        if (entityDoiQuery.count() == 0) {
            this.accountingService.increase(UsageLimitTargetMetric.PLAN_DOIED_COUNT.getValue());
            Long descriptionsCount = this.queryFactory.query(DescriptionQuery.class).authorize(AuthorizationFlags.AllExceptPublic).planIds(planEntity.getId()).isActive(IsActive.Active).count();
            for (long i = 0; i < descriptionsCount; i++) {
                this.accountingService.increase(UsageLimitTargetMetric.DESCRIPTION_DOIED_COUNT.getValue());
            }
        }
        return this.doiService.persist(doiPersist, true, planDepositModel.getProject());
    }

    @Override
    public DepositPreviewModel preview(DepositPreviewRequest previewRequest) throws Exception {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().planAffiliation(previewRequest.getPlanId())), Permission.DepositPlan);

        DepositClient depositClient = this.getDepositClient(previewRequest.getRepositoryId());
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{previewRequest.getRepositoryId(), DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DepositConfiguration configuration = depositClient.getConfiguration();
        if (configuration == null || !configuration.isSupportsPreview()) throw new MyApplicationException("preview is not supported by repository: " + previewRequest.getRepositoryId());

        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(previewRequest.getPlanId()).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{previewRequest.getPlanId(), PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        // name-only envelopes: previews skip file generation for speed, but plugins can still
        // present what will be uploaded; names are predicted from the export naming convention
        List<DepositFileKind> uploadedFiles = this.uploadedFiles(depositClient);
        String exportBaseName = this.exportBaseName(planEntity);
        FileEnvelopeModel pdfNameOnly = null;
        if (uploadedFiles.contains(DepositFileKind.PlanDocument)) {
            pdfNameOnly = new FileEnvelopeModel();
            pdfNameOnly.setFilename(exportBaseName + ".pdf");
        }
        FileEnvelopeModel jsonNameOnly = null;
        if (uploadedFiles.contains(DepositFileKind.RdaJson)) {
            jsonNameOnly = new FileEnvelopeModel();
            jsonNameOnly.setFilename(exportBaseName + ".json");
        }
        FileEnvelopeModel zipNameOnly = null;
        if (uploadedFiles.contains(DepositFileKind.SupportingFiles) && !this.collectSupportingFiles(planEntity).isEmpty()) {
            zipNameOnly = new FileEnvelopeModel();
            zipNameOnly.setFilename(exportBaseName.replaceAll("\\.zip$", "") + ".zip");
        }

        PlanModel previewPlanModel = this.builderFactory.builder(PlanCommonModelBuilder.class).useSharedStorage(configuration.isUseSharedStorage()).authorize(AuthorizationFlags.All)
                .setRepositoryId(previewRequest.getRepositoryId()).setPdfFile(pdfNameOnly).setRdaJsonFile(jsonNameOnly).setSupportingFilesZip(zipNameOnly).build(planEntity);

        PlanDepositModel model = new PlanDepositModel();
        model.setPlanModel(previewPlanModel);

        DepositMetadataPayloadEntity storedPayload = this.loadDepositMetadata(planEntity, previewRequest.getRepositoryId());
        if (storedPayload != null) {
            model.setStoredFields(storedPayload.getFields());
            model.setStoredGroups(storedPayload.getGroups());
        }

        return depositClient.preview(model);
    }

    @Override
    public List<PreviewFieldOption> searchVocabulary(String repositoryId, UUID planId, String fieldCode, String like) throws Exception {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().planAffiliation(planId)), Permission.DepositPlan);

        DepositClient depositClient = this.getDepositClient(repositoryId);
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        return depositClient.searchVocabulary(fieldCode, like);
    }

    private List<String> authInfoCodes(DepositClient depositClient) {
        if (depositClient.getConfiguration() == null || this.conventionService.isListNullOrEmpty(depositClient.getConfiguration().getUserConfigurationFields())) return List.of();
        return depositClient.getConfiguration().getUserConfigurationFields().stream().filter(ConfigurationField::isAuthInfo).map(ConfigurationField::getCode).toList();
    }

    private void saveUserAuthFields(String repositoryId, List<PluginConfigurationUserFieldPersist> fields) {
        try {
            UserEntity user = this.queryFactory.query(UserQuery.class).ids(this.userScopeFactory.getInstance().getUserIdSafe()).first();
            if (user == null) return;
            AdditionalInfoEntity additionalInfo = this.conventionService.isNullOrEmpty(user.getAdditionalInfo()) ? null : this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, user.getAdditionalInfo());
            if (additionalInfo == null) additionalInfo = new AdditionalInfoEntity();
            if (additionalInfo.getPluginConfigurations() == null) additionalInfo.setPluginConfigurations(new ArrayList<>());
            PluginConfigurationUserEntity existing = additionalInfo.getPluginConfigurations().stream().filter(x -> repositoryId.equals(x.getPluginCode()) && PluginType.Deposit.equals(x.getPluginType())).findFirst().orElse(null);

            PluginConfigurationUserPersist persist = new PluginConfigurationUserPersist();
            persist.setPluginCode(repositoryId);
            persist.setPluginType(PluginType.Deposit);
            persist.setUserFields(fields);
            PluginConfigurationUserEntity saved = this.pluginConfigurationService.buildPluginConfigurationUserEntity(persist, existing != null ? List.of(existing) : null);
            if (existing != null && existing.getUserFields() != null) {
                existing.getUserFields().stream().filter(x -> saved.getUserFields().stream().noneMatch(y -> y.getCode().equals(x.getCode()))).forEach(saved.getUserFields()::add);
                additionalInfo.getPluginConfigurations().remove(existing);
            }
            additionalInfo.getPluginConfigurations().add(saved);

            user.setAdditionalInfo(this.jsonHandlingService.toJson(additionalInfo));
            user.setUpdatedAt(Instant.now());
            this.tenantEntityManagerFactory.getInstance().merge(user);
            this.tenantEntityManagerFactory.getInstance().flush();
        } catch (Exception e) {
            // saving the credentials for next time must never fail a successful deposit
            logger.error("failed to save the deposit credentials of " + repositoryId + " to the user's profile", e);
        }
    }

    private void persistDepositMetadata(UUID planId, String repositoryId, List<StoredPreviewFieldModel> storedFields, List<PreviewGroupValuesModel> storedGroups) {
        boolean hasFields = storedFields != null && !storedFields.isEmpty();
        boolean hasGroups = storedGroups != null && !storedGroups.isEmpty();
        if (!hasFields && !hasGroups) return;

        try {
            DepositMetadataPayloadEntity payload = new DepositMetadataPayloadEntity();
            payload.setFields(hasFields ? storedFields : null);
            payload.setGroups(hasGroups ? storedGroups : null);
            String payloadJson = this.jsonHandlingService.toJson(payload);

            DepositMetadataEntity existing = this.queryFactory.query(DepositMetadataQuery.class).planIds(planId).repositoryIds(repositoryId).isActive(IsActive.Active).first();
            if (existing != null) {
                existing.setPayload(payloadJson);
                existing.setUpdatedAt(Instant.now());
                this.tenantEntityManagerFactory.getInstance().merge(existing);
            } else {
                DepositMetadataEntity entity = new DepositMetadataEntity();
                entity.setId(UUID.randomUUID());
                entity.setPlanId(planId);
                entity.setRepositoryId(repositoryId);
                entity.setPayload(payloadJson);
                entity.setCreatedAt(Instant.now());
                entity.setUpdatedAt(Instant.now());
                entity.setIsActive(IsActive.Active);
                this.tenantEntityManagerFactory.getInstance().persist(entity);
            }
            this.tenantEntityManagerFactory.getInstance().flush();
        } catch (Exception e) {
            // persisting overrides must never fail a successful deposit; the DOI is already minted
            logger.error("failed to persist deposit metadata overrides for plan " + planId + " / " + repositoryId, e);
        }
    }

    private DepositMetadataPayloadEntity loadDepositMetadata(PlanEntity planEntity, String repositoryId) {
        try {
            List<UUID> planIds;
            if (planEntity.getGroupId() != null) {
                planIds = this.queryFactory.query(PlanQuery.class).disableTracking().groupIds(planEntity.getGroupId()).collectAs(new BaseFieldSet().ensure(org.opencdmp.model.plan.Plan._id)).stream().map(PlanEntity::getId).filter(Objects::nonNull).toList();
            } else {
                planIds = List.of(planEntity.getId());
            }
            if (planIds.isEmpty()) planIds = List.of(planEntity.getId());

            List<DepositMetadataEntity> candidates = this.queryFactory.query(DepositMetadataQuery.class).disableTracking().planIds(planIds).repositoryIds(repositoryId).isActive(IsActive.Active).collect();
            DepositMetadataEntity latest = candidates.stream().filter(x -> x.getPayload() != null).max(Comparator.comparing(DepositMetadataEntity::getUpdatedAt)).orElse(null);
            if (latest == null) return null;
            return this.jsonHandlingService.fromJsonSafe(DepositMetadataPayloadEntity.class, latest.getPayload());
        } catch (Exception e) {
            logger.error("failed to load deposit metadata overrides for plan " + planEntity.getId() + " / " + repositoryId, e);
            return null;
        }
    }

    @Override
    public List<DepositFile> getFiles(String repositoryId, UUID planId) throws Exception {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().planAffiliation(planId)), Permission.DepositPlan);

        DepositClient depositClient = this.getDepositClient(repositoryId);
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        String exportBaseName = this.exportBaseName(planEntity);
        List<DepositFile> files = new ArrayList<>();
        for (DepositFileKind kind : this.uploadedFiles(depositClient)) {
            DepositFile file = new DepositFile();
            file.setKind(kind);
            switch (kind) {
                case PlanDocument -> file.setFilename(exportBaseName + ".pdf");
                case RdaJson -> file.setFilename(exportBaseName + ".json");
                case SupportingFiles -> {
                    List<StorageFileEntity> supportingFiles = this.collectSupportingFiles(planEntity);
                    if (supportingFiles.isEmpty()) continue;
                    file.setFilename(exportBaseName.replaceAll("\\.zip$", "") + ".zip");
                    file.setContents(supportingFiles.stream().map(x -> x.getName() + (x.getExtension().startsWith(".") ? "" : ".") + x.getExtension()).toList());
                }
            }
            files.add(file);
        }
        return files;
    }

    @Override
    public org.opencdmp.model.file.FileEnvelope getFile(String repositoryId, UUID planId, DepositFileKind kind) throws Exception {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().planAffiliation(planId)), Permission.DepositPlan);

        DepositClient depositClient = this.getDepositClient(repositoryId);
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        if (!this.uploadedFiles(depositClient).contains(kind)) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{kind, DepositFileKind.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        DepositSourceEntity source = this.getDepositSources().stream().filter(depositSource -> depositSource.getRepositoryId().equals(repositoryId)).findFirst().orElse(null);
        if (source == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, DepositSourceEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        return switch (kind) {
            case PlanDocument -> this.fileTransformerService.exportPlanInternal(planEntity.getId(), source.getPdfTransformerId(), "pdf");
            case RdaJson -> this.fileTransformerService.exportPlanInternal(planEntity.getId(), source.getRdaTransformerId(), "json");
            case SupportingFiles -> {
                FileEnvelopeModel zip = this.buildSupportingFilesZip(planEntity);
                if (zip == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{kind, DepositFileKind.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                org.opencdmp.model.file.FileEnvelope envelope = new org.opencdmp.model.file.FileEnvelope();
                envelope.setFilename(zip.getFilename());
                envelope.setFile(zip.getFile());
                yield envelope;
            }
        };
    }

    private boolean isAllowed(DepositPluginSettings settings, DepositAuthMethod method) {
        String ways = settings.text(DepositPluginSettings.PUBLISH_WAYS, DepositPluginSettings.PUBLISH_WAYS_BOTH);
        boolean organisation = DepositAuthMethod.PluginDefault.equals(method);
        if (DepositPluginSettings.PUBLISH_WAYS_ORGANISATION.equals(ways)) return organisation;
        if (DepositPluginSettings.PUBLISH_WAYS_OWN.equals(ways)) return !organisation;
        return true;
    }

    /** The plugin's settings as the organisation set them (the default tenant's when it set none), over the plugin's declared defaults. */
    private DepositPluginSettings tenantDepositSettings(DepositConfiguration configuration) throws InvalidApplicationException {
        if (configuration == null) return DepositPluginSettings.of((List<PluginModel>) null, null);
        TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.PluginConfiguration);
        if (this.tenantScopeFactory.getInstance().isDefaultTenant()) query.tenantIsSet(false);
        else query.tenantIsSet(true).tenantIds(this.tenantScopeFactory.getInstance().getTenant());
        TenantConfigurationEntity entity = query.firstAs(new BaseFieldSet().ensure(TenantConfiguration._pluginConfiguration));
        if (entity == null && !this.tenantScopeFactory.getInstance().isDefaultTenant()) {
            entity = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.PluginConfiguration).tenantIsSet(false)
                    .firstAs(new BaseFieldSet().ensure(TenantConfiguration._pluginConfiguration));
        }
        PluginTenantConfigurationEntity value = entity == null || this.conventionService.isNullOrEmpty(entity.getValue()) ? null : this.jsonHandlingService.fromJsonSafe(PluginTenantConfigurationEntity.class, entity.getValue());
        List<PluginModel> plugins = new ArrayList<>();
        if (value != null && value.getPluginConfigurations() != null) {
            for (PluginConfigurationEntity plugin : value.getPluginConfigurations()) {
                if (!PluginType.Deposit.equals(plugin.getPluginType()) || !configuration.getRepositoryId().equals(plugin.getPluginCode())) continue;
                PluginModel model = new PluginModel();
                model.setCode(plugin.getPluginCode());
                model.setType(org.opencdmp.commonmodels.enums.PluginType.Deposit);
                model.setFields(plugin.getFields() == null ? List.of() : plugin.getFields().stream().map(x -> {
                    PluginFieldModel field = new PluginFieldModel();
                    field.setCode(x.getCode());
                    field.setTextValue(x.getTextValue());
                    return field;
                }).toList());
                plugins.add(model);
            }
        }
        return DepositPluginSettings.of(plugins, configuration);
    }

    /**
     * The files to build: what the plugin uploads (every file when it does not say), narrowed by the organisation's choice,
     * without the plan document or RDA JSON when the organisation switched off the transformer that produces it.
     */
    private List<DepositFileKind> uploadedFiles(DepositClient depositClient) throws Exception {
        DepositConfiguration configuration = depositClient.getConfiguration();
        List<DepositFileKind> files = new ArrayList<>(this.tenantDepositSettings(configuration).depositFiles(configuration != null ? configuration.getUploadedFiles() : null));
        String repositoryId = configuration != null ? configuration.getRepositoryId() : null;
        DepositSourceEntity source = repositoryId == null ? null : this.getDepositSources().stream().filter(x -> repositoryId.equals(x.getRepositoryId())).findFirst().orElse(null);
        if (source != null) {
            if (!this.fileTransformerService.isAvailable(source.getPdfTransformerId())) files.remove(DepositFileKind.PlanDocument);
            if (!this.fileTransformerService.isAvailable(source.getRdaTransformerId())) files.remove(DepositFileKind.RdaJson);
        }
        return files;
    }

    private String exportBaseName(PlanEntity planEntity) {
        return planEntity.getLabel() != null && !planEntity.getLabel().isBlank() ? planEntity.getLabel() : "plan";
    }

    private FileEnvelopeModel buildSupportingFilesZip(PlanEntity planEntity) throws InvalidApplicationException {
        List<StorageFileEntity> storageFilesForZip = this.collectSupportingFiles(planEntity);

        if (!storageFilesForZip.isEmpty()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(baos)) {

                for (StorageFileEntity storageFile: storageFilesForZip) {
                    ZipEntry entry = new ZipEntry(storageFile.getName() + (storageFile.getExtension().startsWith(".") ? "" : ".") + storageFile.getExtension());
                    zipOutputStream.putNextEntry(entry);
                    zipOutputStream.write(this.storageFileService.readAsBytesSafe(storageFile.getId()));
                    zipOutputStream.closeEntry();
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new MyApplicationException(e.getMessage());
            }

            FileEnvelopeModel zipEnvelope = new FileEnvelopeModel();
            zipEnvelope.setFilename(planEntity.getLabel().replaceAll("\\.zip$", "") + ".zip");
            zipEnvelope.setMimeType("application/zip");
            zipEnvelope.setFile(baos.toByteArray());

            return zipEnvelope;
        }

        return null;
    }

    private List<StorageFileEntity> collectSupportingFiles(PlanEntity planEntity) throws InvalidApplicationException {

        List<StorageFileEntity> storageFileEntities = this.queryFactory.query(StorageFileQuery.class).disableTracking().collectAs(new BaseFieldSet().ensure(StorageFile._id).ensure(StorageFile._name).ensure(StorageFile._extension));
        List<StorageFileEntity> storageFilesForZip = new ArrayList<>();

        PlanPropertiesEntity planPropertiesEntity = this.jsonHandlingService.fromJsonSafe(PlanPropertiesEntity.class, planEntity.getProperties());

        if (planPropertiesEntity != null && planPropertiesEntity.getFieldSets() != null && !planPropertiesEntity.getFieldSets().isEmpty()) {
            PlanBlueprintEntity planBlueprintEntity = this.tenantEntityManagerFactory.getInstance().find(PlanBlueprintEntity.class, planEntity.getBlueprintId(), true);
            if (planBlueprintEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planEntity.getBlueprintId(), PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            org.opencdmp.commons.types.planblueprint.DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.planblueprint.DefinitionEntity.class, planBlueprintEntity.getDefinition());
            if (definition != null) {
                for (String filedSetKey : planPropertiesEntity.getFieldSets().keySet()) {
                    if (planPropertiesEntity.getFieldSets().get(filedSetKey).getItems() != null) {
                        for (PropertyDefinitionFieldSetItemEntity fieldSetItem : planPropertiesEntity.getFieldSets().get(filedSetKey).getItems()) {
                            if (fieldSetItem.getFields() != null) {
                                for (String fieldKey : fieldSetItem.getFields().keySet()) {
                                    if (!definition.getFieldById(fieldKey).isEmpty()) {
                                        org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = definition.getFieldById(fieldKey).getFirst();
                                        StorageFileEntity storageFile = this.collectStorageFilePerField(fieldEntity, fieldSetItem, fieldKey, storageFileEntities);
                                        if (storageFile != null) storageFilesForZip.add(storageFile);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<DescriptionEntity> descriptionEntities = this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(planEntity.getId()).isActive(IsActive.Active).collect();
        for (DescriptionEntity description: descriptionEntities) {
            PropertyDefinitionEntity properties = this.jsonHandlingService.fromJsonSafe(PropertyDefinitionEntity.class, description.getProperties());
            if (properties != null) {
                DescriptionTemplateEntity descriptionTemplateEntity = this.tenantEntityManagerFactory.getInstance().find(DescriptionTemplateEntity.class, description.getDescriptionTemplateId(), true);
                if (descriptionTemplateEntity == null)
                    throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{description.getDescriptionTemplateId(), DescriptionTemplateEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(org.opencdmp.commons.types.descriptiontemplate.DefinitionEntity.class, descriptionTemplateEntity.getDefinition());
                if (definition != null) {
                    for (String filedSetKey : properties.getFieldSets().keySet()) {
                        if (properties.getFieldSets().get(filedSetKey).getItems() != null) {
                            for (PropertyDefinitionFieldSetItemEntity fieldSetItem : properties.getFieldSets().get(filedSetKey).getItems()) {
                                if (fieldSetItem.getFields() != null) {
                                    for (String fieldKey : fieldSetItem.getFields().keySet()) {
                                        if (!definition.getFieldById(fieldKey).isEmpty()) {
                                            org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity = definition.getFieldById(fieldKey).getFirst();
                                            StorageFileEntity storageFile = this.collectStorageFilePerField(fieldEntity, fieldSetItem, fieldKey, storageFileEntities);
                                            if (storageFile != null) storageFilesForZip.add(storageFile);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return storageFilesForZip;
    }

    private StorageFileEntity collectStorageFilePerField(org.opencdmp.commons.types.descriptiontemplate.FieldEntity fieldEntity, PropertyDefinitionFieldSetItemEntity fieldSetItem, String fieldKey, List<StorageFileEntity> storageFileEntities) {
        if (storageFileEntities == null) return null;

        // check if we have an upload value
        if (fieldEntity.getData().getFieldType().equals(FieldType.UPLOAD) && fieldSetItem.getFields().get(fieldKey) != null && fieldSetItem.getFields().get(fieldKey).getTextValue() != null) {
            UUID fileId = this.conventionService.isValidUUID(fieldSetItem.getFields().get(fieldKey).getTextValue()) ? UUID.fromString(fieldSetItem.getFields().get(fieldKey).getTextValue()) : null;
            if (fileId != null) {
                return storageFileEntities.stream().filter(x -> x.getId().equals(fileId)).findFirst().orElse(null);
            }
        }

        return null;

    }

    private void sendNotification(PlanEntity planEntity, String repositoryId) throws InvalidApplicationException {
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(planEntity.getId()).isActives(IsActive.Active).collect();
        if (this.conventionService.isListNullOrEmpty(planUsers)){
            throw new MyNotFoundException("Plan does not have Users");
        }

        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).collect(Collectors.toList())).isActive(IsActive.Active).collect();

        for (UserEntity user: users) {
            if (!user.getId().equals(this.userScopeFactory.getInstance().getUserIdSafe()) && !this.conventionService.isListNullOrEmpty(planUsers.stream().filter(x -> x.getUserId().equals(user.getId())).collect(Collectors.toList()))){
                this.createPlanDepositNotificationEvent(planEntity, user, repositoryId);
            }
        }
    }

    private void createPlanDepositNotificationEvent(PlanEntity plan, UserEntity user, String repositoryId) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getPlanDepositType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScopeFactory.getInstance().getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));
        fieldInfoList.add(new FieldInfo("{depositName}", DataType.String, repositoryId));

        if(this.tenantScopeFactory.getInstance().getTenantCode() != null && !this.tenantScopeFactory.getInstance().getTenantCode().equals(this.tenantScopeFactory.getInstance().getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScopeFactory.getInstance().getTenantCode())));
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
        storageFilePersist.setOwnerId(this.userScopeFactory.getInstance().getUserIdSafe());
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

    @Override
    public DepositAuthMethodResult getAuthMethods(String repositoryId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        DepositAuthMethodResult result = new DepositAuthMethodResult();
        List<DepositAuthMethod> authTypes = new ArrayList<>();

        DepositClient depositClient = this.getDepositClient(repositoryId);
        if (depositClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{repositoryId, DepositClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DepositConfiguration configuration = depositClient.getConfiguration();
        if(configuration == null) return result;

        if(this.conventionService.isListNullOrEmpty(configuration.getAuthMethods())) return result;
        if(configuration.getAuthMethods().contains(org.opencdmp.depositbase.enums.DepositAuthMethod.PluginDefault)) authTypes.add(DepositAuthMethod.PluginDefault);
        if(configuration.getAuthMethods().contains(org.opencdmp.depositbase.enums.DepositAuthMethod.oAuth2Flow) && !this.conventionService.isNullOrEmpty(configuration.getRepositoryAuthorizationUrl())) authTypes.add(DepositAuthMethod.oAuth2Flow);

        if(configuration.getAuthMethods().contains(org.opencdmp.depositbase.enums.DepositAuthMethod.AuthInfoFromUserProfile)){
            if (!this.conventionService.isListNullOrEmpty(configuration.getUserConfigurationFields())) {
                List<String> configurationCodes = configuration.getUserConfigurationFields().stream().filter(ConfigurationField::isAuthInfo).map(ConfigurationField::getCode).toList();
                if (!this.conventionService.isListNullOrEmpty(configurationCodes)) {

                    UserEntity user = this.queryFactory.query(UserQuery.class).ids(this.userScopeFactory.getInstance().getUserIdSafe()).disableTracking().firstAs(new BaseFieldSet().ensure(User._name).ensure(User._additionalInfo));
                    if (user == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{this.userScopeFactory.getInstance().getUserIdSafe(), User.class.getSimpleName()}, LocaleContextHolder.getLocale()));
                    authTypes.add(DepositAuthMethod.AuthInfoFromUserProfile);

                    AdditionalInfoEntity additionalInfo = this.conventionService.isNullOrEmpty(user.getAdditionalInfo()) ? null : this.jsonHandlingService.fromJsonSafe(AdditionalInfoEntity.class, user.getAdditionalInfo());
                    if (additionalInfo != null && !this.conventionService.isListNullOrEmpty(additionalInfo.getPluginConfigurations())) {
                        PluginConfigurationUserEntity pluginConfigurationUserEntity = additionalInfo.getPluginConfigurations().stream().filter(x -> x.getPluginCode().equals(repositoryId) && x.getPluginType().equals(PluginType.Deposit)).findFirst().orElse(null);
                        if (pluginConfigurationUserEntity != null) {

                            List<PluginConfigurationUserFieldEntity> userFieldsWithNullValues = pluginConfigurationUserEntity.getUserFields().stream().filter(x -> this.conventionService.isNullOrEmpty(x.getTextValue()) && configurationCodes.contains(x.getCode())).toList();
                            if (this.conventionService.isListNullOrEmpty(userFieldsWithNullValues)){
                                result.setHasMyAccountValue(true);
                            }
                        }
                    }
                }
            }
        }
        DepositPluginSettings settings = this.tenantDepositSettings(configuration);
        authTypes.removeIf(x -> !this.isAllowed(settings, x));
        result.setPreselected(settings.text(DepositPluginSettings.PUBLISH_PRESELECTED, DepositPluginSettings.PUBLISH_WAYS_OWN));
        result.setOrganisationAccountName(settings.text(DepositPluginSettings.ORGANISATION_ACCOUNT_NAME));
        Collections.sort(authTypes);
        result.setDepositAuthInfoTypes(authTypes);
        return result;
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
