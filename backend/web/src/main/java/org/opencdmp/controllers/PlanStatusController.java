package org.opencdmp.controllers;

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
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.model.builder.planstatus.PlanStatusBuilder;
import org.opencdmp.model.censorship.planstatus.PlanStatusCensor;
import org.opencdmp.model.persist.planstatus.PlanStatusPersist;
import org.opencdmp.model.planstatus.PlanStatus;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PlanStatusQuery;
import org.opencdmp.query.lookup.PlanStatusLookup;
import org.opencdmp.service.planstatus.PlanStatusService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/plan-status")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Plan Statuses", description = "Manage plan statuses", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "14")))
@SwaggerCommonErrorResponses
public class PlanStatusController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanStatusController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final PlanStatusService planStatusService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public PlanStatusController(
            BuilderFactory builderFactory,
            AuditService auditService,
            PlanStatusService planStatusService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.planStatusService = planStatusService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all plan statuses", description = SwaggerHelpers.PlanStatus.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.PlanStatus.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = PlanStatus.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.PlanStatus.endpoint_query_response_example
            ))))
    public QueryResult<PlanStatus> Query(@RequestBody PlanStatusLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PlanStatus.class.getSimpleName());

        this.censorFactory.censor(PlanStatusCensor.class).censor(lookup.getProject(), null);

        PlanStatusQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<PlanStatusEntity> data = query.collectAs(lookup.getProject());
        List<PlanStatus> models = this.builderFactory.builder(PlanStatusBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.PlanStatus_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific plan status by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanStatus.class
                    ))
            ))
    @Swagger404
    public PlanStatus Get(
            @Parameter(name = "id", description = "The id of a plan status to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = false, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.PlanStatus.endpoint_field_set_example, allowableValues = SwaggerHelpers.Principal.available_field_set )) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PlanStatus.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PlanStatusCensor.class).censor(fieldSet, null);

        PlanStatusQuery query = this.queryFactory.query(PlanStatusQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PlanStatus model = this.builderFactory.builder(PlanStatusBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.PlanStatus_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing plan status", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanStatus.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = PlanStatusPersist.PlanStatusPersistValidator.ValidatorName, argumentName = "model")
    public PlanStatus Persist(
            @RequestBody PlanStatusPersist model,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.PlanStatus.endpoint_field_set_example, allowableValues = SwaggerHelpers.Principal.available_field_set )) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting" + PlanStatus.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        PlanStatus persisted = this.planStatusService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.PlanStatus_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a plan status by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of a plan status to delete", example = "cb3ced76-9807-4829-82da-75777de1bc78", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + PlanStatus.class.getSimpleName()).And("id", id));

        this.planStatusService.deleteAndSave(id);

        this.auditService.track(AuditableAction.PlanStatus_Delete, "id", id);
    }
}
