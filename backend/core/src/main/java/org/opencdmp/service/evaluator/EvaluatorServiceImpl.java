package org.opencdmp.service.evaluator;

import gr.cite.tools.exception.MyValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolverFactory;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.*;
import org.opencdmp.commons.notification.NotificationProperties;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.opencdmp.commons.types.notification.DataType;
import org.opencdmp.commons.types.notification.FieldInfo;
import org.opencdmp.commons.types.notification.NotificationFieldData;
import org.opencdmp.data.*;
import org.opencdmp.evaluatorbase.enums.RankType;
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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

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
    private final TenantScopeFactory tenantScopeFactory;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private final JsonHandlingService jsonHandlingService;
    private final EvaluatorSourcesCacheService evaluatorSourcesCacheService;
    private final AccountingService accountingService;
    private final FileTransformerService fileTransformerService;
    private final UserScopeFactory userScopeFactory;
    private final StorageFileService storageFileService;
    private final StorageFileProperties storageFileProperties;
    private final ValidatorFactory validatorFactory;
    private final AuthorizationContentResolverFactory authorizationContentResolverFactory;
    private final EvaluationService evaluationService;
    private final NotificationProperties notificationProperties;
    private final NotifyIntegrationEventHandler eventHandler;

    @Autowired
    public EvaluatorServiceImpl(EvaluatorProperties evaluatorProperties, Map<String, EvaluatorClientImpl> clients, TokenExchangeCacheService tokenExchangeCacheService, EvaluatorConfigurationCacheService evaluatorConfigurationCacheService, AuthorizationService authorizationService, QueryFactory queryFactory, BuilderFactory builderFactory, MessageSource messageSource, ConventionService conventionService, TenantScopeFactory tenantScopeFactory, EncryptionService encryptionService, TenantProperties tenantProperties, JsonHandlingService jsonHandlingService, EvaluatorSourcesCacheService evaluatorSourcesCacheService, AccountingService accountingService, FileTransformerService fileTransformerService, UserScopeFactory userScopeFactory, StorageFileService storageFileService, StorageFileProperties storageFileProperties, ValidatorFactory validatorFactory, AuthorizationContentResolverFactory authorizationContentResolverFactory, EvaluationService evaluationService, NotificationProperties notificationProperties, NotifyIntegrationEventHandler eventHandler) {
        this.evaluatorProperties = evaluatorProperties;
        this.clients = clients;
        this.tokenExchangeCacheService = tokenExchangeCacheService;
        this.evaluatorConfigurationCacheService = evaluatorConfigurationCacheService;
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.messageSource = messageSource;
        this.conventionService = conventionService;
        this.tenantScopeFactory = tenantScopeFactory;
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
        this.jsonHandlingService = jsonHandlingService;
        this.evaluatorSourcesCacheService = evaluatorSourcesCacheService;
        this.accountingService = accountingService;
        this.fileTransformerService = fileTransformerService;
        this.userScopeFactory = userScopeFactory;
        this.storageFileService = storageFileService;
        this.storageFileProperties = storageFileProperties;
        this.validatorFactory = validatorFactory;
        this.authorizationContentResolverFactory = authorizationContentResolverFactory;
        this.evaluationService = evaluationService;
        this.notificationProperties = notificationProperties;
        this.eventHandler = eventHandler;
    }
    /** Used when a source declares no response size limit, e.g. one a tenant added through the UI. */
    private static final int DEFAULT_MAX_IN_MEMORY_SIZE_IN_BYTES = 6554000;

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
                    WebClient.builder().baseUrl(StringUtils.removeEnd(source.getUrl(), "/") + "/api/evaluator")
                            .filters(exchangeFilterFunctions -> {
                                exchangeFilterFunctions.add(tokenExchangeFilterFunction);
                                exchangeFilterFunctions.add(logRequest());
                                exchangeFilterFunctions.add(logResponse());
                            })
                            .codecs(codecs -> {
                                codecs.defaultCodecs().maxInMemorySize(source.getMaxInMemorySizeInBytes() > 0 ? source.getMaxInMemorySizeInBytes() : DEFAULT_MAX_IN_MEMORY_SIZE_IN_BYTES);
                                codecs.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(JsonMapper.builder().build()));
                                codecs.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(JsonMapper.builder().build()));
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
        String tenantCode = this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant() ? this.tenantScopeFactory.getInstance().getTenantCode() : "";
        EvaluatorSourcesCacheService.EvaluatorSourceCacheValue cacheValue = this.evaluatorSourcesCacheService.lookup(this.evaluatorSourcesCacheService.buildKey(tenantCode));
        if (cacheValue == null) {
            List<EvaluatorSourceEntity> evaluatorSourceEntities = new ArrayList<>(this.evaluatorProperties.getSources());

            if (this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant()) {
                TenantConfigurationQuery tenantConfigurationQuery = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.EvaluatorPlugins);
                if (this.tenantScopeFactory.getInstance().isDefaultTenant()) tenantConfigurationQuery.tenantIsSet(false);
                else tenantConfigurationQuery.tenantIsSet(true).tenantIds(this.tenantScopeFactory.getInstance().getTenant());
                TenantConfigurationEntity tenantConfiguration = tenantConfigurationQuery.firstAs(new BaseFieldSet().ensure(TenantConfiguration._evaluatorPlugins));

                if (tenantConfiguration != null && !this.conventionService.isNullOrEmpty(tenantConfiguration.getValue())) {
                    EvaluatorTenantConfigurationEntity evaluatorTenantConfigurationEntity = this.jsonHandlingService.fromJsonSafe(EvaluatorTenantConfigurationEntity.class, tenantConfiguration.getValue());
                    if (evaluatorTenantConfigurationEntity != null) {
                        if (evaluatorTenantConfigurationEntity.getDisableSystemSources()) evaluatorSourceEntities = new ArrayList<>();
                        evaluatorSourceEntities.addAll(this.buildEvaluatorSourceItems(evaluatorTenantConfigurationEntity.getSources()));
                        if (evaluatorTenantConfigurationEntity.getDisabledSourceIds() != null) evaluatorSourceEntities.removeIf(x -> evaluatorTenantConfigurationEntity.getDisabledSourceIds().contains(x.getEvaluatorId()));
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
        this.evaluatorSourcesCacheService.evict(this.evaluatorSourcesCacheService.buildKey(event.getTenantCode()));
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
    public List<org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration> getAvailableEvaluators() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        this.authorizationService.authorizeForce(Permission.BrowsePlan, Permission.DeferredAffiliation);
        List<org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration> configurations = new ArrayList<>();

        for(EvaluatorSourceEntity evaluatorSource : this.getEvaluatorSources()){

            String tenantCode = this.tenantScopeFactory.getInstance().isSet() && this.tenantScopeFactory.getInstance().isMultitenant() ? this.tenantScopeFactory.getInstance().getTenantCode() : "";
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
    public List<RankResultModel> rankPlan(UUID planId, String evaluatorId, String format, List<String> benchmarkIds, List<String> testIds) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().planAffiliation(planId)), Permission.EvaluatePlan);
        EvaluatorClientImpl repository = this.getEvaluatorClient(evaluatorId);

        if(repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{format, EvaluatorClientImpl.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanEntity planEntity = this.queryFactory.query(PlanQuery.class).disableTracking().ids(planId).first();
        if (planEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, PlanEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        EvaluatorSourceEntity source = this.getEvaluatorSources().stream().filter(evaluatorSource -> evaluatorSource.getEvaluatorId().equals(evaluatorId)).findFirst().orElse(null);
        FileEnvelopeModel jsonEnvelope = new FileEnvelopeModel();
        if (source != null && !this.conventionService.isNullOrEmpty(source.getRdaTransformerId())) {
            org.opencdmp.model.file.FileEnvelope rda = this.fileTransformerService.exportPlanInternal(planId, source.getRdaTransformerId(), "json");
            jsonEnvelope.setFilename(rda.getFilename());
            jsonEnvelope.setMimeType("application/json");

            if (!repository.getConfiguration().isUseSharedStorage()) jsonEnvelope.setFile(rda.getFile());
            else jsonEnvelope.setFileRef(this.addFileToSharedStorage(rda));
        }

        PlanModel evaluatorModel = this.builderFactory.builder(PlanCommonModelBuilder.class).useSharedStorage(repository.getConfiguration().isUseSharedStorage()).setEvaluatorId(repository.getConfiguration().getEvaluatorId()).setRepositoryId(source != null && !this.conventionService.isNullOrEmpty(source.getRdaTransformerId()) ? source.getRdaTransformerId() : null).isPublic(false).setRdaJsonFile(jsonEnvelope).authorize(AuthorizationFlags.All).build(planEntity);
        if(evaluatorModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{planId, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        List<RankResultModel> rankModel = repository.rankPlan(new PlanEvaluationModel(evaluatorModel,benchmarkIds, testIds));
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.EVALUATION_PLAN_EXECUTION_COUNT_FOR, evaluatorId);

        this.evaluationService.persistInternal(rankModel, repository.getConfiguration().getRankConfig(), planId, EntityType.Plan , evaluatorId, userScopeFactory.getInstance().getUserId());

        this.sendPlanNotification(planEntity, evaluatorId, rankModel, repository.getConfiguration().getRankConfig());

        return rankModel;

    }

    private void sendPlanNotification(PlanEntity planEntity, String repositoryId, List<RankResultModel> rankModel, RankConfig rankConfig) throws InvalidApplicationException {
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(planEntity.getId()).isActives(IsActive.Active).collect();
        if (this.conventionService.isListNullOrEmpty(planUsers)){
            throw new MyNotFoundException("Plan does not have Users");
        }

        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).collect(Collectors.toList())).isActive(IsActive.Active).collect();

        for (UserEntity user: users) {
            if (!user.getId().equals(this.userScopeFactory.getInstance().getUserIdSafe()) && !this.conventionService.isListNullOrEmpty(planUsers.stream().filter(x -> x.getUserId().equals(user.getId())).collect(Collectors.toList()))){
                this.createPlanEvaluationNotificationEvent(planEntity, user, repositoryId, rankModel, rankConfig);
            }
        }
    }

    private void createPlanEvaluationNotificationEvent(PlanEntity plan, UserEntity user, String repositoryId, List<RankResultModel> rankModel,  RankConfig rankConfig) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getPlanEvaluationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScopeFactory.getInstance().getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, plan.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, plan.getId().toString()));
        fieldInfoList.add(new FieldInfo("{evaluatorName}", DataType.String, repositoryId));
        fieldInfoList.add(new FieldInfo("{result}", DataType.String, this.applyRankResultString(rankConfig, rankModel.stream().map(RankResultModel::getRank).toList())));
        fieldInfoList.add(new FieldInfo("{benchmarks}", DataType.String, this.applyBenchmarkTitle(rankConfig, rankModel)));

        if(this.tenantScopeFactory.getInstance().getTenantCode() != null && !this.tenantScopeFactory.getInstance().getTenantCode().equals(this.tenantScopeFactory.getInstance().getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScopeFactory.getInstance().getTenantCode())));
        }
        data.setFields(fieldInfoList);
        event.setData(this.jsonHandlingService.toJsonSafe(data));

        this.eventHandler.handle(event);
    }

    /**
     * Lists every benchmark the evaluator actually ran, across all rank results, with its
     * outcome. Deduplicated by title, because a per-description evaluator runs the same
     * benchmarks once per description, and a benchmark counts as failed if it failed for any
     * of them.
     * <p>
     * Results the evaluator skipped carry no benchmarks. That is not an error, so this
     * reports that nothing was assessed rather than throwing and losing a notification for
     * an evaluation we have already stored.
     */
    private String applyBenchmarkTitle(RankConfig rankConfig, List<RankResultModel> rankModels) {
        if (rankModels == null || rankModels.isEmpty())
            throw new MyApplicationException("Rank Model is Missing.");

        Map<String, List<Double>> ranksByBenchmark = new LinkedHashMap<>();
        for (RankResultModel rankModel : rankModels) {
            if (rankModel == null || rankModel.getResults() == null) continue;
            for (EvaluationResultModel benchmark : rankModel.getResults()) {
                ranksByBenchmark.computeIfAbsent(benchmark.getBenchmarkTitle(), k -> new ArrayList<>()).add(benchmark.getRank());
            }
        }

        if (ranksByBenchmark.isEmpty())
            return this.messageSource.getMessage("rankResult.notEvaluated", new Object[]{}, LocaleContextHolder.getLocale());

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, List<Double>> benchmark : ranksByBenchmark.entrySet()) {

            if (!isFirst) sb.append(", ");

            sb.append(benchmark.getKey())
                    .append("(")
                    .append(this.applyRankResultString(rankConfig, benchmark.getValue()))
                    .append(") ");

            isFirst = false;
        }
        return sb.toString();
    }


    /**
     * Collapses a set of ranks into one word.
     * <p>
     * It passes only when something was actually assessed and every assessed rank passed.
     * Previously the last rank in the list silently overwrote the verdict of the ones before
     * it, which was invisible while evaluators returned a single result and wrong as soon as
     * one of them started returning several. Ranks the evaluator marked as not evaluated are
     * neither a pass nor a failure, so they do not colour the verdict either way.
     */
    private String applyRankResultString(RankConfig rankConfig, List<Double> ranks) {

        boolean anyEvaluated = false;
        boolean allPassed = true;

        if(rankConfig.getRankType().equals(RankType.Selection)){
            SelectionConfiguration selectionConfiguration = rankConfig.getSelectionConfiguration();

            for (Double rank: ranks) {
                SelectionConfiguration.ValueSet matchedValueSet = selectionConfiguration.getValueSetList().stream()
                        .filter(x -> x.getKey() == rank)
                        .findFirst()
                        .orElse(null);

                if (matchedValueSet == null) throw new MyApplicationException("Matched Value Missing.");

                switch (matchedValueSet.getSuccessStatus()) {
                    case Fail -> {
                        anyEvaluated = true;
                        allPassed = false;
                    }
                    case Pass -> anyEvaluated = true;
                    case NotEvaluated -> {
                        // Skipped: contributes nothing to the verdict.
                    }
                }
            }


        } else if(rankConfig.getRankType().equals(RankType.ValueRange)){

            for (Double rank: ranks) {
                ValueRangeConfiguration valueRangeConfiguration = rankConfig.getValueRangeConfiguration();
                anyEvaluated = true;
                if(rank < valueRangeConfiguration.getMinPassValue()){
                    allPassed = false;
                }
            }
        }

        if (!anyEvaluated) {
            return this.messageSource.getMessage("rankResult.notEvaluated", new Object[]{}, LocaleContextHolder.getLocale());
        } else if (allPassed) {
            return this.messageSource.getMessage("rankResult.pass", new Object[]{}, LocaleContextHolder.getLocale());
        } else {
            return this.messageSource.getMessage("rankResult.fail", new Object[]{}, LocaleContextHolder.getLocale());
        }
    }

    @Override
    public List<RankResultModel> rankDescription(UUID descriptionId, String repositoryId, String format, List<String> benchmarkIds, List<String> testIds, boolean isPublic) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolverFactory.getInstance().descriptionAffiliation(descriptionId)), Permission.EvaluateDescription);
        EvaluatorClientImpl repository = this.getEvaluatorClient(repositoryId);

        if(repository == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{format, EvaluatorClientImpl.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DescriptionEntity descriptionEntity = this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(descriptionId).first();
        if (descriptionEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, DescriptionEntity.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        DescriptionModel descriptionEvaluatorModel = this.builderFactory.builder(DescriptionCommonModelBuilder.class).setRepositoryId(repository.getConfiguration().getEvaluatorId()).useSharedStorage(repository.getConfiguration().isUseSharedStorage()).isPublic(isPublic).authorize(AuthorizationFlags.All).build(descriptionEntity);
        if (descriptionEvaluatorModel == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{descriptionId, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.accountingService.increase(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT.getValue());
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.FILE_TRANSFORMER_EXPORT_DESCRIPTIONS_EXECUTION_COUNT_FOR, repositoryId);


        List<RankResultModel> rankModel = repository.rankDescription(new DescriptionEvaluationModel(descriptionEvaluatorModel,benchmarkIds, testIds));
        this.increaseTargetMetricWithRepositoryId(UsageLimitTargetMetric.EVALUATION_DESCRIPTION_EXECUTION_COUNT_FOR, repositoryId);

        this.evaluationService.persistInternal(rankModel, repository.getConfiguration().getRankConfig(), descriptionId, EntityType.Description , repositoryId, userScopeFactory.getInstance().getUserId());

        this.sendDescriptionNotification(descriptionEntity, repositoryId, rankModel, repository.getConfiguration().getRankConfig());

        return rankModel;
    }

    private void sendDescriptionNotification(DescriptionEntity descriptionEntity, String repositoryId, List<RankResultModel> rankModel, RankConfig rankConfig) throws InvalidApplicationException {
        List<PlanUserEntity> planUsers = this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(descriptionEntity.getPlanId()).isActives(IsActive.Active).collect();
        if (this.conventionService.isListNullOrEmpty(planUsers)){
            throw new MyNotFoundException("Plan does not have Users");
        }

        List<UserEntity> users = this.queryFactory.query(UserQuery.class).disableTracking().ids(planUsers.stream().map(PlanUserEntity::getUserId).collect(Collectors.toList())).isActive(IsActive.Active).collect();

        for (UserEntity user: users) {
            if (!user.getId().equals(this.userScopeFactory.getInstance().getUserIdSafe()) && !this.conventionService.isListNullOrEmpty(planUsers.stream().filter(x -> x.getUserId().equals(user.getId())).collect(Collectors.toList()))){
                this.createDescriptionEvaluationNotificationEvent(descriptionEntity, user, repositoryId, rankModel, rankConfig);
            }
        }
    }

    private void createDescriptionEvaluationNotificationEvent(DescriptionEntity description, UserEntity user, String repositoryId, List<RankResultModel> rankModel,  RankConfig rankConfig) throws InvalidApplicationException {
        NotifyIntegrationEvent event = new NotifyIntegrationEvent();
        event.setUserId(user.getId());

        event.setNotificationType(this.notificationProperties.getDescriptionEvaluationType());
        NotificationFieldData data = new NotificationFieldData();
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        fieldInfoList.add(new FieldInfo("{recipient}", DataType.String, user.getName()));
        fieldInfoList.add(new FieldInfo("{reasonName}", DataType.String, this.queryFactory.query(UserQuery.class).disableTracking().ids(this.userScopeFactory.getInstance().getUserId()).first().getName()));
        fieldInfoList.add(new FieldInfo("{name}", DataType.String, description.getLabel()));
        fieldInfoList.add(new FieldInfo("{id}", DataType.String, description.getId().toString()));
        fieldInfoList.add(new FieldInfo("{evaluatorName}", DataType.String, repositoryId));
        fieldInfoList.add(new FieldInfo("{result}", DataType.String, this.applyRankResultString(rankConfig, rankModel.stream().map(RankResultModel::getRank).toList())));
        fieldInfoList.add(new FieldInfo("{benchmarks}", DataType.String, this.applyBenchmarkTitle(rankConfig, rankModel)));

        if(this.tenantScopeFactory.getInstance().getTenantCode() != null && !this.tenantScopeFactory.getInstance().getTenantCode().equals(this.tenantScopeFactory.getInstance().getDefaultTenantCode())){
            fieldInfoList.add(new FieldInfo("{tenant-url-path}", DataType.String, String.format("/t/%s", this.tenantScopeFactory.getInstance().getTenantCode())));
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

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.mutate().build().bodyToMono(String.class)
                        .flatMap(body -> {
                            logger.error(new MapLogEntry("Response").And("method", response.request().getMethod().toString()).And("url", response.request().getURI()).And("status", response.statusCode().toString()).And("body", body));
                            if (response.statusCode() == HttpStatus.BAD_REQUEST) {
                                Map map = this.jsonHandlingService.fromJsonSafe(Map.class, body);
                                if (map != null && map.containsKey("error")) {
                                    Object error = map.get("error");
                                    if (error instanceof String) {
                                        return Mono.error(new MyValidationException(response.statusCode().value(), (String) error));
                                    }
                                    return Mono.error(new MyValidationException(response.statusCode().value(), this.jsonHandlingService.toJsonSafe(map.get("error"))));
                                }

                            }
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
}
