package org.opencdmp.controllers;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.model.file.ExportRequestModel;
import org.opencdmp.model.file.FileEnvelope;
import org.opencdmp.model.file.RepositoryFileFormat;
import org.opencdmp.service.filetransformer.FileTransformerService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "File Transformers", description = "Manage file transformers, perform exports")
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
                                    implementation = RepositoryFileFormat.class
                            )))
            ))
    public List<RepositoryFileFormat> getAvailableConfigurations() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("getAvailableConfigurations"));

        List<RepositoryFileFormat> model = this.fileTransformerService.getAvailableExportFileFormats();
        this.auditService.track(AuditableAction.FileTransformer_GetAvailableConfigurations);
        //this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

        return model;
    }

    @PostMapping("/export-plan")
    @OperationWithTenantHeader(summary = "Export a plan", description = SwaggerHelpers.FileTransformer.endpoint_export_plans,
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<byte[]> exportPlan(@RequestBody ExportRequestModel requestModel) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting plan"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportPlan(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), false);
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/export-public-plan")
    @OperationWithTenantHeader(summary = "Export a public published plan", description = SwaggerHelpers.FileTransformer.endpoint_export_plans,
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<byte[]> exportPublicPlan(@RequestBody ExportRequestModel requestModel) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting plan"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportPlan(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), true);
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/export-description")
    @OperationWithTenantHeader(summary = "Export a description", description = SwaggerHelpers.FileTransformer.endpoint_export_descriptions,
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<byte[]> exportDescription(@RequestBody ExportRequestModel requestModel) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting description"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportDescription(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), false);
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/export-public-description")
    @OperationWithTenantHeader(summary = "Export a public description", description = SwaggerHelpers.FileTransformer.endpoint_export_descriptions,
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    public ResponseEntity<byte[]> exportPublicDescription(@RequestBody ExportRequestModel requestModel) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting description"));
        HttpHeaders headers = new HttpHeaders();

        FileEnvelope fileEnvelope = this.fileTransformerService.exportDescription(requestModel.getId(), requestModel.getRepositoryId(), requestModel.getFormat(), true);
        headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileEnvelope.getFilename(), StandardCharsets.UTF_8).replace("+", "_"));
        byte[] data = fileEnvelope.getFile();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
