package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.opencdmp.data.ReferenceEntity;
import org.opencdmp.model.builder.reference.ReferenceBuilder;
import org.opencdmp.model.censorship.reference.ReferenceCensor;
import org.opencdmp.model.persist.ReferencePersist;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.ReferenceQuery;
import org.opencdmp.query.lookup.ReferenceLookup;
import org.opencdmp.query.lookup.ReferenceSearchLookup;
import org.opencdmp.query.lookup.ReferenceTestLookup;
import org.opencdmp.service.reference.ReferenceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/reference")
@Tag(name = "References", description = "Manage references")
@SwaggerCommonErrorResponses
public class ReferenceController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(ReferenceController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final ReferenceService referenceService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    @Autowired
    public ReferenceController(
            BuilderFactory builderFactory,
            ReferenceService referenceService,
            AuditService auditService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.referenceService = referenceService;
        this.auditService = auditService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all references", description = SwaggerHelpers.Reference.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Reference.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.Reference.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Reference.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Reference.endpoint_query_response_example
            ))))
    public QueryResult<Reference> query(@RequestBody ReferenceLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Reference.class.getSimpleName());

        this.censorFactory.censor(ReferenceCensor.class).censor(lookup.getProject(), null);

        ReferenceQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<ReferenceEntity> data = query.collectAs(lookup.getProject());
        List<Reference> models = this.builderFactory.builder(ReferenceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.Reference_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @PostMapping("search")
    @OperationWithTenantHeader(summary = "Query all references including results from external APIs", description = SwaggerHelpers.Reference.endpoint_search, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Reference.endpoint_search_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.Reference.endpoint_search_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Reference.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Reference.endpoint_search_response_example
            ))))
    @Swagger404
    public List<Reference> searchReference(@RequestBody ReferenceSearchLookup lookup) throws MyNotFoundException, InvalidApplicationException {
        logger.debug("search with db definition {}", Reference.class.getSimpleName());

        this.censorFactory.censor(ReferenceCensor.class).censor(lookup.getProject(), null);

        List<Reference> references = this.referenceService.searchReferenceData(lookup);

        this.auditService.track(AuditableAction.Reference_Search, "lookup", lookup);

        return references;
    }

    @PostMapping("test")
    @OperationWithTenantHeader(summary = "Test external APIs reference results", description = SwaggerHelpers.Reference.endpoint_test, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Reference.endpoint_search_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.Reference.endpoint_search_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Reference.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Reference.endpoint_search_response_example
            ))))
    @Swagger404
    public List<Reference> testReferenceWithDefinition(@RequestBody ReferenceTestLookup lookup) throws MyNotFoundException, InvalidApplicationException {
        logger.debug("search with db definition {}", Reference.class.getSimpleName());

        this.censorFactory.censor(ReferenceCensor.class).censor(lookup.getProject(), null);

        List<Reference> references = this.referenceService.testReferenceData(lookup);

        this.auditService.track(AuditableAction.Reference_Test, "lookup", lookup);

        return references;
    }

    @GetMapping("find/{referenceTypeId}")
    @OperationWithTenantHeader(summary = "Fetch a specific reference by type id and reference value", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Boolean.class
                    ))
            ))
    @Swagger404
    public Boolean findReference(
            @Parameter(name = "referenceTypeId", description = "The type id of a reference to check if it exists", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("referenceTypeId") UUID referenceTypeId,
            @Parameter(name = "reference", description = "The reference value of a reference to check if it exists", example = "EUPL-1.2 license", required = true) @RequestParam("reference") String reference
    ) throws MyNotFoundException {
        logger.debug("search with db definition {}", Reference.class.getSimpleName());

        this.censorFactory.censor(ReferenceCensor.class).censor(null, null);

        Boolean result = this.referenceService.findReference(reference, referenceTypeId);

        this.auditService.track(AuditableAction.Reference_Query, "reference", reference);

        return result;
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific reference by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Reference.class
                    ))
            ))
    @Swagger404
    public Reference get(
            @Parameter(name = "id", description = "The id of a reference to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Reference.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(ReferenceCensor.class).censor(fieldSet, null);

        ReferenceQuery query = this.queryFactory.query(ReferenceQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Reference model = this.builderFactory.builder(ReferenceBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Reference.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Reference_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing reference", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Reference.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = ReferencePersist.ReferencePersistValidator.ValidatorName, argumentName = "model")
    public Reference persist(
            @RequestBody ReferencePersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + Reference.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(ReferenceCensor.class).censor(fieldSet, null);

        Reference persisted = this.referenceService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Reference_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a reference by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of a reference to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Reference.class.getSimpleName()).And("id", id));

        this.referenceService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Reference_Delete, "id", id);
    }

}
