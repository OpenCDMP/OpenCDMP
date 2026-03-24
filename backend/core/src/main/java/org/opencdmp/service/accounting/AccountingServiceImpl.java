package org.opencdmp.service.accounting;

import org.opencdmp.data.TenantEntityManagerFactory;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeCacheService;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeFilterFunction;
import gr.cite.commons.web.oidc.filter.webflux.TokenExchangeModel;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.enums.UsageLimitPeriodicityRange;
import org.opencdmp.commons.enums.accounting.AccountingAggregateType;
import org.opencdmp.commons.enums.accounting.AccountingDataRangeType;
import org.opencdmp.commons.enums.accounting.AccountingMeasureType;
import org.opencdmp.commons.enums.accounting.AccountingValueType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.commons.scope.user.UserScopeFactory;
import org.opencdmp.commons.types.accounting.AccountingSourceEntity;
import org.opencdmp.commons.types.usagelimit.DefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.UserCredentialEntity;
import org.opencdmp.integrationevent.outbox.accountingentrycreated.AccountingEntryCreatedIntegrationEvent;
import org.opencdmp.integrationevent.outbox.accountingentrycreated.AccountingEntryCreatedIntegrationEventHandler;
import org.opencdmp.model.accounting.AccountingAggregateResults;
import org.opencdmp.query.UserCredentialQuery;
import org.opencdmp.query.lookup.accounting.AccountingInfoLookup;
import org.opencdmp.query.lookup.accounting.FieldSet;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
public class AccountingServiceImpl implements AccountingService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(AccountingServiceImpl.class));
    private final QueryFactory queryFactory;
    private final AccountingEntryCreatedIntegrationEventHandler accountingEntryCreatedIntegrationEventHandler;
    private final UserScopeFactory userScopeFactory;
    private final TenantScopeFactory tenantScopeFactory;
    private final MessageSource messageSource;
    private final ConventionService conventionService;
    private final TokenExchangeCacheService tokenExchangeCacheService;
    private final AccountingProperties accountingProperties;
    private final ValidatorFactory validatorFactory;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    private final Boolean isEnabled;

    @Autowired
    public AccountingServiceImpl(
            QueryFactory queryFactory, AccountingEntryCreatedIntegrationEventHandler accountingEntryCreatedIntegrationEventHandler, UserScopeFactory userScopeFactory, TenantScopeFactory tenantScopeFactory, MessageSource messageSource, ConventionService conventionService, TokenExchangeCacheService tokenExchangeCacheService, AccountingProperties accountingProperties, ValidatorFactory validatorFactory, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.queryFactory = queryFactory;
        this.accountingEntryCreatedIntegrationEventHandler = accountingEntryCreatedIntegrationEventHandler;
        this.userScopeFactory = userScopeFactory;
        this.tenantScopeFactory = tenantScopeFactory;
        this.messageSource = messageSource;
        this.conventionService = conventionService;
        this.tokenExchangeCacheService = tokenExchangeCacheService;
        this.accountingProperties = accountingProperties;
        this.validatorFactory = validatorFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.isEnabled = this.accountingProperties.getEnabled();
    }

    private AccountingClient getAccountingClient() throws InvalidApplicationException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        //GK: It's register time
        AccountingSourceEntity source = this.accountingProperties.getSource();
        if (source != null) {
            TokenExchangeModel tokenExchangeModel = new TokenExchangeModel("accounting:" + source, source.getIssuerUrl(), source.getClientId(), source.getClientSecret(), source.getScope());
            TokenExchangeFilterFunction apiKeyExchangeFilterFunction = new TokenExchangeFilterFunction(this.tokenExchangeCacheService, tokenExchangeModel);
            WebClient webClient = WebClient.builder().baseUrl(source.getUrl() + "/api/accounting-service")
                    .filters(exchangeFilterFunctions -> {
                        exchangeFilterFunctions.add(apiKeyExchangeFilterFunction);
                        exchangeFilterFunctions.add(logRequest());
                        exchangeFilterFunctions.add(logResponse());
                    }).codecs(codecs -> {
                        codecs.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(JsonMapper.builder().disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS).build()));
                        codecs.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(JsonMapper.builder().disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS).build()));
                    }).build();
            AccountingClientImpl accounting = new AccountingClientImpl(webClient);
            return accounting;
        }
        return null;
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

    public Integer getCurrentMetricValue(String metric, DefinitionEntity definition) throws InvalidApplicationException {
        if (this.isEnabled) {
            AccountingClient accountingClient = null;
            try {
                accountingClient = this.getAccountingClient();
            } catch (InvalidApplicationException e) {
                throw new RuntimeException(e);
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            if (accountingClient == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{this.accountingProperties.getSource(), AccountingClient.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            AccountingInfoLookup lookup = new AccountingInfoLookup();
            if (definition != null && definition.getHasPeriodicity()) {
                if (definition.getPeriodicityRange().equals(UsageLimitPeriodicityRange.Monthly)) lookup.setDateRangeType(AccountingDataRangeType.ThisMonth);
                else lookup.setDateRangeType(AccountingDataRangeType.ThisYear);
            } else {
                lookup.setDateRangeType(AccountingDataRangeType.Custom);
                lookup.setTo(Instant.now());
                lookup.setFrom(this.accountingProperties.getFromInstant());
            }

            lookup.setMeasure(AccountingMeasureType.Unit);
            lookup.setServiceCodes(List.of(this.accountingProperties.getServiceId()));
            lookup.setAggregateTypes(List.of(AccountingAggregateType.Sum));
            lookup.setActionCodes(List.of(metric));

            try {
                lookup.setResourceCodes(List.of(this.tenantScopeFactory.getInstance().getTenantCode() != null ? this.tenantScopeFactory.getInstance().getTenantCode() : this.tenantScopeFactory.getInstance().getDefaultTenantCode()));
            } catch (InvalidApplicationException e) {
                throw new RuntimeException(e);
            }

            List<String> fields = new ArrayList<>();
            fields.add(AccountingEntryCreatedIntegrationEvent._serviceId);
            fields.add(AccountingEntryCreatedIntegrationEvent._action);
            fields.add(AccountingEntryCreatedIntegrationEvent._resource);

            lookup.setGroupingFields(new FieldSet(fields));

            lookup.setProject(new FieldSet(this.accountingProperties.getProjectFields()));

            this.validatorFactory.validator(AccountingInfoLookup.AccountingInfoLookupValidator.class).validateForce(lookup);

            AccountingAggregateResults accountingAggregateResultItem = null;
            try {
                accountingAggregateResultItem = accountingClient.calculate(lookup);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (accountingAggregateResultItem != null && !this.conventionService.isListNullOrEmpty(accountingAggregateResultItem.getItems())) {
                return accountingAggregateResultItem.getItems().getFirst().getSum().intValue();
            } else return 0;
        }
        return null;
    }

    public void set(String metric, UUID tenantId, String tenantCode, Integer value) throws InvalidApplicationException{
        if (this.isEnabled) {
            String subjectId = this.getSubjectId();

            this.accountingEntryCreatedIntegrationEventHandler.handleAccountingEntry(metric, AccountingValueType.Reset, subjectId, tenantId, tenantCode, value, this.userScopeFactory.getInstance().getUserIdSafe());
        }
    }

    public void increase(String metric) throws InvalidApplicationException {
        if (this.isEnabled) {
            String subjectId = this.getSubjectId();

            this.accountingEntryCreatedIntegrationEventHandler.handleAccountingEntry(metric, AccountingValueType.Plus, subjectId, this.tenantScopeFactory.getInstance().getTenant(), this.tenantScopeFactory.getInstance().getTenantCode() != null ? this.tenantScopeFactory.getInstance().getTenantCode() : this.tenantScopeFactory.getInstance().getDefaultTenantCode(), 1, this.userScopeFactory.getInstance().getUserIdSafe());
        }
    }

    public void increase(String metric, Boolean useDefaultTenantCode) throws InvalidApplicationException {
        if (this.isEnabled) {
            if (useDefaultTenantCode) {
                String subjectId = this.getSubjectId();
                this.accountingEntryCreatedIntegrationEventHandler.handleAccountingEntry(metric, AccountingValueType.Plus, subjectId, null, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), 1, this.userScopeFactory.getInstance().getUserIdSafe());
                return;
            }
            this.increase(metric);
        }
    }

    public void decrease(String metric) throws InvalidApplicationException {
        if (this.isEnabled) {
            String subjectId = this.getSubjectId();

            this.accountingEntryCreatedIntegrationEventHandler.handleAccountingEntry(metric, AccountingValueType.Minus, subjectId, this.tenantScopeFactory.getInstance().getTenant(), this.tenantScopeFactory.getInstance().getTenantCode() != null ? this.tenantScopeFactory.getInstance().getTenantCode() : this.tenantScopeFactory.getInstance().getDefaultTenantCode(), 1, this.userScopeFactory.getInstance().getUserIdSafe());
        }
    }

    public void decrease(String metric, Boolean useDefaultTenantCode) throws InvalidApplicationException {
        if (this.isEnabled) {
            if (useDefaultTenantCode) {
                String subjectId = this.getSubjectId();
                this.accountingEntryCreatedIntegrationEventHandler.handleAccountingEntry(metric, AccountingValueType.Minus, subjectId, null, this.tenantScopeFactory.getInstance().getDefaultTenantCode(), 1, this.userScopeFactory.getInstance().getUserIdSafe());
                return;
            }
            this.decrease(metric);
        }
    }

    private String getSubjectId() throws InvalidApplicationException {

        if (!this.userScopeFactory.getInstance().isSet()) return null;

        UserCredentialEntity userCredential = null;
        try {
            this.tenantEntityManagerFactory.getInstance().disableTenantFilters();
            userCredential = this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(this.userScopeFactory.getInstance().getUserId()).first();
        } finally {
            this.tenantEntityManagerFactory.getInstance().reloadTenantFilters();
        }

        if (userCredential != null) return userCredential.getExternalId();
        return this.userScopeFactory.getInstance().getUserId().toString();
    }
}

