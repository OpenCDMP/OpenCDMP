package org.opencdmp.service.evaluator;

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
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.data.*;
import org.opencdmp.evaluatorbase.enums.RankType;
import org.opencdmp.evaluatorbase.enums.SuccessStatus;
import org.opencdmp.evaluatorbase.interfaces.EvaluatorClient;
import org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration;
import org.opencdmp.commons.types.tenantconfiguration.EvaluatorTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.evaluatorbase.interfaces.SelectionConfiguration;
import org.opencdmp.evaluatorbase.interfaces.ValueRangeConfiguration;
import org.opencdmp.evaluatorbase.models.misc.*;
import org.opencdmp.event.TenantConfigurationTouchedEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEvent;
import org.opencdmp.integrationevent.outbox.notification.NotifyIntegrationEventHandler;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.description.DescriptionCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.plan.PlanCommonModelBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.*;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.evaluation.EvaluationService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.storage.StorageFileProperties;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluatorServiceImpl implements EvaluatorService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EvaluatorServiceImpl.class));
    private final EvaluatorProperties evaluatorProperties;
    private final Map<String, EvaluatorClientImpl> clients;
    private final TokenExchangeCacheService tokenExchangeCacheService;
    private final EvaluatorConfigurationCacheService evaluatorConfigurationCacheService;
    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final MessageSource messageSource;
    private final ConventionService conventionService;
    private final TenantScope tenantScope;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private final JsonHandlingService jsonHandlingService;
    private final EvaluatorSourcesCacheService evaluatorSourcesCacheService;
    private final AccountingService accountingService;
    private final TenantEntityManager entityManager;
    private final FileTransformerService fileTransformerService;
    private final UserScope userScope;
    private final StorageFileService storageFileService;
    private final StorageFileProperties storageFileProperties;
    private final ValidatorFactory validatorFactory;
    private final AuthorizationContentResolver authorizationContentResolver;
    private final EvaluationService evaluationService;
    private final NotificationProperties notificationProperties;
    private final NotifyIntegrationEventHandler eventHandler;

    @Autowired
    public EvaluatorServiceImpl(EvaluatorProperties evaluatorProperties, Map<String, EvaluatorClientImpl> clients, TokenExchangeCacheService tokenExchangeCacheService, EvaluatorConfigurationCacheService evaluatorConfigurationCacheService, AuthorizationService authorizationService, QueryFactory queryFactory, BuilderFactory builderFactory, MessageSource messageSource, ConventionService conventionService, TenantScope tenantScope, EncryptionService encryptionService, TenantProperties tenantProperties, JsonHandlingService jsonHandlingService, EvaluatorSourcesCacheService evaluatorSourcesCacheService, AccountingService accountingService, TenantEntityManager entityManager, FileTransformerService fileTransformerService, UserScope userScope, StorageFileService storageFileService, StorageFileProperties storageFileProperties, ValidatorFactory validatorFactory, AuthorizationContentResolver authorizationContentResolver, EvaluationService evaluationService, NotificationProperties notificationProperties, NotifyIntegrationEventHandler eventHandler) {
        this.evaluatorProperties = evaluatorProperties;
        this.clients = clients;
        this.tokenExchangeCacheService = tokenExchangeCacheService;
        this.evaluatorConfigurationCacheService = evaluatorConfigurationCacheService;
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.messageSource = messageSource;
        this.conventionService = conventionService;
        this.tenantScope = tenantScope;
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
        this.jsonHandlingService = jsonHandlingService;
        this.evaluatorSourcesCacheService = evaluatorSourcesCacheService;
        this.accountingService = accountingService;
        this.entityManager = entityManager;
        this.fileTransformerService = fileTransformerService;
        this.userScope = userScope;
        this.storageFileService = storageFileService;
        this.storageFileProperties = storageFileProperties;
        this.validatorFactory = validatorFactory;
        this.authorizationContentResolver = authorizationContentResolver;
        this.evaluationService = evaluationService;
        this.notificationProperties = notificationProperties;
        this.eventHandler = eventHandler;
    }
    private EvaluatorClientImpl getEvaluatorClient(String repoId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String repositoryIdByTenant = this.getRepositoryIdByTenant(repoId);
        if (this.clients.containsKey(repositoryIdByTenant)) {
            return this.clients.get(repositoryIdByTenant);
        }

        EvaluatorSourceEntity source = this.getEvaluatorSources().stream()
                .filter(evaluatorSourceEntity -> evaluatorSourceEntity.getEvaluatorId().equals(repoId))
                .findFirst().orElse(null);

        try {
            TokenExchangeModel tokenExchangeModel = new TokenExchangeModel(
                    "evaluator:" + repositoryIdByTenant,
                    source.getIssuerUrl(),
                    source.getClientId(),
                    source.getClientSecret(),
                    source.getScope()
            );

            TokenExchangeFilterFunction tokenExchangeFilterFunction = new TokenExchangeFilterFunction(
                    this.tokenExchangeCacheService,
                    tokenExchangeModel
            );

            EvaluatorClientImpl repository = new EvaluatorClientImpl(
                    WebClient.builder().baseUrl(source.getUrl() + "/api/evaluator")
                            .filters(exchangeFilterFunctions -> {
                                exchangeFilterFunctions.add(tokenExchangeFilterFunction);
                                exchangeFilterFunctions.add(logRequest());
                                exchangeFilterFunctions.add(logResponse());
                            })
                            .codecs(codecs -> {
                                codecs.defaultCodecs().maxInMemorySize(source.getMaxInMemorySizeInBytes());
                                codecs.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new ObjectMapper().registerModule(new JavaTimeModule()), MediaType.APPLICATION_JSON));
                                codecs.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(new ObjectMapper().registerModule(new JavaTimeModule()), MediaType.APPLICATION_JSON));
                            })
                            .build()
            );

            this.clients.put(repositoryIdByTenant, repository);
            return repository;
        } catch (Exception e) {
            logger.error("Exception occurred while creating EvaluatorClientImpl", e);
            return null;
        }
    }


    private List<EvaluatorSourceEntity> getEvaluatorSources() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : "";
        EvaluatorSourcesCacheService.EvaluatorSourceCacheValue cacheValue = this.evaluatorSourcesCacheService.lookup(this.evaluatorSourcesCacheService.buildKey(tenantCode));
        if (cacheValue == null) {
            List<EvaluatorSourceEntity> evaluatorSourceEntities = new ArrayList<>(this.evaluatorProperties.getSources());

            if (this.tenantScope.isSet() && this.tenantScope.isMultitenant()) {
                TenantConfigurationQuery tenantConfigurationQuery = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.EvaluatorPlugins);
                if (this.tenantScope.isDefaultTenant()) tenantConfigurationQuery.tenantIsSet(false);
                else tenantConfigurationQuery.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());
                TenantConfigurationEntity tenantConfiguration = tenantConfigurationQuery.firstAs(new BaseFieldSet().ensure(TenantConfiguration._evaluatorPlugins));

                if (tenantConfiguration != null && !this.conventionService.isNullOrEmpty(tenantConfiguration.getValue())) {
                    EvaluatorTenantConfigurationEntity evaluatorTenantConfigurationEntity = this.jsonHandlingService.fromJsonSafe(EvaluatorTenantConfigurationEntity.class, tenantConfiguration.getValue());
                    if (evaluatorTenantConfigurationEntity != null) {
                        if (evaluatorTenantConfigurationEntity.getDisableSystemSources()) evaluatorSourceEntities = new ArrayList<>();
                        evaluatorSourceEntities.addAll(this.buildEvaluatorSourceItems(evaluatorTenantConfigurationEntity.getSources()));
                    }
                }
            }
            cacheValue = new EvaluatorSourcesCacheService.EvaluatorSourceCacheValue(tenantCode, evaluatorSourceEntities);
            this.evaluatorSourcesCacheService.put(cacheValue);
        }
        return cacheValue.getSources();
    }

    @EventListener
    public void handleTenantConfigurationTouchedEvent(TenantConfigurationTouchedEvent event) {
        if (!event.getType().equals(TenantConfigurationType.EvaluatorPlugins)) return;
        EvaluatorSourcesCacheService.EvaluatorSourceCacheValue evaluatorSourceCacheValue = this.evaluatorSourcesCacheService.lookup(this.evaluatorSourcesCacheService.buildKey(event.getTenantCode()));
        if (evaluatorSourceCacheValue != null && evaluatorSourceCacheValue.getSources() != null){
            for (EvaluatorSourceEntity source : evaluatorSourceCacheValue.getSources()){
                String repositoryIdByTenant = source.getEvaluatorId() + "_" + event.getTenantCode();
                this.clients.remove(repositoryIdByTenant);
                this.evaluatorConfigurationCacheService.evict(this.evaluatorConfigurationCacheService.buildKey(source.getEvaluatorId(), event.getTenantCode()));
            }
        }
        this.evaluatorConfigurationCacheService.evict(this.evaluatorSourcesCacheService.buildKey(event.getTenantCode()));
    }

    private List<EvaluatorSourceEntity> buildEvaluatorSourceItems(List<EvaluatorSourceEntity> sources) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        List<EvaluatorSourceEntity> items = new ArrayList<>();
        if (this.conventionService.isListNullOrEmpty(sources)) return items;
        for (EvaluatorSourceEntity source : sources){
            EvaluatorSourceEntity item = new EvaluatorSourceEntity();
            item.setEvaluatorId(source.getEvaluatorId());
            item.setRdaTransformerId(source.getRdaTransformerId());
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
    public List<org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration> getAvailableEvaluators() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        this.authorizationService.authorizeForce(Permission.BrowsePlan, Permission.DeferredAffiliation);
        List<org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration> configurations = new ArrayList<>();

        for(EvaluatorSourceEntity evaluatorSource : this.getEvaluatorSources()){

            String tenantCode = this.tenantScope.isSet() && this.tenantScope.isMultitenant() ? this.tenantScope.getTenantCode() : "";
            EvaluatorConfigurationCacheService.EvaluatorConfigurationCacheValue cacheValue = this.evaluatorConfigurationCacheService.lookup(this.evaluatorConfigurationCacheService.buildKey(evaluatorSource.getEvaluatorId(), tenantCode));

            if(cacheValue == null){
                try{
                    EvaluatorClientImpl evaluatorClient = this.getEvaluatorClient(evaluatorSource.getEvaluatorId());
                    if(evaluatorClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{evaluatorSource.getEvaluatorId(), EvaluatorClientImpl.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                    EvaluatorConfiguration configuration = evaluatorClient.getConfiguration();
                    cacheValue = new EvaluatorConfigurationCacheService.EvaluatorConfigurationCacheValue(evaluatorSource.getEvaluatorId(), tenantCode, configuration);
                    this.evaluatorConfigurationCacheService.put(cacheValue);
                }catch (Exception e){
                    logger.error(e.getMessage(), e);
                }
            }
            if(cacheValue != null){
                configurations.add(cacheValue.getConfiguration());
            }
        }

        return configurations;
    }

    @Override
    public RankResultModel rankPlan(UUID planId, String evaluatorId, String format, List<String> benchmarkIds, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.planAffiliation(planId)), Permission.EvaluatePlan);
        EvaluatorClientImpl repository = this.getEvaluatorClient(evaluatorId);

        if(repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{format, EvaluatorClientImpl.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        EvaluatorSourceEntity source = this.getEvaluatorSources().stream().filter(evaluatorSource -> !this.conventionService.isNullOrEmpty(evaluatorSource.getRdaTransformerId())).findFirst().orElse(null);
        FileEnvelopeModel jsonEnvelope = new FileEnvelopeModel();
        if (source != null) {
            org.opencdmp.model.file.FileEnvelope rda = this.fileTransformerService.exportPlan(planId, source.getRdaTransformerId(), "json", isPublic);
            jsonEnvelope.setFilename(rda.getFilename());
            jsonEnvelope.setMimeType("application/json");

            if (!repository.getConfiguration().isUseSharedStorage()) jsonEnvelope.setFile(rda.getFile());
            else jsonEnvelope.setFileRef(this.addFileToSharedStorage(rda));
        }

        PlanModel evaluatorModel = this.builderFactory.builder(PlanCommonModelBuilder.class).useSharedStorage(repository.getConfiguration().isUseSharedStorage()).setEvaluatorId(repository.getConfiguration().getEvaluatorId()).setRepositoryId(source != null && !this.conventionService.isNullOrEmpty(source.getRdaTransformerId()) ? source.getRdaTransformerId() : null).isPublic(isPublic).setRdaJsonFile(jsonEnvelope).authorize(AuthorizationFlags.All).build(planEntity);
        if(evaluatorModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.accountingService.increase(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_PLAN_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_PLAN_EXECUTION_COUNT_FOR, evaluatorId);

        RankResultModel rankModel = repository.rankPlan(new PlanEvaluationModel(evaluatorModel,benchmarkIds));
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.EVALUATION_PLAN_EXECUTION_COUNT_FOR, evaluatorId);

        this.evaluationService.persistInternal(rankModel, repository.getConfiguration().getRankConfig(), planId, EntityType.Plan , evaluatorId, userScope.getUserId());

        this.sendPlanNotification(planEntity, evaluatorId, rankModel, repository.getConfiguration().getRankConfig());

        return rankModel;

    }

    private void sendPlanNotification(PlanEntity planEntity, String repositoryId, RankResultModel rankModel, RankConfig rankConfig) throws InvalidApplicationException {
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(planEntity.getId()).isActives(IsActive.Active).collect();
        if (this.conventionService.isListNullOrEmpty(planUsers)){
            throw new MyNotFoundException("Plan does not have Users");
        }

        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).collect(Collectors.toList())).isActive(IsActive.Active).collect();

        for (UserEntity user: users) {
            if (!user.getId().equals(this.userScope.getUserIdSafe()) && !this.conventionService.isListNullOrEmpty(planUsers.stream().filter(x -> x.getUserId().equals(user.getId())).collect(Collectors.toList()))){
                this.createPlanEvaluationNotificationEvent(planEntity, user, repositoryId, rankModel, rankConfig);
            }
        }
    }

    private void createPlanEvaluationNotificationEvent(PlanEntity plan, UserEntity user, String repositoryId, RankResultModel rankModel,  RankConfig rankConfig) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getPlanEvaluationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));
        fieldInfoList.add(new FieldInfo("{evaluatorName}", DataType.String, repositoryId));
        fieldInfoList.add(new FieldInfo("{result}", DataType.String, this.applyRankResultString(rankConfig, rankModel.getRank())));
        fieldInfoList.add(new FieldInfo("{benchmarks}", DataType.String, this.applyBenchmarkTitle(rankConfig, rankModel)));

        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

        this.eventHandler.handle(event);
    }

    private String applyBenchmarkTitle(RankConfig rankConfig, RankResultModel rankModel) {
        if (rankModel == null || rankModel.getResults() == null || rankModel.getResults().isEmpty())
            throw new MyApplicationException("Rank Model is Missing.");

        StringBuilder sb = new StringBuilder();
        List<EvaluationResultModel> benchmarks = rankModel.getResults();
        boolean isFirst = true;
        for(EvaluationResultModel benchmark : benchmarks){

            if(!isFirst) sb.append(", ");

            sb.append(benchmark.getBenchmarkTitle())
                    .append("(")
                    .append(this.applyRankResultString(rankConfig, benchmark.getRank()))
                    .append(") ");


            isFirst = false;
        }
        return sb.toString();
    }


    private String applyRankResultString(RankConfig rankConfig, double rank) {

        if(rankConfig.getRankType().equals(RankType.Selection)){
            SelectionConfiguration selectionConfiguration = rankConfig.getSelectionConfiguration();

            SelectionConfiguration.ValueSet matchedValueSet = selectionConfiguration.getValueSetList().stream()
                    .filter(x -> x.getKey() == rank)
                    .findFirst()
                    .orElse(null);

                    if (matchedValueSet == null) throw new MyApplicationException("Matched Value Missing.");

            return switch (matchedValueSet.getSuccessStatus()) {
                case Fail ->
                        this.messageSource.getMessage("rankResult.fail", new Object[]{}, LocaleContextHolder.getLocale());
                case Pass ->
                        this.messageSource.getMessage("rankResult.pass", new Object[]{}, LocaleContextHolder.getLocale());
            };

        } else if(rankConfig.getRankType().equals(RankType.ValueRange)){
            ValueRangeConfiguration valueRangeConfiguration = rankConfig.getValueRangeConfiguration();
            if(rank >= valueRangeConfiguration.getMinPassValue()){
                return this.messageSource.getMessage("rankResult.pass", new Object[]{}, LocaleContextHolder.getLocale());
            }else{
                return this.messageSource.getMessage("rankResult.fail", new Object[]{}, LocaleContextHolder.getLocale());
            }
        }
        throw new MyApplicationException("Rank Type is Missing.");
    }

    @Override
    public RankResultModel rankDescription(UUID descriptionId, String repositoryId, String format, List<String> benchmarkIds, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.descriptionAffiliation(descriptionId)), Permission.EvaluateDescription);
        EvaluatorClientImpl repository = this.getEvaluatorClient(repositoryId);

        if(repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{format, EvaluatorClientImpl.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DescriptionEntity descriptionEntity = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).first();
        if (descriptionEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, DescriptionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DescriptionModel descriptionEvaluatorModel = this.builderFactory.builder(DescriptionCommonModelBuilder.class).setRepositoryId(repository.getConfiguration().getEvaluatorId()).useSharedStorage(repository.getConfiguration().isUseSharedStorage()).isPublic(isPublic).authorize(AuthorizationFlags.All).build(descriptionEntity);
        if (descriptionEvaluatorModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.accountingService.increase(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT_FOR, repositoryId);


        RankResultModel rankModel = repository.rankDescription(new DescriptionEvaluationModel(descriptionEvaluatorModel,benchmarkIds));
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.EVALUATION_DESCRIPTION_EXECUTION_COUNT_FOR, repositoryId);

        this.evaluationService.persistInternal(rankModel, repository.getConfiguration().getRankConfig(), descriptionId, EntityType.Description , repositoryId, userScope.getUserId());

        this.sendDescriptionNotification(descriptionEntity, repositoryId, rankModel, repository.getConfiguration().getRankConfig());

        return rankModel;
    }

    private void sendDescriptionNotification(DescriptionEntity descriptionEntity, String repositoryId, RankResultModel rankModel, RankConfig rankConfig) throws InvalidApplicationException {
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(descriptionEntity.getId()).isActives(IsActive.Active).collect();
        if (this.conventionService.isListNullOrEmpty(planUsers)){
            throw new MyNotFoundException("Plan does not have Users");
        }

        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).collect(Collectors.toList())).isActive(IsActive.Active).collect();

        for (UserEntity user: users) {
            if (!user.getId().equals(this.userScope.getUserIdSafe()) && !this.conventionService.isListNullOrEmpty(planUsers.stream().filter(x -> x.getUserId().equals(user.getId())).collect(Collectors.toList()))){
                this.createDescriptionEvaluationNotificationEvent(descriptionEntity, user, repositoryId, rankModel, rankConfig);
            }
        }
    }

    private void createDescriptionEvaluationNotificationEvent(DescriptionEntity description, UserEntity user, String repositoryId, RankResultModel rankModel,  RankConfig rankConfig) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getPlanEvaluationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScope.getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, description.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, description.getId().toString()));
        fieldInfoList.add(new FieldInfo("{evaluatorName}", DataType.String, repositoryId));
        fieldInfoList.add(new FieldInfo("{result}", DataType.String, this.applyRankResultString(rankConfig, rankModel.getRank())));
        fieldInfoList.add(new FieldInfo("{benchmarks}", DataType.String, this.applyBenchmarkTitle(rankConfig, rankModel)));

        if(this.tenantScope.getTenantCode() != null && !this.tenantScope.getTenantCode().equals(this.tenantScope.getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScope.getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

        this.eventHandler.handle(event);
    }

    @Override
    public String getLogo(String evaluatorId) throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        this.authorizationService.authorizeForce(Permission.BrowseDeposit, Permission.DeferredAffiliation);
        EvaluatorClient evaluatorClient = this.getEvaluatorClient(evaluatorId);
        if(evaluatorClient == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{evaluatorId, EvaluatorClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        return evaluatorClient.getLogo();
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
}
