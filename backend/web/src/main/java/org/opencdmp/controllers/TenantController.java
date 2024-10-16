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
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.builder.TenantBuilder;
import org.opencdmp.model.censorship.TenantCensor;
import org.opencdmp.model.persist.TenantPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.lookup.TenantLookup;
import org.opencdmp.service.tenant.TenantService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/tenant")
public class TenantController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final TenantService tenantService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public TenantController(
            BuilderFactory builderFactory,
            AuditService auditService,
            TenantService tenantService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.tenantService = tenantService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    public QueryResult<Tenant> query(@RequestBody TenantLookup lookup) throws MyApplicationException, MyForbiddenException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidApplicationException {
        logger.debug("querying {}", Tenant.class.getSimpleName());

        this.censorFactory.censor(TenantCensor.class).censor(lookup.getProject(), null);
        TenantQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<TenantEntity> data = query.collectAs(lookup.getProject());
        List<Tenant> models = this.builderFactory.builder(TenantBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.Tenant_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public Tenant get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Tenant.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(TenantCensor.class).censor(fieldSet, null);

        TenantQuery query = this.queryFactory.query(TenantQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Tenant model = this.builderFactory.builder(TenantBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Tenant.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Tenant_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = TenantPersist.TenantPersistValidator.ValidatorName, argumentName = "model")
    public Tenant persist(@RequestBody TenantPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("persisting" + Tenant.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(TenantCensor.class).censor(fieldSet, null);

        Tenant persisted = this.tenantService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Tenant_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Tenant.class.getSimpleName()).And("id", id));

        this.tenantService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Tenant_Delete, "id", id);
    }

}
