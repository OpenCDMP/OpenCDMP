package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.Ordering;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.data.LanguageEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.model.Language;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.builder.LanguageBuilder;
import org.opencdmp.model.censorship.LanguageCensor;
import org.opencdmp.model.persist.LanguagePersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.LanguageQuery;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.lookup.LanguageLookup;
import org.opencdmp.service.language.LanguageService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@RestController
@RequestMapping(path = "api/language")
public class LanguageController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(LanguageController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final LanguageService languageService;

    private final TenantScope tenantScope;

    @Autowired
    public LanguageController(
            BuilderFactory builderFactory,
            AuditService auditService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource,
            LanguageService languageService, TenantScope tenantScope) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.languageService = languageService;
        this.tenantScope = tenantScope;
    }

    @PostMapping("query")
    public QueryResult<Language> query(@RequestBody LanguageLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Language.class.getSimpleName());

        this.censorFactory.censor(LanguageCensor.class).censor(lookup.getProject(), null);

        LanguageQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<LanguageEntity> data = query.collectAs(lookup.getProject());
        List<Language> models = this.builderFactory.builder(LanguageBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.Language_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public Language get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Language.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(LanguageCensor.class).censor(fieldSet, null);

        LanguageQuery query = this.queryFactory.query(LanguageQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Language model = this.builderFactory.builder(LanguageBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Language.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Language_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping(value = {"public/code/{code}/{tenantCode}", "public/code/{code}"})
    public Language get(@PathVariable("code") String code, @PathVariable(value = "tenantCode", required = false) String tenantCode, FieldSet fieldSet, boolean overrideFromFile) throws MyApplicationException, MyForbiddenException, MyNotFoundException, IOException {
        logger.debug(new MapLogEntry("retrieving" + Language.class.getSimpleName()).And("code", code).And("fields", fieldSet));

        this.censorFactory.censor(LanguageCensor.class).censor(fieldSet, null);

        LanguageQuery query = this.queryFactory.query(LanguageQuery.class).disableTracking().authorize(EnumSet.of(Public)).codes(code).tenantIsSet(false).isActive(IsActive.Active);
        Language model = null;

        if (!overrideFromFile) {
            if (tenantCode != null && !tenantCode.isEmpty() && !tenantCode.equals(this.tenantScope.getDefaultTenantCode())) {
                TenantEntity tenant = this.queryFactory.query(TenantQuery.class).codes(tenantCode).firstAs(new BaseFieldSet(Tenant._id));
                if (tenant == null)
                    throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{tenantCode, Tenant.class.getSimpleName()}, LocaleContextHolder.getLocale()));

                query.tenantIds(tenant.getId()).tenantIsSet(true);
                model = this.builderFactory.builder(LanguageBuilder.class).authorize(EnumSet.of(Public)).build(fieldSet, query.firstAs(fieldSet));
                if (model == null) {
                    query.clearTenantIds().tenantIsSet(false);
                    model = this.builderFactory.builder(LanguageBuilder.class).authorize(EnumSet.of(Public)).build(fieldSet, query.firstAs(fieldSet));
                }
            }
            else {
                model = this.builderFactory.builder(LanguageBuilder.class).authorize(EnumSet.of(Public)).build(fieldSet, query.firstAs(fieldSet));
            }
            if (model == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{code, Language.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        } else {
            model = new Language();
            model.setCode(code);
        }

        if (model.getPayload() == null) {
            model.setPayload(this.languageService.getPayload(code));
        }
        this.auditService.track(AuditableAction.Language_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("code", code),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping(value = {"public/available-languages/{tenantCode}", "public/available-languages"})
    public QueryResult<String> queryLanguageCodes(@RequestBody LanguageLookup lookup, @PathVariable(value = "tenantCode", required = false) String tenantCode) {
        logger.debug("querying {}", Language.class.getSimpleName());

        this.censorFactory.censor(LanguageCensor.class).censor(lookup.getProject(), null);

        LanguageQuery query = lookup.enrich(this.queryFactory).authorize(EnumSet.of(Public));
        query.setOrder(new Ordering().addAscending(Language._ordinal));
        query.tenantIsSet(false);

        List<LanguageEntity> data = query.collectAs(new BaseFieldSet(Language._code));
        if (tenantCode != null && !tenantCode.isBlank() && !tenantCode.equals(this.tenantScope.getDefaultTenantCode())) {
            TenantEntity tenant = this.queryFactory.query(TenantQuery.class).codes(tenantCode).firstAs(new BaseFieldSet(Tenant._id));
            if (tenant == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{tenantCode, Tenant.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            query.tenantIsSet(true).tenantIds(tenant.getId());
            List<LanguageEntity> tenantLanguages = query.collectAs(new BaseFieldSet(Language._code));
            for (LanguageEntity l : tenantLanguages) {
               if (data.stream().noneMatch(d1 -> d1.getCode().equals(l.getCode()))) {
                   data.add(l);
               }
            }
        }

        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : data.size();

        return new QueryResult<>(data.stream().map(LanguageEntity::getCode).distinct().collect(Collectors.toList()), count);
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = LanguagePersist.LanguagePersistValidator.ValidatorName, argumentName = "model")
    public Language persist(@RequestBody LanguagePersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + Language.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(LanguageCensor.class).censor(fieldSet, null);

        Language persisted = this.languageService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Language_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Language.class.getSimpleName()).And("id", id));

        this.languageService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Language_Delete, "id", id);
    }

}
