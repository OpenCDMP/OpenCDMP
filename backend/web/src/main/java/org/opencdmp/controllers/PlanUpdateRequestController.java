package org.opencdmp.controllers;

import tools.jackson.core.JacksonException;
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
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.data.PlanUpdateRequestEntity;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.model.builder.planupdaterequest.PlanUpdateRequestBuilder;
import org.opencdmp.model.censorship.PlanUpdateRequestCensor;
import org.opencdmp.model.persist.PlanSuggestion;
import org.opencdmp.model.persist.PlanUpdateRequestPersist;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planupdaterequest.PlanUpdateRequest;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PlanUpdateRequestQuery;
import org.opencdmp.query.lookup.PlanUpdateRequestLookup;
import org.opencdmp.service.planupdaterequest.PlanUpdateRequestService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@RequestMapping(path = "api/plan-update-request")
public class PlanUpdateRequestController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanUpdateRequestController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final PlanUpdateRequestService planUpdateRequestService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public PlanUpdateRequestController(
            BuilderFactory builderFactory,
            AuditService auditService,
            PlanUpdateRequestService planUpdateRequestService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.planUpdateRequestService = planUpdateRequestService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    public QueryResult<PlanUpdateRequest> Query(@RequestBody PlanUpdateRequestLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PlanUpdateRequest.class.getSimpleName());

        this.censorFactory.censor(PlanUpdateRequestCensor.class).censor(lookup.getProject());

        PlanUpdateRequestQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<PlanUpdateRequestEntity> data = query.collectAs(lookup.getProject());
        List<PlanUpdateRequest> models = this.builderFactory.builder(PlanUpdateRequestBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.PlanUpdateRequest_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    public PlanUpdateRequest Get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PlanUpdateRequest.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PlanUpdateRequestCensor.class).censor(fieldSet);

        PlanUpdateRequestQuery query = this.queryFactory.query(PlanUpdateRequestQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PlanUpdateRequest model = this.builderFactory.builder(PlanUpdateRequestBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanUpdateRequest.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.PlanUpdateRequest_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @Transactional
    @ValidationFilterAnnotation(validator = PlanUpdateRequestPersist.PlanUpdateRequestPersistValidator.ValidatorName, argumentName = "model")
    public PlanUpdateRequest Persist(
            @RequestBody PlanUpdateRequestPersist model,
            FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, JacksonException {
        logger.debug(new MapLogEntry("persisting" + PlanUpdateRequest.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        PlanUpdateRequest persisted = this.planUpdateRequestService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.PlanUpdateRequest_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @Transactional
    public void Delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + PlanUpdateRequest.class.getSimpleName()).And("id", id));

        this.planUpdateRequestService.deleteAndSave(id);

        this.auditService.track(AuditableAction.PlanUpdateRequest_Delete, "id", id);
    }

    @GetMapping("preprocessing/{id}")
    public PreprocessingPlanModel preprocessing(@PathVariable("id") UUID id) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("preprocessing plan update request" + PlanUpdateRequest.class.getSimpleName()).And("id", id));

        PreprocessingPlanModel model = this.planUpdateRequestService.preprocessing(id);

        this.auditService.track(AuditableAction.PlanUpdateRequest_Preprocessing, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));

        return model;
    }

    @PostMapping("build-plan")
    @Transactional
    @ValidationFilterAnnotation(validator = PlanSuggestion.PlanSuggestionValidator.ValidatorName, argumentName = "model")
    public Plan buildPlan(@RequestBody PlanSuggestion model) throws InvalidAlgorithmParameterException, JAXBException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException {
        logger.debug(new MapLogEntry("build plan from plan update suggestion" + PlanSuggestion.class.getSimpleName()).And("model", model));
        Plan plan = this.planUpdateRequestService.buildPlan(model);

        this.auditService.track(AuditableAction.PlanUpdateRequest_BuildPlan, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return plan;
    }
}
