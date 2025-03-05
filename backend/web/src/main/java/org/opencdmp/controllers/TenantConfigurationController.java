package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.TenantConfigurationType;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.data.TenantConfigurationEntity;
import org.opencdmp.model.builder.tenantconfiguration.TenantConfigurationBuilder;
import org.opencdmp.model.censorship.tenantconfiguration.TenantConfigurationCensor;
import org.opencdmp.model.persist.tenantconfiguration.TenantConfigurationPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.model.tenantconfiguration.TenantConfiguration;
import org.opencdmp.query.TenantConfigurationQuery;
import org.opencdmp.query.lookup.TenantConfigurationLookup;
import org.opencdmp.service.tenantconfiguration.TenantConfigurationService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/tenant-configuration")
public class TenantConfigurationController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantConfigurationController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final TenantConfigurationService tenantConfigurationService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;
    private final TenantScope tenantScope;

    public TenantConfigurationController(
		    BuilderFactory builderFactory,
		    AuditService auditService,
		    TenantConfigurationService tenantConfigurationService, CensorFactory censorFactory,
		    QueryFactory queryFactory,
		    MessageSource messageSource, TenantScope tenantScope) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.tenantConfigurationService = tenantConfigurationService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
	    this.tenantScope = tenantScope;
    }

    @PostMapping("query")
    public QueryResult<TenantConfiguration> query(@RequestBody TenantConfigurationLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", TenantConfiguration.class.getSimpleName());

        this.censorFactory.censor(TenantConfigurationCensor.class).censor(lookup.getProject(), null);

        TenantConfigurationQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<TenantConfigurationEntity> data = query.collectAs(lookup.getProject());
        List<TenantConfiguration> models = this.builderFactory.builder(TenantConfigurationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.TenantConfiguration_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public TenantConfiguration get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + TenantConfiguration.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(TenantConfigurationCensor.class).censor(fieldSet, null);

        TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        TenantConfiguration model = this.builderFactory.builder(TenantConfigurationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, TenantConfiguration.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.TenantConfiguration_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("type/{type}")
    public TenantConfiguration getType(@PathVariable("type") Short type, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving type" + TenantConfiguration.class.getSimpleName()).And("type", type).And("fields", fieldSet));

        this.censorFactory.censor(TenantConfigurationCensor.class).censor(fieldSet, null);

        TenantConfigurationQuery query = this.queryFactory.query(TenantConfigurationQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).isActive(IsActive.Active).types(TenantConfigurationType.of(type));
        if (this.tenantScope.isDefaultTenant()) query.tenantIsSet(false);
        else query.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());

        TenantConfiguration model = this.builderFactory.builder(TenantConfigurationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));

        this.auditService.track(AuditableAction.TenantConfiguration_LookupByType, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("type", type),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("active-type/{type}")
    public TenantConfiguration getActiveType(@PathVariable("type") Short type, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving active type" + TenantConfiguration.class.getSimpleName()).And("type", type).And("fields", fieldSet));

        this.censorFactory.censor(TenantConfigurationCensor.class).censor(fieldSet, null);

        TenantConfiguration model = this.builderFactory.builder(TenantConfigurationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, this.tenantConfigurationService.getActiveType(TenantConfigurationType.of(type), fieldSet));

        this.auditService.track(AuditableAction.TenantConfiguration_LookupBActiveType, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("type", type),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = TenantConfigurationPersist.TenantConfigurationPersistValidator.ValidatorName, argumentName = "model")
    public TenantConfiguration persist(@RequestBody TenantConfigurationPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JsonProcessingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("persisting" + TenantConfiguration.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        TenantConfiguration persisted = this.tenantConfigurationService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.TenantConfiguration_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + TenantConfiguration.class.getSimpleName()).And("id", id));

        this.tenantConfigurationService.deleteAndSave(id);

        this.auditService.track(AuditableAction.TenantConfiguration_Delete, "id", id);
    }

}
