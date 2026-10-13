package org.opencdmp.service.language;

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
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.locale.LocaleProperties;
import org.opencdmp.commons.types.tenantconfiguration.DefaultUserLocaleTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.LanguageEntity;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.Language;
import org.opencdmp.model.builder.LanguageBuilder;
import org.opencdmp.model.deleter.LanguageDeleter;
import org.opencdmp.model.persist.LanguagePersist;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.planblueprint.PlanBlueprintServiceImpl;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LanguageServiceImpl implements LanguageService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintServiceImpl.class));
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final ErrorThesaurusProperties errors;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final LocaleProperties localeProperties;


    public LanguageServiceImpl(
            TenantEntityManagerFactory tenantEntityManagerFactory, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
            ConventionService conventionService, MessageSource messageSource, ErrorThesaurusProperties errors, UsageLimitService usageLimitService, AccountingService accountingService,
            QueryFactory queryFactory, JsonHandlingService jsonHandlingService, LocaleProperties localeProperties){
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.errors = errors;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.localeProperties = localeProperties;
    }


    public Language persist(LanguagePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException{
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditLanguage);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        LanguageEntity data;
        if (isUpdate) {
            data = this.tenantEntityManagerFactory.getInstance().find(LanguageEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Language.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            this.usageLimitService.checkIncrease(UsageLimitTargetMetric.LANGUAGE_COUNT);

            data = new LanguageEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
            // The ordinal no longer orders the language menu, but the column is still NOT NULL.
            data.setOrdinal(0);
        }
        data.setCode(model.getCode());
        data.setPayload(model.getPayload() != null && !model.getPayload().isEmpty() ? model.getPayload() : null);
        data.setUpdatedAt(Instant.now());
        if (isUpdate) this.tenantEntityManagerFactory.getInstance().merge(data);
        else {
            this.tenantEntityManagerFactory.getInstance().persist(data);
            this.accountingService.increase(UsageLimitTargetMetric.LANGUAGE_COUNT.getValue());
        }

        this.tenantEntityManagerFactory.getInstance().flush();

        return this.builderFactory.builder(LanguageBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Language._id), data);
    }

    public String mergePayloads(String... payloads) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode merged = mapper.createObjectNode();
        for (String payload : payloads) {
            if (payload == null || payload.isBlank()) continue;
            JsonNode node = mapper.readTree(payload);
            if (node.isObject()) this.mergeInto(merged, (ObjectNode) node);
        }
        return merged.toString();
    }

    // The language set as new users' default under Region in the tenant configuration, else the installation's.
    public String getDefaultLanguageCode(UUID tenantId) {
        TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().isActive(IsActive.Active).types(TenantConfigurationType.DefaultUserLocale);
        BaseFieldSet fields = new BaseFieldSet(TenantConfiguration._id, TenantConfiguration._defaultUserLocale);
        TenantConfigurationEntity configuration = null;
        if (tenantId != null) configuration = query.tenantIds(tenantId).tenantIsSet(true).firstAs(fields);
        if (configuration == null) configuration = query.clearTenantIds().tenantIsSet(false).firstAs(fields);

        if (configuration != null) {
            DefaultUserLocaleTenantConfigurationEntity locale = this.jsonHandlingService.fromJsonSafe(DefaultUserLocaleTenantConfigurationEntity.class, configuration.getValue());
            if (locale != null && locale.getLanguage() != null) return locale.getLanguage();
        }
        return this.localeProperties.getLanguage();
    }

    private void mergeInto(ObjectNode target, ObjectNode source) {
        for (Map.Entry<String, JsonNode> property : source.properties()) {
            JsonNode existing = target.get(property.getKey());
            if (existing instanceof ObjectNode && property.getValue() instanceof ObjectNode) this.mergeInto((ObjectNode) existing, (ObjectNode) property.getValue());
            else target.set(property.getKey(), property.getValue());
        }
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteLanguage);

        this.deleterFactory.deleter(LanguageDeleter.class).deleteAndSaveByIds(List.of(id));
    }
}
