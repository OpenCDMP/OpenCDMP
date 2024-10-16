package org.opencdmp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
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
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.model.builder.descriptiontemplate.DescriptionTemplateBuilder;
import org.opencdmp.model.censorship.descriptiontemplate.DescriptionTemplateCensor;
import org.opencdmp.model.censorship.planblueprint.PlanBlueprintCensor;
import org.opencdmp.model.descriptiontemplate.DescriptionTemplate;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.persist.DescriptionTemplatePersist;
import org.opencdmp.model.persist.NewVersionDescriptionTemplatePersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.DescriptionTemplateQuery;
import org.opencdmp.query.lookup.DescriptionTemplateLookup;
import org.opencdmp.service.descriptiontemplate.DescriptionTemplateService;
import org.opencdmp.service.fieldsetexpander.FieldSetExpanderService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping(path = "api/description-template")
@Tag(name = "Description Templates", description = "Manage description templates")
@SwaggerCommonErrorResponses
public class DescriptionTemplateController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionTemplateController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final DescriptionTemplateService descriptionTemplateTypeService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final FieldSetExpanderService fieldSetExpanderService;

    public DescriptionTemplateController(
            BuilderFactory builderFactory,
            AuditService auditService,
            DescriptionTemplateService descriptionTemplateTypeService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource, FieldSetExpanderService fieldSetExpanderService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.descriptionTemplateTypeService = descriptionTemplateTypeService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.fieldSetExpanderService = fieldSetExpanderService;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all description templates", description = SwaggerHelpers.DescriptionTemplate.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.DescriptionTemplate.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.DescriptionTemplate.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = DescriptionTemplate.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.DescriptionTemplate.endpoint_query_response_example
            ))))
    public QueryResult<DescriptionTemplate> query(@RequestBody DescriptionTemplateLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", DescriptionTemplate.class.getSimpleName());

        this.censorFactory.censor(DescriptionTemplateCensor.class).censor(lookup.getProject(), null);

        DescriptionTemplateQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);

        List<DescriptionTemplateEntity> data = query.collectAs(lookup.getProject());
        List<DescriptionTemplate> models = this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.DescriptionTemplate_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific description template by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplate.class
                    ))
            ))
    @Swagger404
    public DescriptionTemplate get(
            @Parameter(name = "id", description = "The id of a description template to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + DescriptionTemplate.class.getSimpleName()).And("id", id).And("fields", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(DescriptionTemplateCensor.class).censor(fieldSet, null);

        DescriptionTemplateQuery query = this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        DescriptionTemplate model = this.builderFactory.builder(DescriptionTemplateBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, DescriptionTemplate.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.DescriptionTemplate_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing description template", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplate.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionTemplatePersist.DescriptionTemplatePersistValidator.ValidatorName, argumentName = "model")
    public DescriptionTemplate persist(
            @RequestBody DescriptionTemplatePersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + DescriptionTemplate.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);
        new BaseFieldSet(fieldSet.getFields()).ensure(DescriptionTemplate._id);
        DescriptionTemplate persisted = this.descriptionTemplateTypeService.persist(model, null, fieldSet);

        this.auditService.track(AuditableAction.DescriptionTemplate_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a description template by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of a description template to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + DescriptionTemplate.class.getSimpleName()).And("id", id));

        this.descriptionTemplateTypeService.deleteAndSave(id);

        this.auditService.track(AuditableAction.DescriptionTemplate_Delete, "id", id);
    }

    @GetMapping("clone/{id}")
    @OperationWithTenantHeader(summary = "Clone a description template by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplate.class
                    ))
            ))
    @Swagger404
    public DescriptionTemplate buildClone(
            @Parameter(name = "id", description = "The id of a description template to clone", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("clone" + PlanBlueprint.class.getSimpleName()).And("id", id).And("fields", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(PlanBlueprintCensor.class).censor(fieldSet, null);

        DescriptionTemplate model = this.descriptionTemplateTypeService.buildClone(id, fieldSet);

        this.auditService.track(AuditableAction.DescriptionTemplate_Clone, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("new-version")
    @OperationWithTenantHeader(summary = "Create a new version of a description template", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplate.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = NewVersionDescriptionTemplatePersist.NewVersionDescriptionTemplatePersistValidator.ValidatorName, argumentName = "model")
    public DescriptionTemplate createNewVersion(
            @RequestBody NewVersionDescriptionTemplatePersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, JAXBException, ParserConfigurationException, JsonProcessingException, TransformerException {
        logger.debug(new MapLogEntry("persisting" + NewVersionDescriptionTemplatePersist.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);
        DescriptionTemplate persisted = this.descriptionTemplateTypeService.createNewVersion(model, fieldSet);

        this.auditService.track(AuditableAction.DescriptionTemplate_PersistNewVersion, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xml/export/{id}", produces = "application/xml")
    @OperationWithTenantHeader(summary = "Export a description template in xml by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public @ResponseBody ResponseEntity<byte[]> getXml(
            @Parameter(name = "id", description = "The id of a description template to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable UUID id
    ) throws JAXBException, ParserConfigurationException, IOException, TransformerException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export" + DescriptionTemplate.class.getSimpleName()).And("id", id));

        ResponseEntity<byte[]> response = this.descriptionTemplateTypeService.exportXml(id);

        this.auditService.track(AuditableAction.DescriptionTemplate_GetXml, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));

        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = {"/xml/import/{groupId}", "/xml/import"})
    @OperationWithTenantHeader(summary = "Import a description template from an xml file", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = DescriptionTemplate.class
                    ))
            ))
    @Transactional
    public DescriptionTemplate importXml(
            @RequestParam("file") MultipartFile file,
            @Parameter(name = "groupId", description = "The group id of a description template to import. This is optional.", example = "c0c163dc-2965-45a5-9608-f76030578609") @PathVariable(value = "groupId", required = false) UUID groupId,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws IOException, JAXBException, InvalidApplicationException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import" + DescriptionTemplate.class.getSimpleName()).And("file", file).And("groupId", groupId));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(DescriptionTemplateCensor.class).censor(fieldSet, null);

        DescriptionTemplate model = this.descriptionTemplateTypeService.importXml(file.getBytes(), groupId, file.getOriginalFilename(), fieldSet);

        this.auditService.track(AuditableAction.DescriptionTemplate_Import, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("file", file),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

}
