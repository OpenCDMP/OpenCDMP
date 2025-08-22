package org.opencdmp.controllers;

import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
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
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.DescriptionWorkflowEntity;
import org.opencdmp.model.builder.descriptionworkflow.DescriptionWorkflowBuilder;
import org.opencdmp.model.censorship.descriptionworkflow.DescriptionWorkflowCensor;
import org.opencdmp.model.descriptionworkflow.DescriptionWorkflow;
import org.opencdmp.model.persist.descriptionworkflow.DescriptionWorkflowPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.DescriptionWorkflowQuery;
import org.opencdmp.query.lookup.DescriptionWorkflowLookup;
import org.opencdmp.service.descriptionworkflow.DescriptionWorkflowService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/description-workflow")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Description Workflows", description = "Manage description workflows", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "17")))
@SwaggerCommonErrorResponses
public class DescriptionWorkflowController {

    private final static LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionWorkflowController.class));

    private final BuilderFactory builderFactory;
    private final CensorFactory censorFactory;
    private final QueryFactory queryFactory;
    private final DescriptionWorkflowService descriptionWorkflowService;
    private final AuditService auditService;
    private final TenantScope tenantScope;
    private final MessageSource messageSource;

    public DescriptionWorkflowController(BuilderFactory builderFactory, CensorFactory censorFactory, QueryFactory queryFactory, DescriptionWorkflowService descriptionWorkflowService, AuditService auditService, TenantScope tenantScope, MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.descriptionWorkflowService = descriptionWorkflowService;
        this.auditService = auditService;
        this.tenantScope = tenantScope;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all descriptionWorkflows", description = SwaggerHelpers.DescriptionWorkflow.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.DescriptionWorkflow.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = DescriptionWorkflow.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.DescriptionWorkflow.endpoint_query_response_example
            ))))

    public QueryResult<DescriptionWorkflow> Query(@RequestBody DescriptionWorkflowLookup lookup) {
        logger.debug("querying {}", DescriptionWorkflow.class.getSimpleName());

        this.censorFactory.censor(DescriptionWorkflowCensor.class).censor(lookup.getProject(), null);

        DescriptionWorkflowQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<DescriptionWorkflowEntity> data = query.collectAs(lookup.getProject());
        List<DescriptionWorkflow> models = this.builderFactory.builder(DescriptionWorkflowBuilder.class).build(lookup.getProject(), data);
        long count = ( lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.DescriptionWorkflow_Query);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific description workflow by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionWorkflow.class
                    ))
            ))
    @Swagger404
    public DescriptionWorkflow Get(
            @Parameter(name = "id", description = "The id of a Description Workflow to fetch", example = "8651af83-8b24-4776-ae45-329031db9f5e", required = true) @PathVariable UUID id,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.DescriptionWorkflow.endpoint_field_set_example)) FieldSet fieldSet,
            Locale locale
    ) {
        logger.debug(new MapLogEntry("retrieving" + DescriptionWorkflow.class.getSimpleName()).And("id", id).And("fieldSet", fieldSet));

        this.censorFactory.censor(DescriptionWorkflowCensor.class).censor(fieldSet, null);

        DescriptionWorkflowQuery query = this.queryFactory.query(DescriptionWorkflowQuery.class).authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        DescriptionWorkflow model = this.builderFactory.builder(DescriptionWorkflowBuilder.class).build(fieldSet, query.firstAs(fieldSet));
        if (model == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionWorkflow.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.DescriptionWorkflow_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fieldSet", fieldSet)
        ));

        return model;
    }

    @GetMapping("current-tenant")
    @OperationWithTenantHeader(summary = "Fetch a specific description workflow by current tenant", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionWorkflow.class
                    ))
            ))
    @Swagger404
    public DescriptionWorkflow GetByCurrentTenant(
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.DescriptionWorkflow.endpoint_field_set_example)) FieldSet fieldSet,
            Locale locale
    ) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + DescriptionWorkflow.class.getSimpleName()).And("fieldSet", fieldSet));

        this.censorFactory.censor(DescriptionWorkflowCensor.class).censor(fieldSet, null);

        DescriptionWorkflowQuery query = this.queryFactory.query(DescriptionWorkflowQuery.class).authorize(AuthorizationFlags.AllExceptPublic).isActives(IsActive.Active);

        if (this.tenantScope.isDefaultTenant()) query.tenantIsSet(false);
        else query.tenantIsSet(true).tenantIds(this.tenantScope.getTenant());

        DescriptionWorkflow model = this.builderFactory.builder(DescriptionWorkflowBuilder.class).build(fieldSet, query.firstAs(fieldSet));

        this.auditService.track(AuditableAction.DescriptionWorkflow_Lookup, "fieldSet", fieldSet);

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing description workflow", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionWorkflow.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionWorkflowPersist.DescriptionWorkflowPersistValidator.ValidatorName, argumentName = "model")
    public DescriptionWorkflow Persist(
            @RequestBody DescriptionWorkflowPersist model,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.DescriptionWorkflow.endpoint_field_set_example)) FieldSet fieldSet
    ) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting"+DescriptionWorkflow.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

        DescriptionWorkflow persisted = this.descriptionWorkflowService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.DescriptionWorkflow_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fieldSet", fieldSet)
        ));

        return persisted;
    }


    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a description workflow by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of description workflow to delete", required = true) @PathVariable UUID id
    ) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("deleting"+DescriptionWorkflow.class.getSimpleName()).And("id", id));

        this.descriptionWorkflowService.deleteAndSave(id);

        this.auditService.track(AuditableAction.DescriptionWorkflow_Delete, "id", id);
    }
}
