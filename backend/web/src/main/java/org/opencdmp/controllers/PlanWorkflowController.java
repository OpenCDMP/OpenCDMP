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
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.PlanWorkflowEntity;
import org.opencdmp.model.builder.planworkflow.PlanWorkflowBuilder;
import org.opencdmp.model.censorship.planworkflow.PlanWorkflowCensor;
import org.opencdmp.model.persist.planworkflow.PlanWorkflowPersist;
import org.opencdmp.model.planworkflow.PlanWorkflow;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PlanWorkflowQuery;
import org.opencdmp.query.lookup.PlanWorkflowLookup;
import org.opencdmp.service.planworkflow.PlanWorkflowService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/plan-workflow")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Plan Workflows", description = "Manage plan workflows", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "16")))
@SwaggerCommonErrorResponses
public class PlanWorkflowController {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanWorkflowController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final PlanWorkflowService planWorkflowService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final TenantScope tenantScope;

    private final MessageSource messageSource;

    public PlanWorkflowController(
            BuilderFactory builderFactory,
            AuditService auditService,
            PlanWorkflowService planWorkflowService,
            CensorFactory censorFactory,
            QueryFactory queryFactory, TenantScope tenantScope,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.planWorkflowService = planWorkflowService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.tenantScope = tenantScope;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all plan workflows", description = SwaggerHelpers.PlanWorkflow.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.PlanWorkflow.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = PlanWorkflow.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.PlanWorkflow.endpoint_query_response_example
            ))))
    public QueryResult<PlanWorkflow> Query(@RequestBody PlanWorkflowLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PlanWorkflow.class.getSimpleName());

        this.censorFactory.censor(PlanWorkflowCensor.class).censor(lookup.getProject(), null);

        PlanWorkflowQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<PlanWorkflowEntity> data = query.collectAs(lookup.getProject());
        List<PlanWorkflow> models = this.builderFactory.builder(PlanWorkflowBuilder.class).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.PlanWorkflow_Query);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific Plan Workflow by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanWorkflow.class
                    ))
            ))
    @Swagger404
    public PlanWorkflow Get(
            @Parameter(name = "id", description = "The id of a Plan Workflow to fetch", example = "44df0e24-7879-48cc-bbe0-cd8a2b618855", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.PlanWorkflow.endpoint_field_set_example)) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PlanWorkflow.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PlanWorkflowCensor.class).censor(fieldSet, null);

        PlanWorkflowQuery query = this.queryFactory.query(PlanWorkflowQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PlanWorkflow model = this.builderFactory.builder(PlanWorkflowBuilder.class).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanWorkflow.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.PlanWorkflow_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("current-tenant")
    @OperationWithTenantHeader(summary = "Fetch a specific plan workflow by current tenant", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanWorkflow.class
                    ))
            ))
    @Swagger404
    public PlanWorkflow GetByCurrentTenant(
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.PlanWorkflow.endpoint_field_set_example)) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + PlanWorkflow.class.getSimpleName()).And("fields", fieldSet));

        this.censorFactory.censor(PlanWorkflowCensor.class).censor(fieldSet, null);

        PlanWorkflowQuery query = this.queryFactory.query(PlanWorkflowQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).isActives(IsActive.Active);

        if (this.tenantScope.isDefaultTenant()) query.tenantIsSet(false);
        else query.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());

        PlanWorkflow model = this.builderFactory.builder(PlanWorkflowBuilder.class).build(fieldSet, query.firstAs(fieldSet));

        this.auditService.track(AuditableAction.PlanWorkflow_Lookup, "fields", fieldSet);

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing PlanWorkflow", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanWorkflow.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = PlanWorkflowPersist.PlanWorkflowPersistValidator.ValidatorName, argumentName = "model")
    public PlanWorkflow Persist(
            @RequestBody PlanWorkflowPersist model,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.PlanWorkflow.endpoint_field_set_example)) FieldSet fieldSet
            ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting"+PlanWorkflow.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        PlanWorkflow persisted = this.planWorkflowService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.PlanWorkflow_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a plan workflow by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of PlanWorkflow to delete", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("deleting"+ PlanWorkflow.class.getSimpleName()).And("id", id));

        this.planWorkflowService.deleteAndSave(id);

        this.auditService.track(AuditableAction.PlanWorkflow_Delete, "id", id);
    }
}
