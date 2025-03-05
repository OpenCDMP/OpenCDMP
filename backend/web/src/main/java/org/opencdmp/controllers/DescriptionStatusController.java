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
import org.opencdmp.data.DescriptionStatusEntity;
import org.opencdmp.model.builder.descriptionstatus.DescriptionStatusBuilder;
import org.opencdmp.model.censorship.descriptionstatus.DescriptionStatusCensor;
import org.opencdmp.model.descriptionstatus.DescriptionStatus;
import org.opencdmp.model.persist.descriptionstatus.DescriptionStatusPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.DescriptionStatusQuery;
import org.opencdmp.query.lookup.DescriptionStatusLookup;
import org.opencdmp.service.descriptionstatus.DescriptionStatusService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/description-status")
@io.swagger.v3.oas.annotations.tags.Tag(name = "DescriptionStatuses", description = "Manage description statuses")
@SwaggerCommonErrorResponses
public class DescriptionStatusController {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionStatusController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final DescriptionStatusService descriptionStatusService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public DescriptionStatusController(
            BuilderFactory builderFactory,
            AuditService auditService,
            DescriptionStatusService descriptionStatusService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.descriptionStatusService = descriptionStatusService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all descriptionStatuses", description = SwaggerHelpers.Tag.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Tag.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.Tag.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = DescriptionStatus.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Tag.endpoint_query_response_example
            ))))
    public QueryResult<DescriptionStatus> Query(@RequestBody DescriptionStatusLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", DescriptionStatus.class.getSimpleName());

        this.censorFactory.censor(DescriptionStatusCensor.class).censor(lookup.getProject(), null);

        DescriptionStatusQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<DescriptionStatusEntity> data = query.collectAs(lookup.getProject());
        List<DescriptionStatus> models = this.builderFactory.builder(DescriptionStatusBuilder.class).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.DescriptionStatus_Query);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific descriptionStatus by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionStatus.class
                    ))
            ))
    @Swagger404
    public DescriptionStatus Get(
            @Parameter(name = "id", description = "The id of a descriptionStatus to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + DescriptionStatus.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(DescriptionStatusCensor.class).censor(fieldSet, null);

        DescriptionStatusQuery query = this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        DescriptionStatus model = this.builderFactory.builder(DescriptionStatusBuilder.class).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionStatus.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.DescriptionStatus_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing descriptionStatus", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionStatus.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionStatusPersist.DescriptionStatusPersistValidation.ValidatorName, argumentName = "model")
    public DescriptionStatus Persist(
            @RequestBody DescriptionStatusPersist model,
            @Parameter(name = "fieldSet",description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException {
        logger.debug(new MapLogEntry("persisting"+DescriptionStatus.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        DescriptionStatus persisted = this.descriptionStatusService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.DescriptionStatus_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a descriptionStatus by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of descriptionStatus to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("deleting"+DescriptionStatus.class.getSimpleName()).And("id", id));

        this.descriptionStatusService.deleteAndSave(id);

        this.auditService.track(AuditableAction.DescriptionStatus_Delete, "id", id);
    }

}
