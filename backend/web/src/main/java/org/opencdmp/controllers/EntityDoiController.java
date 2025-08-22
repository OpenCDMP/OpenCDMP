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
import org.opencdmp.data.EntityDoiEntity;
import org.opencdmp.model.DescriptionTemplateType;
import org.opencdmp.model.EntityDoi;
import org.opencdmp.model.builder.EntityDoiBuilder;
import org.opencdmp.model.censorship.EntityDoiCensor;
import org.opencdmp.model.persist.EntityDoiPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.EntityDoiQuery;
import org.opencdmp.query.lookup.EntityDoiLookup;
import org.opencdmp.service.entitydoi.EntityDoiService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/entity-doi")
@Tag(name = "Entity DOIs", description = "Manage entity dois", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "13")))
@SwaggerCommonErrorResponses
public class EntityDoiController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EntityDoiController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final EntityDoiService entityDoiService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public EntityDoiController(
            BuilderFactory builderFactory,
            AuditService auditService,
            EntityDoiService entityDoiService, CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.entityDoiService = entityDoiService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all entity dois", description = SwaggerHelpers.EntityDoi.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.EntityDoi.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = EntityDoi.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.EntityDoi.endpoint_query_response_example
            ))))
    public QueryResult<EntityDoi> query(@RequestBody EntityDoiLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", EntityDoi.class.getSimpleName());

        this.censorFactory.censor(EntityDoiCensor.class).censor(lookup.getProject(), null);

        EntityDoiQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<EntityDoiEntity> data = query.collectAs(lookup.getProject());
        List<EntityDoi> models = this.builderFactory.builder(EntityDoiBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.EntityDoi_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific entity doi by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = EntityDoi.class
                    ))
            ))
    @Swagger404
    public EntityDoi get(
            @Parameter(name = "id", description = "The id of an entity doi to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.EntityDoi.endpoint_field_set_example)) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + EntityDoi.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(EntityDoiCensor.class).censor(fieldSet, null);

        EntityDoiQuery query = this.queryFactory.query(EntityDoiQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        EntityDoi model = this.builderFactory.builder(EntityDoiBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, EntityDoi.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.EntityDoi_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing entity doi", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = EntityDoi.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = EntityDoiPersist.EntityDoiPersistValidator.ValidatorName, argumentName = "model")
    public EntityDoi persist(
            @RequestBody EntityDoiPersist model,
            @Parameter(name = "f", description = SwaggerHelpers.Commons.fieldset_description, required = true, style = ParameterStyle.FORM, explode = Explode.TRUE, schema = @Schema(type = "array", example = SwaggerHelpers.EntityDoi.endpoint_field_set_example)) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + DescriptionTemplateType.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        EntityDoi persisted = this.entityDoiService.persist(model, false, fieldSet);

        this.auditService.track(AuditableAction.EntityDoi_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete an entity doi by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of an entity doi to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + EntityDoi.class.getSimpleName()).And("id", id));

        this.entityDoiService.deleteAndSave(id);

        this.auditService.track(AuditableAction.EntityDoi_Delete, "id", id);
    }

}
