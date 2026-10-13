package org.opencdmp.service.pluginintegration;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeCacheService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeFilterFunction;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeModel;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.types.deposit.DepositSourceEntity;
import org.opencdmp.commons.types.evaluator.EvaluatorSourceEntity;
import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import org.opencdmp.commons.types.tenantconfiguration.DepositTenantConfigurationEntity;
import org.opencdmp.commons.types.tenantconfiguration.EvaluatorTenantConfigurationEntity;
import org.opencdmp.commons.types.tenantconfiguration.FileTransformerTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.model.deposit.DepositConfiguration;
import org.opencdmp.model.pluginintegration.PluginConnectionTestRequest;
import org.opencdmp.model.pluginintegration.PluginConnectionTestResult;
import org.opencdmp.model.pluginintegration.PluginSourceOverview;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.service.deposit.DepositProperties;
import org.opencdmp.service.deposit.DepositService;
import org.opencdmp.service.encryption.EncryptionService;
import org.opencdmp.service.evaluator.EvaluatorProperties;
import org.opencdmp.service.evaluator.EvaluatorService;
import org.opencdmp.service.filetransformer.FileTransformerProperties;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.tenant.TenantProperties;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.*;

@Service
public class PluginIntegrationServiceImpl implements PluginIntegrationService {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PluginIntegrationServiceImpl.class));

    private static final int TEST_MAX_IN_MEMORY_SIZE_IN_BYTES = 6554000;
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(20);

    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final JsonHandlingService jsonHandlingService;
    private final ConventionService conventionService;
    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;
    private final TokenExchangeCacheService tokenExchangeCacheService;
    private final DepositProperties depositProperties;
    private final EvaluatorProperties evaluatorProperties;
    private final FileTransformerProperties fileTransformerProperties;
    private final DepositService depositService;
    private final EvaluatorService evaluatorService;
    private final FileTransformerService fileTransformerService;

    public PluginIntegrationServiceImpl(AuthorizationService authorizationService, QueryFactory queryFactory, TenantScopeFactory tenantScopeFactory, JsonHandlingService jsonHandlingService, ConventionService conventionService, EncryptionService encryptionService, TenantProperties tenantProperties, TokenExchangeCacheService tokenExchangeCacheService, DepositProperties depositProperties, EvaluatorProperties evaluatorProperties, FileTransformerProperties fileTransformerProperties, DepositService depositService, EvaluatorService evaluatorService, FileTransformerService fileTransformerService) {
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.conventionService = conventionService;
        this.encryptionService = encryptionService;
        this.tenantProperties = tenantProperties;
        this.tokenExchangeCacheService = tokenExchangeCacheService;
        this.depositProperties = depositProperties;
        this.evaluatorProperties = evaluatorProperties;
        this.fileTransformerProperties = fileTransformerProperties;
        this.depositService = depositService;
        this.evaluatorService = evaluatorService;
        this.fileTransformerService = fileTransformerService;
    }

    /** Connection details of one source, whatever its kind. The secret is plain text. */
    private record Source(String id, String url, String issuerUrl, String clientId, String clientSecret, String scope, boolean tenantOwned) {
    }

    /** What the tenant configuration of one kind says: its own sources and what it switched off. */
    private record TenantSources(List<Source> sources, boolean disableSystemSources, Set<String> disabledSourceIds) {
        static final TenantSources NONE = new TenantSources(List.of(), false, Set.of());
    }

    //region listing

    @Override
    public List<PluginSourceOverview> getSources() {
        this.authorizationService.authorizeForce(Permission.BrowseTenantConfiguration);

        List<PluginSourceOverview> items = new ArrayList<>();
        for (PluginType type : PluginType.values()) {
            TenantSources tenant = this.tenantSources(type);
            Set<String> reachable = this.reachableIds(type);
            for (Source source : this.installationSources(type)) {
                boolean enabled = !tenant.disableSystemSources() && !tenant.disabledSourceIds().contains(source.id());
                items.add(this.overview(type, source, PluginSourceOverview.PROVIDER_INSTALLATION, enabled, reachable));
            }
            for (Source source : tenant.sources()) {
                boolean enabled = !tenant.disabledSourceIds().contains(source.id());
                items.add(this.overview(type, source, PluginSourceOverview.PROVIDER_TENANT, enabled, reachable));
            }
        }
        return items;
    }

    private PluginSourceOverview overview(PluginType type, Source source, String provider, boolean enabled, Set<String> reachable) {
        PluginSourceOverview item = new PluginSourceOverview();
        item.setPluginType(type);
        item.setId(source.id());
        item.setProvider(provider);
        item.setEnabled(enabled);
        item.setReachable(enabled ? reachable.contains(source.id()) : null);
        // installation addresses are the operator's business, not the tenant's
        if (source.tenantOwned()) item.setUrl(source.url());
        return item;
    }

    /** Ids of the enabled sources that answered, through the same cached lookups the rest of the app uses. */
    private Set<String> reachableIds(PluginType type) {
        Set<String> ids = new HashSet<>();
        try {
            switch (type) {
                case Deposit -> this.depositService.getAvailableConfigurations(new BaseFieldSet(DepositConfiguration._repositoryId))
                        .forEach(x -> ids.add(x.getRepositoryId()));
                case Evaluation -> this.evaluatorService.getAvailableEvaluators().forEach(x -> ids.add(x.getEvaluatorId()));
                case FileTransformer -> this.fileTransformerService.getAvailableExportFileFormats().forEach(x -> ids.add(x.getFileTransformerId()));
            }
        } catch (Exception e) {
            logger.error("Could not check which " + type + " plugins are reachable", e);
        }
        return ids;
    }

    //endregion

    //region sources

    private List<Source> installationSources(PluginType type) {
        List<Source> items = new ArrayList<>();
        switch (type) {
            case Deposit -> {
                if (this.depositProperties.getSources() != null) for (DepositSourceEntity s : this.depositProperties.getSources())
                    items.add(new Source(s.getRepositoryId(), s.getUrl(), s.getIssuerUrl(), s.getClientId(), s.getClientSecret(), s.getScope(), false));
            }
            case Evaluation -> {
                if (this.evaluatorProperties.getSources() != null) for (EvaluatorSourceEntity s : this.evaluatorProperties.getSources())
                    items.add(new Source(s.getEvaluatorId(), s.getUrl(), s.getIssuerUrl(), s.getClientId(), s.getClientSecret(), s.getScope(), false));
            }
            case FileTransformer -> {
                if (this.fileTransformerProperties.getSources() != null) for (FileTransformerSourceEntity s : this.fileTransformerProperties.getSources())
                    items.add(new Source(s.getTransformerId(), s.getUrl(), s.getIssuerUrl(), s.getClientId(), s.getClientSecret(), s.getScope(), false));
            }
        }
        return items;
    }

    /** Reads the tenant's own record of this kind, the same way the plugin services do. Secrets are decrypted. */
    private TenantSources tenantSources(PluginType type) {
        try {
            if (!this.tenantScopeFactory.getInstance().isSet() || !this.tenantScopeFactory.getInstance().isMultitenant()) return TenantSources.NONE;

            TenantConfigurationType configurationType;
            String field;
            switch (type) {
                case Deposit -> { configurationType = TenantConfigurationType.DepositPlugins; field = TenantConfiguration._depositPlugins; }
                case Evaluation -> { configurationType = TenantConfigurationType.EvaluatorPlugins; field = TenantConfiguration._evaluatorPlugins; }
                default -> { configurationType = TenantConfigurationType.FileTransformerPlugins; field = TenantConfiguration._fileTransformerPlugins; }
            }
            TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(configurationType);
            if (this.tenantScopeFactory.getInstance().isDefaultTenant()) query.tenantIsSet(false);
            else query.tenantIsSet(true).tenantIds(this.tenantScopeFactory.getInstance().getTenant());
            TenantConfigurationEntity entity = query.firstAs(new BaseFieldSet().ensure(field));
            if (entity == null || this.conventionService.isNullOrEmpty(entity.getValue())) return TenantSources.NONE;

            List<Source> sources = new ArrayList<>();
            boolean disableSystemSources;
            List<String> disabled;
            switch (type) {
                case Deposit -> {
                    DepositTenantConfigurationEntity value = this.jsonHandlingService.fromJsonSafe(DepositTenantConfigurationEntity.class, entity.getValue());
                    if (value == null) return TenantSources.NONE;
                    if (value.getSources() != null) for (DepositSourceEntity s : value.getSources())
                        sources.add(new Source(s.getRepositoryId(), s.getUrl(), s.getIssuerUrl(), s.getClientId(), this.decrypt(s.getClientSecret()), s.getScope(), true));
                    disableSystemSources = value.getDisableSystemSources();
                    disabled = value.getDisabledSourceIds();
                }
                case Evaluation -> {
                    EvaluatorTenantConfigurationEntity value = this.jsonHandlingService.fromJsonSafe(EvaluatorTenantConfigurationEntity.class, entity.getValue());
                    if (value == null) return TenantSources.NONE;
                    if (value.getSources() != null) for (EvaluatorSourceEntity s : value.getSources())
                        sources.add(new Source(s.getEvaluatorId(), s.getUrl(), s.getIssuerUrl(), s.getClientId(), this.decrypt(s.getClientSecret()), s.getScope(), true));
                    disableSystemSources = value.getDisableSystemSources();
                    disabled = value.getDisabledSourceIds();
                }
                default -> {
                    FileTransformerTenantConfigurationEntity value = this.jsonHandlingService.fromJsonSafe(FileTransformerTenantConfigurationEntity.class, entity.getValue());
                    if (value == null) return TenantSources.NONE;
                    if (value.getSources() != null) for (FileTransformerSourceEntity s : value.getSources())
                        sources.add(new Source(s.getTransformerId(), s.getUrl(), s.getIssuerUrl(), s.getClientId(), this.decrypt(s.getClientSecret()), s.getScope(), true));
                    disableSystemSources = value.getDisableSystemSources();
                    disabled = value.getDisabledSourceIds();
                }
            }
            return new TenantSources(sources, disableSystemSources, disabled != null ? new HashSet<>(disabled) : Set.of());
        } catch (Exception e) {
            logger.error("Could not read the tenant's " + type + " plugins", e);
            return TenantSources.NONE;
        }
    }

    private String decrypt(String secret) throws Exception {
        if (this.conventionService.isNullOrEmpty(secret)) return secret;
        return this.encryptionService.decryptAES(secret, this.tenantProperties.getConfigEncryptionAesKey(), this.tenantProperties.getConfigEncryptionAesIv());
    }

    //endregion

    //region connection test

    @Override
    public PluginConnectionTestResult testConnection(PluginConnectionTestRequest request) {
        this.authorizationService.authorizeForce(Permission.EditTenantConfiguration);
        if (request == null || request.getPluginType() == null) throw new MyValidationException("pluginType is required");

        Source source = this.resolveTestSource(request);
        if (source == null) return new PluginConnectionTestResult(false, null, "No plugin with this id is set up for the tenant");
        if (this.conventionService.isNullOrEmpty(source.url()) || this.conventionService.isNullOrEmpty(source.issuerUrl()) || this.conventionService.isNullOrEmpty(source.clientId()))
            throw new MyValidationException("url, issuerUrl and clientId are required");

        String basePath;
        String configurationPath;
        String idField;
        switch (request.getPluginType()) {
            case Deposit -> { basePath = "/api/deposit"; configurationPath = "/configuration"; idField = "repositoryId"; }
            case Evaluation -> { basePath = "/api/evaluator"; configurationPath = "/config"; idField = "evaluatorId"; }
            default -> { basePath = "/api/file-transformer"; configurationPath = "/formats"; idField = "fileTransformerId"; }
        }

        try {
            // a throw-away key, so a token obtained with unsaved credentials is never reused for the real plugin
            TokenExchangeModel tokenExchangeModel = new TokenExchangeModel("plugin-test:" + UUID.randomUUID(), source.issuerUrl(), source.clientId(), source.clientSecret(), source.scope());
            WebClient client = WebClient.builder().baseUrl(trimTrailingSlash(source.url()) + basePath)
                    .filter(new TokenExchangeFilterFunction(this.tokenExchangeCacheService, tokenExchangeModel))
                    .codecs(codecs -> {
                        codecs.defaultCodecs().maxInMemorySize(TEST_MAX_IN_MEMORY_SIZE_IN_BYTES);
                        codecs.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(JsonMapper.builder().build()));
                    })
                    .build();
            Map<String, Object> configuration = client.get().uri(configurationPath).retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(TEST_TIMEOUT)
                    .block();
            Object reportedId = configuration != null ? configuration.get(idField) : null;
            return new PluginConnectionTestResult(true, reportedId != null ? reportedId.toString() : null, null);
        } catch (WebClientResponseException e) {
            return new PluginConnectionTestResult(false, null, "The plugin answered " + e.getStatusCode().value() + " " + e.getStatusText());
        } catch (Exception e) {
            logger.warn("Plugin connection test failed", e);
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            return new PluginConnectionTestResult(false, null, cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        }
    }

    /**
     * With no url, the source in effect under that id; otherwise the details given, reusing a stored tenant secret
     * when none was typed.
     */
    private Source resolveTestSource(PluginConnectionTestRequest request) {
        TenantSources tenant = this.tenantSources(request.getPluginType());
        Source stored = tenant.sources().stream().filter(x -> Objects.equals(x.id(), request.getId())).findFirst()
                .orElse(this.installationSources(request.getPluginType()).stream().filter(x -> Objects.equals(x.id(), request.getId())).findFirst().orElse(null));

        if (this.conventionService.isNullOrEmpty(request.getUrl())) return stored;

        String secret = request.getClientSecret();
        if (this.conventionService.isNullOrEmpty(secret) && stored != null && stored.tenantOwned()) secret = stored.clientSecret();
        return new Source(request.getId(), request.getUrl(), request.getIssuerUrl(), request.getClientId(), secret, request.getScope(), true);
    }

    private static String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    //endregion
}
