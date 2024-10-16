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
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.model.builder.referencetype.ReferenceTypeBuilder;
import org.opencdmp.model.censorship.referencetype.ReferenceTypeCensor;
import org.opencdmp.model.persist.ReferenceTypePersist;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.ReferenceTypeQuery;
import org.opencdmp.query.lookup.ReferenceTypeLookup;
import org.opencdmp.service.referencetype.ReferenceTypeService;
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
@RequestMapping(path = "api/reference-type")
@Tag(name = "Reference types", description = "Manage reference types")
@SwaggerCommonErrorResponses
public class ReferenceTypeController{

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceTypeController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final AuthorizationService authorizationService;

    private final ReferenceTypeService referenceTypeService;

    @Autowired
    public ReferenceTypeController(
            BuilderFactory builderFactory,
            AuditService auditService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource, AuthorizationService authorizationService, ReferenceTypeService referenceTypeService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.authorizationService = authorizationService;
        this.referenceTypeService = referenceTypeService;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all reference types", description = SwaggerHelpers.ReferenceType.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.ReferenceType.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.ReferenceType.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = ReferenceType.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.ReferenceType.endpoint_query_response_example
            ))))
    public QueryResult<ReferenceType> query(@RequestBody ReferenceTypeLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", ReferenceType.class.getSimpleName());

        this.censorFactory.censor(ReferenceTypeCensor.class).censor(lookup.getProject(), null);

        ReferenceTypeQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<ReferenceTypeEntity> data = query.collectAs(lookup.getProject());
        List<ReferenceType> models = this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.ReferenceType_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific reference type by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = ReferenceType.class
                    ))
            ))
    @Swagger404
    public ReferenceType get(
            @Parameter(name = "id", description = "The id of a reference type to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + ReferenceType.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(ReferenceTypeCensor.class).censor(fieldSet, null);

        ReferenceTypeQuery query = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        ReferenceType model = this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.ReferenceType_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @GetMapping("code/{code}")
    @OperationWithTenantHeader(summary = "Fetch a specific reference type by code", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = ReferenceType.class
                    ))
            ))
    @Swagger404
    public ReferenceType get(
            @Parameter(name = "code", description = "The code of a reference type to fetch", example = "licences", required = true) @PathVariable("code") String code,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + ReferenceType.class.getSimpleName()).And("code", code).And("fields", fieldSet));

        this.censorFactory.censor(ReferenceTypeCensor.class).censor(fieldSet, null);

        ReferenceTypeQuery query = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).codes(code);
        ReferenceType model = this.builderFactory.builder(ReferenceTypeBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{code, ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.ReferenceType_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("code", code),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing reference type", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = ReferenceType.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = ReferenceTypePersist.ReferenceTypePersistValidator.ValidatorName, argumentName = "model")
    public ReferenceType persist(
            @RequestBody ReferenceTypePersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, JsonProcessingException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + ReferenceType.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(ReferenceTypeCensor.class).censor(fieldSet, null);

        ReferenceType persisted = this.referenceTypeService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.ReferenceType_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a plan blueprint by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of a reference type to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + ReferenceType.class.getSimpleName()).And("id", id));

        this.referenceTypeService.deleteAndSave(id);

        this.auditService.track(AuditableAction.ReferenceType_Delete, "id", id);
    }

}
