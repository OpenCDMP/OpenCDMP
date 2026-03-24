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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.PlanBlueprintTypeEntity;
import org.opencdmp.model.PlanBlueprintType;
import org.opencdmp.model.builder.PlanBlueprintTypeBuilder;
import org.opencdmp.model.censorship.PlanBlueprintTypeCensor;
import org.opencdmp.model.persist.PlanBlueprintTypePersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PlanBlueprintTypeQuery;
import org.opencdmp.query.lookup.PlanBlueprintTypeLookup;
import org.opencdmp.service.planblueprinttype.PlanBlueprintTypeService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/plan-blueprint-type")
@Tag(name = "Plan Blueprint Types", description = "Manage plan blueprint types", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "4")))
@SwaggerCommonErrorResponses
public class PlanBlueprintTypeController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintTypeController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final PlanBlueprintTypeService planBlueprintTypeService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public PlanBlueprintTypeController(
            BuilderFactory builderFactory,
            AuditService auditService,
            PlanBlueprintTypeService planBlueprintTypeService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.planBlueprintTypeService = planBlueprintTypeService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all plan blueprint types", description = SwaggerHelpers.PlanBlueprintType.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.PlanBlueprintType.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = PlanBlueprintType.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.PlanBlueprintType.endpoint_query_response_example
            ))))
    public QueryResult<PlanBlueprintType> Query(@RequestBody PlanBlueprintTypeLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PlanBlueprintType.class.getSimpleName());

        this.censorFactory.censor(PlanBlueprintTypeCensor.class).censor(lookup.getProject(), null);

        PlanBlueprintTypeQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<PlanBlueprintTypeEntity> data = query.collectAs(lookup.getProject());
        List<PlanBlueprintType> models = this.builderFactory.builder(PlanBlueprintTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.PlanBlueprintType_Query, "lookup", lookup);
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific plan blueprint type by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprintType.class
                    ))
            ))
    @Swagger404
    public PlanBlueprintType Get(
            @Parameter(name = "id", description = "The id of a plan blueprint type to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = "[\"id\"]")) FieldSet fieldSet, Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PlanBlueprintType.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PlanBlueprintTypeCensor.class).censor(fieldSet, null);

        PlanBlueprintTypeQuery query = this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PlanBlueprintType model = this.builderFactory.builder(PlanBlueprintTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprintType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.PlanBlueprintType_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing plan blueprint type", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprintType.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = PlanBlueprintTypePersist.PlanBlueprintTypePersistValidator.ValidatorName, argumentName = "model")
    public PlanBlueprintType Persist(
            @RequestBody PlanBlueprintTypePersist model,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = "[\"id\"]")) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + PlanBlueprintType.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        PlanBlueprintType persisted = this.planBlueprintTypeService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.PlanBlueprintType_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a plan blueprint type by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of a plan blueprint type to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + PlanBlueprintType.class.getSimpleName()).And("id", id));

        this.planBlueprintTypeService.deleteAndSave(id);

        this.auditService.track(AuditableAction.PlanBlueprintType_Delete, "id", id);
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
    }
}
