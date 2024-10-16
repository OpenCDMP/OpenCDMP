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
import io.swagger.v3.oas.annotations.Hidden;
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
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.model.DescriptionValidationResult;
import org.opencdmp.model.PublicDescription;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.PublicDescriptionBuilder;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.censorship.PublicDescriptionCensor;
import org.opencdmp.model.censorship.description.DescriptionCensor;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.planblueprint.PlanBlueprint;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.lookup.DescriptionLookup;
import org.opencdmp.service.description.DescriptionService;
import org.opencdmp.service.elastic.ElasticQueryHelperService;
import org.opencdmp.service.fieldsetexpander.FieldSetExpanderService;
import org.opencdmp.service.storage.StorageFileService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@RestController
@RequestMapping(path = "api/description")
@Tag(name = "Descriptions", description = "Manage descriptions")
@SwaggerCommonErrorResponses
public class DescriptionController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(DescriptionController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final DescriptionService descriptionService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final ElasticQueryHelperService elasticQueryHelperService;

    private final StorageFileService storageFileService;

    private final ConventionService conventionService;

    private final FieldSetExpanderService fieldSetExpanderService;

    public DescriptionController(
            BuilderFactory builderFactory,
            AuditService auditService,
            DescriptionService descriptionService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource,
            ElasticQueryHelperService elasticQueryHelperService, StorageFileService storageFileService, ConventionService conventionService, FieldSetExpanderService fieldSetExpanderService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.descriptionService = descriptionService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.elasticQueryHelperService = elasticQueryHelperService;
        this.storageFileService = storageFileService;
        this.conventionService = conventionService;
        this.fieldSetExpanderService = fieldSetExpanderService;
    }

    @PostMapping("public/query")
    @OperationWithTenantHeader(summary = "Query public descriptions")
    @Hidden
    public QueryResult<PublicDescription> publicQuery(@RequestBody DescriptionLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", PublicDescription.class.getSimpleName());

        this.censorFactory.censor(PublicDescriptionCensor.class).censor(lookup.getProject());

        QueryResult<PublicDescription> queryResult = this.elasticQueryHelperService.collectPublic(lookup, EnumSet.of(Public), null);

        this.auditService.track(AuditableAction.Description_PublicQuery, "lookup", lookup);

        return queryResult;
    }

    @GetMapping("public/{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific public description by id")
    @Hidden
    public PublicDescription publicGet(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + PublicDescription.class.getSimpleName()).And("id", id).And("fields", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(PublicDescriptionCensor.class).censor(fieldSet);

        DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(id).planSubQuery(this.queryFactory.query(PlanQuery.class).isActive(IsActive.Active).statuses(PlanStatus.Finalized).accessTypes(PlanAccessType.Public));

        PublicDescription model = this.builderFactory.builder(PublicDescriptionBuilder.class).authorize(EnumSet.of(Public)).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, PublicDescription.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Description_PublicLookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all descriptions", description = SwaggerHelpers.Description.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Description.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.Description.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Description.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Description.endpoint_query_response_example
            ))))
    public QueryResult<Description> query(@RequestBody DescriptionLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Description.class.getSimpleName());

        this.censorFactory.censor(DescriptionCensor.class).censor(lookup.getProject(), null);

        QueryResult<Description> queryResult = this.elasticQueryHelperService.collect(lookup, AuthorizationFlags.AllExceptPublic, null);

        this.auditService.track(AuditableAction.Description_Query, "lookup", lookup);

        return queryResult;
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific description by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Description.class
                    ))
            ))
    @Swagger404
    public Description get(
            @Parameter(name = "id", description = "The id of a description to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Description.class.getSimpleName()).And("id", id).And("fields", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        this.censorFactory.censor(DescriptionCensor.class).censor(fieldSet, null);

        DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Description model = this.builderFactory.builder(DescriptionBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Description_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing description", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Description.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionPersist.DescriptionPersistValidator.ValidatorName, argumentName = "model")
    public Description persist(
            @RequestBody DescriptionPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("persisting" + Description.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        Description persisted = this.descriptionService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Description_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @PostMapping("persist-status")
    @OperationWithTenantHeader(summary = "Update the status of an existing description", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Description.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionStatusPersist.DescriptionStatusPersistValidator.ValidatorName, argumentName = "model")
    public Description persistStatus(
            @RequestBody DescriptionStatusPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, IOException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + Description.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        Description persisted = this.descriptionService.persistStatus(model, fieldSet);

        this.auditService.track(AuditableAction.Description_PersistStatus, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @PostMapping("get-description-section-permissions")
    @OperationWithTenantHeader(summary = "Fetch the section specific user permissions")
    @Hidden
    @ValidationFilterAnnotation(validator = DescriptionSectionPermissionResolver.DescriptionSectionPermissionResolverPersistValidator.ValidatorName, argumentName = "model")
    public Map<UUID, List<String>> getDescriptionSectionPermissions(@RequestBody DescriptionSectionPermissionResolver model) {
        logger.debug(new MapLogEntry("persisting" + Description.class.getSimpleName()).And("model", model));
        Map<UUID, List<String>> persisted = this.descriptionService.getDescriptionSectionPermissions(model);

        this.auditService.track(AuditableAction.Description_GetDescriptionSectionPermissions, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return persisted;
    }

    @GetMapping("validate")
    @OperationWithTenantHeader(summary = "Validate if a description is ready for finalization by id")
    @Hidden
    public List<DescriptionValidationResult> validate(@RequestParam("descriptionIds") List<UUID> descriptionIds) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("validating" + Description.class.getSimpleName()).And("descriptionIds", descriptionIds));

        this.censorFactory.censor(DescriptionCensor.class).censor(null, null);

        List<DescriptionValidationResult> descriptionValidationResults = this.descriptionService.validate(descriptionIds);

        this.auditService.track(AuditableAction.Description_Validate, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("descriptionIds", descriptionIds)
        ));

        return descriptionValidationResults;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a description by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of a description to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("retrieving" + Description.class.getSimpleName()).And("id", id));

        this.descriptionService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Description_Delete, "id", id);
    }

    @GetMapping("{id}/export/{type}")
    @OperationWithTenantHeader(summary = "Export a description in various formats by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public ResponseEntity<byte[]> export(
            @Parameter(name = "id", description = "The id of a description to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "type", description = "The type of the export", example = "rda", required = true) @PathVariable("type") String exportType
    ) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting description"));

        return this.descriptionService.export(id, exportType, true);
    }

    @GetMapping("{id}/export-public/{type}")
    @OperationWithTenantHeader(summary = "Export a public description in various formats by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public ResponseEntity<byte[]> exportPublic(
            @Parameter(name = "id", description = "The id of a public description to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "type", description = "The type of the export", example = "rda", required = true) @PathVariable("type") String exportType
    ) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting description"));

        return this.descriptionService.export(id, exportType, false);
    }

    @PostMapping("field-file/upload")
    @OperationWithTenantHeader(summary = "Upload a file attachment on a field that supports it", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = StorageFile.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = DescriptionFieldFilePersist.PersistValidator.ValidatorName, argumentName = "model")
    public StorageFile uploadFieldFiles(
            @RequestParam("file") MultipartFile file,
            @RequestParam("model") DescriptionFieldFilePersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws IOException {
        logger.debug(new MapLogEntry("uploadFieldFiles" + Description.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);

        StorageFile persisted = this.descriptionService.uploadFieldFile(model, file, fieldSet);

        this.auditService.track(AuditableAction.Description_UploadFieldFiles, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @GetMapping("{id}/field-file/{fileId}")
    @OperationWithTenantHeader(summary = "Fetch a field file attachment as byte array", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public ResponseEntity<ByteArrayResource> getFieldFile(
            @Parameter(name = "id", description = "The id of a description", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fileIid", description = "The id of the file we want to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("fileId") UUID fileId
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("get Field File" + Description.class.getSimpleName()).And("id", id).And("fileId", fileId));

        StorageFileEntity storageFile = this.descriptionService.getFieldFile(id, fileId);

        byte[] file = this.storageFileService.readAsBytesSafe(id);
        if (file == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Description_GetFieldFile, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));

        String contentType = storageFile.getMimeType();
        if (this.conventionService.isNullOrEmpty(contentType))
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storageFile.getName() + (storageFile.getExtension().startsWith(".") ? "" : ".") + storageFile.getExtension() + "\"")
                .body(new ByteArrayResource(file));
    }

    @PostMapping("update-description-template")
    @OperationWithTenantHeader(summary = "Change the template of a description", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = UpdateDescriptionTemplatePersist.UpdateDescriptionTemplatePersistValidator.ValidatorName, argumentName = "model")
    public Boolean updateDescriptionTemplate(@RequestBody UpdateDescriptionTemplatePersist model) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException, JAXBException {
        logger.debug(new MapLogEntry("update description template" + Description.class.getSimpleName()).And("model", model));
        this.descriptionService.updateDescriptionTemplate(model);

        this.auditService.track(AuditableAction.Description_UpdateDescriptionTemplate, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return true;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xml/export/{id}", produces = "application/xml")
    @OperationWithTenantHeader(summary = "Export a description in xml format by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public @ResponseBody ResponseEntity<byte[]> getXml(
            @Parameter(name = "id", description = "The id of a description to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable UUID id
    ) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export" + Description.class.getSimpleName()).And("id", id));

        ResponseEntity<byte[]> response = this.descriptionService.exportXml(id);

        this.auditService.track(AuditableAction.Description_GetXml, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xml/export-public/{id}", produces = "application/xml")
    @OperationWithTenantHeader(summary = "Export a public description in xml format by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public @ResponseBody ResponseEntity<byte[]> getPublicXml(
            @Parameter(name = "id", description = "The id of a public description to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable UUID id
    ) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export public" + PublicDescription.class.getSimpleName()).And("id", id));

        ResponseEntity<byte[]> response = this.descriptionService.exportPublicXml(id);

        this.auditService.track(AuditableAction.Description_GetPublicXml, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));
        return response;
    }

}
