package org.opencdmp.controllers;

import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.filetransformerbase.models.misc.TemplateValidationResultModel;
import org.opencdmp.model.file.ExportRequestModel;
import org.opencdmp.model.file.FileEnvelope;
import org.opencdmp.model.file.FileTransformerConfiguration;
import org.opencdmp.model.file.TemplateValidationRequestModel;
import org.opencdmp.service.filetransformer.FileTransformerService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = {"/api/file-transformer/"})
@Tag(name = "File Transformers", description = "Manage file transformers, perform exports, imports", extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "10")))
@SwaggerCommonErrorResponses
public class FileTransformerController {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FileTransformerController.class));


    private final FileTransformerService fileTransformerService;
    private final AuditService auditService;

    @Autowired
    public FileTransformerController(FileTransformerService fileTransformerService, AuditService auditService){
        this.fileTransformerService = fileTransformerService;
	    this.auditService = auditService;
    }

    @GetMapping("/available")
    @OperationWithTenantHeader(summary = "Fetch all transformers", description = SwaggerHelpers.FileTransformer.endpoint_get_available_transformers,
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = FileTransformerConfiguration.class
                            )))
            ),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "1")))
    public List<FileTransformerConfiguration> getAvailableConfigurations() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("getAvailableConfigurations"));

        List<FileTransformerConfiguration> model = this.fileTransformerService.getAvailableExportFileFormats();
        this.auditService.track(AuditableAction.FileTransformer_GetAvailableConfigurations);
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return model;
    }

    @PostMapping("/validate-template")
    @OperationWithTenantHeader(summary = "Validate an uploaded export template", description = "Validates an uploaded export template file against the selected file transformer (optional plugin capability; returns no content when the transformer does not support validation).",
            responses = @ApiResponse(description = "OK", responseCode = "200"),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "9")))
    public TemplateValidationResultModel validateTemplate(@RequestBody TemplateValidationRequestModel requestModel) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("validating template").And("repositoryId", requestModel.getRepositoryId()));

        TemplateValidationResultModel model = this.fileTransformerService.validateTemplate(requestModel.getFileId(), requestModel.getRepositoryId(), requestModel.getEntityType());

        this.auditService.track(AuditableAction.FileTransformer_ValidateTemplate);
        return model;
    }

    @PostMapping("/export-plan")
    @OperationWithTenantHeader(summary = "Export a plan", description = SwaggerHelpers.FileTransformer.endpoint_export_plans,
            responses = @ApiResponse(description = "OK", responseCode = "200"),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "2")))
    public ResponseEntity<byte[]> exportPlan(@RequestBody ExportRequestModel requestModel) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting plan"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportPlan(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), false, requestModel.getOptions());
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(this.contentType(fileEnvelope));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /** The transformer's built-in template, to start a custom one from; 404 when the transformer offers none. */
    @GetMapping("/default-template/{repositoryId}")
    @io.swagger.v3.oas.annotations.Hidden
    public ResponseEntity<byte[]> getDefaultTemplate(@PathVariable("repositoryId") String repositoryId, @RequestParam(value = "entityType", required = false) org.opencdmp.commonmodels.enums.PluginEntityType entityType) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] data = this.fileTransformerService.getDefaultTemplate(repositoryId, entityType);
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();
        boolean description = org.opencdmp.commonmodels.enums.PluginEntityType.Description.equals(entityType);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + repositoryId + (description ? "-asset-template.docx" : "-plan-template.docx"));
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /** For the export settings pages: a PDF rendered with the settings as edited, shown inline. */
    @PostMapping("/preview-settings")
    @io.swagger.v3.oas.annotations.Hidden
    public ResponseEntity<byte[]> previewSettings(@RequestBody org.opencdmp.model.file.PluginSettingsPreviewRequest request) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("previewing file transformer settings").And("repositoryId", request != null ? request.getRepositoryId() : null));
        FileEnvelope fileEnvelope = this.fileTransformerService.previewWithSettings(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(fileEnvelope.getFile(), headers, HttpStatus.OK);
    }

    @PostMapping("/export-public-plan")
    @OperationWithTenantHeader(summary = "Export a public published plan", description = SwaggerHelpers.FileTransformer.endpoint_export_plans,
            responses = @ApiResponse(description = "OK", responseCode = "200"),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "4")))
    public ResponseEntity<byte[]> exportPublicPlan(@RequestBody ExportRequestModel requestModel) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting plan"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportPlan(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), true, requestModel.getOptions());
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(this.contentType(fileEnvelope));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/export-description")
    @OperationWithTenantHeader(summary = "Export a description", description = SwaggerHelpers.FileTransformer.endpoint_export_descriptions,
            responses = @ApiResponse(description = "OK", responseCode = "200"),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "3")))
    public ResponseEntity<byte[]> exportDescription(@RequestBody ExportRequestModel requestModel) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting description"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportDescription(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), false, requestModel.getOptions());
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(this.contentType(fileEnvelope));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/export-public-description")
    @OperationWithTenantHeader(summary = "Export a public description", description = SwaggerHelpers.FileTransformer.endpoint_export_descriptions,
            responses = @ApiResponse(description = "OK", responseCode = "200"),
            extensions = @Extension(name = "x-order", properties = @ExtensionProperty(name = "value", value = "5")))
    public ResponseEntity<byte[]> exportPublicDescription(@RequestBody ExportRequestModel requestModel) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting description"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportDescription(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), true, requestModel.getOptions());
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(this.contentType(fileEnvelope));
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    /** The type the transformer reports, else one guessed from the file name's extension. */
    private MediaType contentType(FileEnvelope fileEnvelope) {
        if (fileEnvelope.getMimeType() != null && !fileEnvelope.getMimeType().isBlank()) {
            try {
                return MediaType.parseMediaType(fileEnvelope.getMimeType());
            } catch (InvalidMediaTypeException e) {
                logger.warn("invalid mime type from file transformer: " + fileEnvelope.getMimeType());
            }
        }
        return fileEnvelope.getFilename() == null ? MediaType.APPLICATION_OCTET_STREAM : MediaTypeFactory.getMediaType(fileEnvelope.getFilename()).orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}
