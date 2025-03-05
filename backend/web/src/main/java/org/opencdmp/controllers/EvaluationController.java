package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.commons.web.authz.service.AuthorizationService;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.EvaluationEntity;
import org.opencdmp.model.builder.evaluation.EvaluationBuilder;
import org.opencdmp.model.censorship.evaluation.EvaluationCensor;
import org.opencdmp.model.evaluation.Evaluation;
import org.opencdmp.model.persist.EvaluationPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.EvaluationQuery;
import org.opencdmp.query.lookup.EvaluationLookup;
import org.opencdmp.service.evaluation.EvaluationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/evaluation")
@Tag(name = "Evaluation", description = "Manage evaluations")
@SwaggerCommonErrorResponses
public class EvaluationController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EvaluationController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final AuthorizationService authorizationService;

    private final EvaluationService evaluationService;


    @Autowired
    public EvaluationController(
            BuilderFactory builderFactory,
            AuditService auditService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource, AuthorizationService authorizationService, EvaluationService evaluationService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.authorizationService = authorizationService;
        this.evaluationService = evaluationService;

    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all evaluations", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody( content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Evaluation.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description
            ))))
    public QueryResult<Evaluation> query(@RequestBody EvaluationLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Evaluation.class.getSimpleName());

        this.censorFactory.censor(EvaluationCensor.class).censor(lookup.getProject(), null);

        EvaluationQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<EvaluationEntity> data = query.collectAs(lookup.getProject());
        List<Evaluation> models = this.builderFactory.builder(EvaluationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.Evaluation_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific evaluation by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Evaluation.class
                    ))
            ))
    @Swagger404
    public Evaluation get(
            @Parameter(name = "id", description = "The id of a evaluation to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Evaluation.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(EvaluationCensor.class).censor(fieldSet, null);

        EvaluationQuery query = this.queryFactory.query(EvaluationQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Evaluation model = this.builderFactory.builder(EvaluationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Evaluation.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Evaluation_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }


    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing evaluation", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Evaluation.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = EvaluationPersist.EvaluationPersistValidator.ValidatorName, argumentName = "model")
    public Evaluation persist(
            @RequestBody EvaluationPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + Evaluation.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

        this.censorFactory.censor(EvaluationCensor.class).censor(fieldSet, null);

        Evaluation persisted = this.evaluationService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Evaluation_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a evaluation by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of a evaluation to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Evaluation.class.getSimpleName()).And("id", id));

        this.evaluationService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Evaluation_Delete, "id", id);
    }

}
