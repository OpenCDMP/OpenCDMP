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
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.TagEntity;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.TagBuilder;
import org.opencdmp.model.censorship.TagCensor;
import org.opencdmp.model.persist.TagPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.TagQuery;
import org.opencdmp.query.lookup.TagLookup;
import org.opencdmp.service.tag.TagService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.util.*;

@RestController
@RequestMapping(path = "api/tag")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Manage tags")
@SwaggerCommonErrorResponses
public class TagController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TagController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final TagService tagService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public TagController(
            BuilderFactory builderFactory,
            AuditService auditService,
            TagService tagService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.tagService = tagService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all tags", description = SwaggerHelpers.Tag.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Tag.endpoint_query_request_body, content = @Content(
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
                            implementation = Tag.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Tag.endpoint_query_response_example
            ))))
    public QueryResult<Tag> Query(@RequestBody TagLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Tag.class.getSimpleName());

        this.censorFactory.censor(TagCensor.class).censor(lookup.getProject(), null);

        TagQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<TagEntity> data = query.collectAs(lookup.getProject());
        List<Tag> models = this.builderFactory.builder(TagBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.Tag_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific tag by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Tag.class
                    ))
            ))
    @Swagger404
    public Tag Get(
            @Parameter(name = "id", description = "The id of a tag to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Tag.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(TagCensor.class).censor(fieldSet, null);

        TagQuery query = this.queryFactory.query(TagQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Tag model = this.builderFactory.builder(TagBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Tag.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Tag_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing tag", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Tag.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = TagPersist.TagPersistValidator.ValidatorName, argumentName = "model")
    public Tag Persist(
            @RequestBody TagPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + Tag.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        Tag persisted = this.tagService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Tag_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a tag by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of a tag to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Tag.class.getSimpleName()).And("id", id));

        this.tagService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Tag_Delete, "id", id);
    }
}
