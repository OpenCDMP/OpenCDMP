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
import org.opencdmp.data.PlanBlueprintEntity;
import org.opencdmp.model.builder.planblueprint.PlanBlueprintBuilder;
import org.opencdmp.model.censorship.planblueprint.PlanBlueprintCensor;
import org.opencdmp.model.persist.NewVersionPlanBlueprintPersist;
import org.opencdmp.model.persist.PlanBlueprintPersist;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PlanBlueprintQuery;
import org.opencdmp.query.lookup.PlanBlueprintLookup;
import org.opencdmp.service.planblueprint.PlanBlueprintService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/plan-blueprint")
@Tag(name = "Plan Blueprints", description = "Manage plan blueprints")
@SwaggerCommonErrorResponses
public class PlanBlueprintController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanBlueprintController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final PlanBlueprintService planBlueprintService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    public PlanBlueprintController(
            BuilderFactory builderFactory,
            AuditService auditService,
            PlanBlueprintService planBlueprintService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.planBlueprintService = planBlueprintService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all plan blueprints", description = SwaggerHelpers.PlanBlueprint.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.PlanBlueprint.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.PlanBlueprint.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = PlanBlueprint.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.PlanBlueprint.endpoint_query_response_example
            ))))
    public QueryResult<PlanBlueprint> query(@RequestBody PlanBlueprintLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PlanBlueprint.class.getSimpleName());

        this.censorFactory.censor(PlanBlueprintCensor.class).censor(lookup.getProject(), null);
        PlanBlueprintQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<PlanBlueprintEntity> data = query.collectAs(lookup.getProject());
        List<PlanBlueprint> models = this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.PlanBlueprint_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific plan blueprint by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprint.class
                    ))
            ))
    @Swagger404
    public PlanBlueprint get(
            @Parameter(name = "id", description = "The id of a plan blueprint to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PlanBlueprint.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PlanBlueprintCensor.class).censor(fieldSet, null);

        PlanBlueprintQuery query = this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        PlanBlueprint model = this.builderFactory.builder(PlanBlueprintBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PlanBlueprint.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.PlanBlueprint_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing plan blueprint", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprint.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = PlanBlueprintPersist.PlanBlueprintPersistValidator.ValidatorName, argumentName = "model")
    public PlanBlueprint persist(
            @RequestBody PlanBlueprintPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + PlanBlueprint.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(PlanBlueprintCensor.class).censor(fieldSet, null);

        PlanBlueprint persisted = this.planBlueprintService.persist(model, null, fieldSet);

        this.auditService.track(AuditableAction.PlanBlueprint_Persist, Map.ofEntries(
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
            @Parameter(name = "id", description = "The id of a plan blueprint to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + PlanBlueprint.class.getSimpleName()).And("id", id));

        this.planBlueprintService.deleteAndSave(id);

        this.auditService.track(AuditableAction.PlanBlueprint_Delete, "id", id);
    }

    @GetMapping("clone/{id}")
    @OperationWithTenantHeader(summary = "Clone a plan blueprint by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprint.class
                    ))
            ))
    @Swagger404
    public PlanBlueprint buildClone(
            @Parameter(name = "id", description = "The id of a plan blueprint to clone", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("clone" + PlanBlueprint.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PlanBlueprintCensor.class).censor(fieldSet, null);

        PlanBlueprint model = this.planBlueprintService.buildClone(id, fieldSet);

        this.auditService.track(AuditableAction.PlanBlueprint_Clone, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("new-version")
    @OperationWithTenantHeader(summary = "Create a new version of an existing plan blueprint", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprint.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = NewVersionPlanBlueprintPersist.NewVersionPlanBlueprintPersistValidator.ValidatorName, argumentName = "model")
    public PlanBlueprint createNewVersion(
            @RequestBody NewVersionPlanBlueprintPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws JAXBException, InvalidApplicationException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + NewVersionPlanBlueprintPersist.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        PlanBlueprint persisted = this.planBlueprintService.createNewVersion(model, fieldSet);

        this.auditService.track(AuditableAction.PlanBlueprint_PersistNewVersion, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xml/export/{id}", produces = "application/xml")
    @OperationWithTenantHeader(summary = "Export a plan blueprint in xml format by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public @ResponseBody ResponseEntity<byte[]> getXml(
            @Parameter(name = "id", description = "The id of a plan blueprint to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable UUID id
    ) throws JAXBException, ParserConfigurationException, IOException, TransformerException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export" + PlanBlueprint.class.getSimpleName()).And("id", id));

        ResponseEntity<byte[]> response = this.planBlueprintService.exportXml(id);

        this.auditService.track(AuditableAction.PlanBlueprint_GetXml, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = {"/xml/import/{groupId}", "/xml/import"})
    @OperationWithTenantHeader(summary = "Import a plan blueprint from an xml file", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PlanBlueprint.class
                    ))
            ))
    @Transactional
    public PlanBlueprint importXml(
            @RequestParam("file") MultipartFile file,
            @Parameter(name = "groupId", description = "The group id of a plan blueprint to import. This is optional.", example = "c0c163dc-2965-45a5-9608-f76030578609") @PathVariable(value = "groupId", required = false) UUID groupId,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws IOException, JAXBException, InvalidApplicationException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("clone" + PlanBlueprint.class.getSimpleName()).And("file", file));

        this.censorFactory.censor(PlanBlueprintCensor.class).censor(fieldSet, null);

        PlanBlueprint model = this.planBlueprintService.importXml(file.getBytes(), groupId, file.getOriginalFilename(), fieldSet);

        this.auditService.track(AuditableAction.PlanBlueprint_Import, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("file", file),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }
}
