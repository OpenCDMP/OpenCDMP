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
import org.opencdmp.data.DescriptionTemplateTypeEntity;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.builder.DescriptionTemplateTypeBuilder;
import org.opencdmp.model.censorship.DescriptionTemplateTypeCensor;
import org.opencdmp.model.persist.DescriptionTemplateTypePersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.DescriptionTemplateTypeQuery;
import org.opencdmp.query.lookup.DescriptionTemplateTypeLookup;
import org.opencdmp.service.descriptiontemplatetype.DescriptionTemplateTypeService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/description-template-type")
@Tag(name = "Description Template Types", description = "Manage description template types", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "5")))
@SwaggerCommonErrorResponses
public class DescriptionTemplateTypeController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateTypeController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final DescriptionTemplateTypeService descriptionTemplateTypeService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public DescriptionTemplateTypeController(
            BuilderFactory builderFactory,
            AuditService auditService,
            DescriptionTemplateTypeService descriptionTemplateTypeService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.descriptionTemplateTypeService = descriptionTemplateTypeService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all description template types", description = SwaggerHelpers.DescriptionTemplateType.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.DescriptionTemplateType.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = DescriptionTemplateType.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.DescriptionTemplateType.endpoint_query_response_example
            ))))
    public QueryResult<DescriptionTemplateType> Query(@RequestBody DescriptionTemplateTypeLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", DescriptionTemplateType.class.getSimpleName());

        this.censorFactory.censor(DescriptionTemplateTypeCensor.class).censor(lookup.getProject(), null);

        DescriptionTemplateTypeQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<DescriptionTemplateTypeEntity> data = query.collectAs(lookup.getProject());
        List<DescriptionTemplateType> models = this.builderFactory.builder(DescriptionTemplateTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.DescriptionTemplateType_Query, "lookup", lookup);
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific description template type by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplateType.class
                    ))
            ))
    @Swagger404
    public DescriptionTemplateType Get(
            @Parameter(name = "id", description = "The id of a description template type to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = "[\"id\"]")) FieldSet fieldSet, Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + DescriptionTemplateType.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(DescriptionTemplateTypeCensor.class).censor(fieldSet, null);

        DescriptionTemplateTypeQuery query = this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        DescriptionTemplateType model = this.builderFactory.builder(DescriptionTemplateTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplateType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.DescriptionTemplateType_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing description template type", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplateType.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionTemplateTypePersist.DescriptionTemplateTypePersistValidator.ValidatorName, argumentName = "model")
    public DescriptionTemplateType Persist(
            @RequestBody DescriptionTemplateTypePersist model,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = "[\"id\"]")) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + DescriptionTemplateType.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        DescriptionTemplateType persisted = this.descriptionTemplateTypeService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.DescriptionTemplateType_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a description template type by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of a description template type to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + DescriptionTemplateType.class.getSimpleName()).And("id", id));

        this.descriptionTemplateTypeService.deleteAndSave(id);

        this.auditService.track(AuditableAction.DescriptionTemplateType_Delete, "id", id);
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
    }
}
