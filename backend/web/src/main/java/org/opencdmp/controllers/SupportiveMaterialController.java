package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
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
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.data.SupportiveMaterialEntity;
import org.opencdmp.data.TenantEntity;
import org.opencdmp.model.SupportiveMaterial;
import org.opencdmp.model.Tenant;
import org.opencdmp.model.builder.SupportiveMaterialBuilder;
import org.opencdmp.model.censorship.SupportiveMaterialCensor;
import org.opencdmp.model.persist.SupportiveMaterialPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.SupportiveMaterialQuery;
import org.opencdmp.query.TenantQuery;
import org.opencdmp.query.lookup.SupportiveMaterialLookup;
import org.opencdmp.service.supportivematerial.SupportiveMaterialService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@RestController
@RequestMapping(path = "/api/supportive-material")
public class SupportiveMaterialController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(SupportiveMaterialController.class));


    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final SupportiveMaterialService supportiveMaterialService;

    private final TenantScope tenantScope;

    @Autowired
    public SupportiveMaterialController(SupportiveMaterialService supportiveMaterialService, BuilderFactory builderFactory,
                                        AuditService auditService, CensorFactory censorFactory, QueryFactory queryFactory, MessageSource messageSource, TenantScope tenantScope) {
        this.supportiveMaterialService = supportiveMaterialService;
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.tenantScope = tenantScope;
    }

    @PostMapping("query")
    public QueryResult<SupportiveMaterial> query(@RequestBody SupportiveMaterialLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", SupportiveMaterial.class.getSimpleName());

        this.censorFactory.censor(SupportiveMaterialCensor.class).censor(lookup.getProject(), null);

        SupportiveMaterialQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<SupportiveMaterialEntity> data = query.collectAs(lookup.getProject());
        List<SupportiveMaterial> models = this.builderFactory.builder(SupportiveMaterialBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.SupportiveMaterial_Query, "lookup", lookup);

        return new QueryResult<SupportiveMaterial>(models, count);
    }

    @GetMapping("{id}")
    public SupportiveMaterial get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving " + SupportiveMaterial.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(SupportiveMaterialCensor.class).censor(fieldSet, null);

        SupportiveMaterialQuery query = this.queryFactory.query(SupportiveMaterialQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        SupportiveMaterial model = this.builderFactory.builder(SupportiveMaterialBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, SupportiveMaterial.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.SupportiveMaterial_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("get-payload-from-file/{type}/{language}")
    public String getPayloadFromFile(@PathVariable("type") Short type, @PathVariable("language") String language) throws IOException {
        logger.debug("get payload from file {}", SupportiveMaterial.class.getSimpleName());

        byte[] content = this.supportiveMaterialService.loadFromFile(language, SupportiveMaterialFieldType.of(type));

        return new String(content, StandardCharsets.UTF_8);
    }

    @GetMapping(value = {"public/get-payload/{type}/{language}/{tenantCode}", "public/get-payload/{type}/{language}"})
    public ResponseEntity<byte[]> getPayload(@PathVariable("type") Short type, @PathVariable("language") String language, @PathVariable(value = "tenantCode", required = false) String tenantCode) throws IOException {
        logger.debug("querying {}", SupportiveMaterial.class.getSimpleName());

        SupportiveMaterialQuery query = this.queryFactory.query(SupportiveMaterialQuery.class).disableTracking().types(SupportiveMaterialFieldType.of(type)).languageCodes(language).authorize(EnumSet.of(Public)).tenantIsSet(false).isActive(IsActive.Active);
        SupportiveMaterialEntity data = null;

        if (tenantCode != null && !tenantCode.isEmpty() && !tenantCode.equals(this.tenantScope.getDefaultTenantCode())) {
            TenantEntity tenant = this.queryFactory.query(TenantQuery.class).codes(tenantCode).firstAs(new BaseFieldSet(Tenant._id));
            if (tenant == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{tenantCode, Tenant.class.getSimpleName()}, LocaleContextHolder.getLocale()));

            query.tenantIds(tenant.getId()).tenantIsSet(true);
            data = query.firstAs(new BaseFieldSet().ensure(SupportiveMaterial._id).ensure(SupportiveMaterial._payload));
            if (data == null) {
                query.clearTenantIds().tenantIsSet(false);
                data = query.firstAs(new BaseFieldSet().ensure(SupportiveMaterial._id).ensure(SupportiveMaterial._payload));
            }
        } else {
            data = query.firstAs(new BaseFieldSet().ensure(SupportiveMaterial._id).ensure(SupportiveMaterial._payload));
        }

        byte[] content;
        if (data != null && data.getPayload() != null && !data.getPayload().isEmpty()) content = data.getPayload().getBytes();
        else content = this.supportiveMaterialService.loadFromFile(language, SupportiveMaterialFieldType.of(type));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(content.length);
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        responseHeaders.set("Content-Disposition", "attachment;filename=" + SupportiveMaterialFieldType.of(type).name().toLowerCase(Locale.ROOT) + "_" + language + "html");
        responseHeaders.set("Access-Control-Expose-Headers", "Content-Disposition");
        responseHeaders.get("Access-Control-Expose-Headers").add("Content-Type");

        return new ResponseEntity<>(content, responseHeaders, HttpStatus.OK);
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = SupportiveMaterialPersist.SupportiveMaterialPersistValidator.ValidatorName, argumentName = "model")
    public SupportiveMaterial persist(@RequestBody SupportiveMaterialPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + SupportiveMaterial.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(SupportiveMaterialCensor.class).censor(fieldSet, null);

        SupportiveMaterial persisted = this.supportiveMaterialService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.SupportiveMaterial_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));
        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + SupportiveMaterial.class.getSimpleName()).And("id", id));

        this.supportiveMaterialService.deleteAndSave(id);

        this.auditService.track(AuditableAction.SupportiveMaterial_Delete, "id", id);
    }

}
