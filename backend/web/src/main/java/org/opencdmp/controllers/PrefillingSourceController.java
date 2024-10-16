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
import org.opencdmp.data.PrefillingSourceEntity;
import org.opencdmp.model.Prefilling;
import org.opencdmp.model.builder.prefillingsource.PrefillingSourceBuilder;
import org.opencdmp.model.censorship.description.DescriptionCensor;
import org.opencdmp.model.censorship.prefillingsource.PrefillingCensor;
import org.opencdmp.model.censorship.prefillingsource.PrefillingSourceCensor;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.persist.DescriptionPrefillingRequest;
import org.opencdmp.model.persist.PrefillingSearchRequest;
import org.opencdmp.model.persist.PrefillingSourcePersist;
import org.opencdmp.model.prefillingsource.PrefillingSource;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PrefillingSourceQuery;
import org.opencdmp.query.lookup.PrefillingSourceLookup;
import org.opencdmp.service.fieldsetexpander.FieldSetExpanderService;
import org.opencdmp.service.prefillingsource.PrefillingSourceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/prefilling-source")
public class PrefillingSourceController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PrefillingSourceController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;
    private final PrefillingSourceService prefillingSourceService;
    private final FieldSetExpanderService fieldSetExpanderService;

    @Autowired
    public PrefillingSourceController(
		    BuilderFactory builderFactory,
		    AuditService auditService,
		    CensorFactory censorFactory,
		    QueryFactory queryFactory,
		    MessageSource messageSource, PrefillingSourceService prefillingSourceService, FieldSetExpanderService fieldSetExpanderService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.prefillingSourceService = prefillingSourceService;
	    this.fieldSetExpanderService = fieldSetExpanderService;
    }

    @PostMapping("query")
    public QueryResult<PrefillingSource> query(@RequestBody PrefillingSourceLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PrefillingSource.class.getSimpleName());

        this.censorFactory.censor(PrefillingSourceCensor.class).censor(lookup.getProject(), null);

        PrefillingSourceQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<PrefillingSourceEntity> data = query.collectAs(lookup.getProject());
        List<PrefillingSource> models = this.builderFactory.builder(PrefillingSourceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.PrefillingSource_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public PrefillingSource get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PrefillingSource.class.getSimpleName()).And("id", id).And("fields", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(PrefillingSourceCensor.class).censor(fieldSet, null);

        PrefillingSourceQuery query = this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PrefillingSource model = this.builderFactory.builder(PrefillingSourceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PrefillingSource.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.PrefillingSource_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = PrefillingSourcePersist.PrefillingSourcePersistValidator.ValidatorName, argumentName = "model")
    public PrefillingSource persist(@RequestBody PrefillingSourcePersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + PrefillingSource.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);
        this.censorFactory.censor(PrefillingSourceCensor.class).censor(fieldSet, null);

        PrefillingSource persisted = this.prefillingSourceService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.PrefillingSource_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + PrefillingSource.class.getSimpleName()).And("id", id));

        this.prefillingSourceService.deleteAndSave(id);

        this.auditService.track(AuditableAction.PrefillingSource_Delete, "id", id);
    }

    @PostMapping("search")
    @ValidationFilterAnnotation(validator = PrefillingSearchRequest.PrefillingSearchRequestValidator.ValidatorName, argumentName = "model")
    public List<Prefilling> search(@RequestBody PrefillingSearchRequest model) throws MyApplicationException, MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("searching" + Prefilling.class.getSimpleName()).And("model", model));

        this.censorFactory.censor(PrefillingCensor.class).censor(null, null);

        List<Prefilling> item = this.prefillingSourceService.searchPrefillings(model);

        this.auditService.track(AuditableAction.PrefillingSource_Generate, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return item;
    }

    @PostMapping("generate")
    @ValidationFilterAnnotation(validator = DescriptionPrefillingRequest.DescriptionProfilingRequestValidator.ValidatorName, argumentName = "model")
    public  Description generate(@RequestBody DescriptionPrefillingRequest model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + Prefilling.class.getSimpleName()).And("model", model));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(DescriptionCensor.class).censor(fieldSet, null);

        Description item = this.prefillingSourceService.getPrefilledDescription(model, fieldSet);

        this.auditService.track(AuditableAction.PrefillingSource_Generate, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return item;
    }

}
