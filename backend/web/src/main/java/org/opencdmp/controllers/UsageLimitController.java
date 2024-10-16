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
import org.opencdmp.data.UsageLimitEntity;
import org.opencdmp.model.UsageLimit;
import org.opencdmp.model.builder.UsageLimitBuilder;
import org.opencdmp.model.censorship.UsageLimitsCensor;
import org.opencdmp.model.persist.UsageLimitPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.UsageLimitQuery;
import org.opencdmp.query.lookup.UsageLimitLookup;
import org.opencdmp.service.usagelimit.UsageLimitService;
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
@RequestMapping(path = "api/usage-limit")
public class UsageLimitController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UsageLimitController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final UsageLimitService usageLimitService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public UsageLimitController(
            BuilderFactory builderFactory,
            AuditService auditService,
            UsageLimitService usageLimitService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.usageLimitService = usageLimitService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    public QueryResult<UsageLimit> query(@RequestBody UsageLimitLookup lookup) throws MyApplicationException, MyForbiddenException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, InvalidApplicationException {
        logger.debug("querying {}", UsageLimit.class.getSimpleName());

        this.censorFactory.censor(UsageLimitsCensor.class).censor(lookup.getProject());
        UsageLimitQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<UsageLimitEntity> data = query.collectAs(lookup.getProject());
        List<UsageLimit> models = this.builderFactory.builder(UsageLimitBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.UsageLimit_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public UsageLimit get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + UsageLimit.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(UsageLimitsCensor.class).censor(fieldSet);

        UsageLimitQuery query = this.queryFactory.query(UsageLimitQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        UsageLimit model = this.builderFactory.builder(UsageLimitBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, UsageLimit.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.UsageLimit_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = UsageLimitPersist.UsageLimitPersistValidator.ValidatorName, argumentName = "model")
    public UsageLimit persist(@RequestBody UsageLimitPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("persisting" + UsageLimit.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(UsageLimitsCensor.class).censor(fieldSet);

        UsageLimit persisted = this.usageLimitService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.UsageLimit_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + UsageLimit.class.getSimpleName()).And("id", id));

        this.usageLimitService.deleteAndSave(id);

        this.auditService.track(AuditableAction.UsageLimit_Delete, "id", id);
    }

}
