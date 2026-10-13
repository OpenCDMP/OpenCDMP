package org.opencdmp.controllers;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.logging.LoggerService;
import jakarta.transaction.Transactional;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.Permission;
import org.opencdmp.service.transfer.TenantCopyRequest;
import org.opencdmp.service.transfer.TenantPurgeReport;
import org.opencdmp.service.transfer.TenantPurgeRequest;
import org.opencdmp.service.transfer.TenantPurgeService;
import org.opencdmp.service.transfer.TenantTransferService;
import org.opencdmp.service.transfer.TransferExportRequest;
import org.opencdmp.service.transfer.TransferImportReport;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Map;

@RestController
@RequestMapping(path = "api/transfer")
public class TransferController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TransferController.class));

    private final AuthorizationService authorizationService;
    private final TenantTransferService tenantTransferService;
    private final TenantPurgeService tenantPurgeService;
    private final AuditService auditService;

    public TransferController(
            AuthorizationService authorizationService,
            TenantTransferService tenantTransferService,
            TenantPurgeService tenantPurgeService,
            AuditService auditService) {
        this.authorizationService = authorizationService;
        this.tenantTransferService = tenantTransferService;
        this.tenantPurgeService = tenantPurgeService;
        this.auditService = auditService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/export")
    public @ResponseBody ResponseEntity<byte[]> export(@RequestBody TransferExportRequest request) throws IOException, InvalidApplicationException {
        logger.debug("exporting tenant transfer bundle");
        this.authorizationService.authorizeForce(Permission.TransferTenantData);

        byte[] bundle = this.tenantTransferService.exportBundle(request);

        this.auditService.track(AuditableAction.TenantTransfer_Export);

        String fileName = "opencdmp-transfer-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC).format(Instant.now()) + ".zip";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "_"));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, Content-Type");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(bundle, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/import/analyze")
    @Transactional
    public TransferImportReport analyzeImport(@RequestParam("file") MultipartFile file) throws IOException, InvalidApplicationException {
        logger.debug("analyzing tenant transfer bundle");
        this.authorizationService.authorizeForce(Permission.TransferTenantData);

        TransferImportReport report = this.tenantTransferService.analyzeImport(file.getBytes());

        this.auditService.track(AuditableAction.TenantTransfer_AnalyzeImport, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("fileName", file.getOriginalFilename())));
        return report;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/import")
    @Transactional
    public TransferImportReport importBundle(@RequestParam("file") MultipartFile file) throws IOException, InvalidApplicationException {
        logger.debug("importing tenant transfer bundle");
        this.authorizationService.authorizeForce(Permission.TransferTenantData);

        TransferImportReport report = this.tenantTransferService.importBundle(file.getBytes());

        this.auditService.track(AuditableAction.TenantTransfer_Import, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("fileName", file.getOriginalFilename())));
        return report;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/copy/analyze")
    @Transactional
    public TransferImportReport analyzeCopy(@RequestBody TenantCopyRequest request) throws InvalidApplicationException {
        logger.debug("analyzing tenant copy");
        this.authorizationService.authorizeForce(Permission.TransferTenantData);

        TransferImportReport report = this.tenantTransferService.analyzeCopy(request);

        this.auditService.track(AuditableAction.TenantTransfer_AnalyzeCopy, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("sourceTenantCode", request == null ? null : request.getSourceTenantCode())));
        return report;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/copy")
    @Transactional
    public TransferImportReport copyTenant(@RequestBody TenantCopyRequest request) throws InvalidApplicationException {
        logger.debug("copying tenant data");
        this.authorizationService.authorizeForce(Permission.TransferTenantData);

        TransferImportReport report = this.tenantTransferService.copyTenant(request);

        this.auditService.track(AuditableAction.TenantTransfer_Copy, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("sourceTenantCode", request == null ? null : request.getSourceTenantCode())));
        return report;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/purge/analyze")
    @Transactional
    public TenantPurgeReport analyzePurge(@RequestBody TenantPurgeRequest request) throws InvalidApplicationException {
        logger.debug("analyzing tenant purge");
        this.authorizationService.authorizeForce(Permission.PurgeTenantData);

        TenantPurgeReport report = this.tenantPurgeService.analyzePurge(request);

        this.auditService.track(AuditableAction.TenantTransfer_AnalyzePurge);
        return report;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/purge")
    @Transactional
    public TenantPurgeReport purge(@RequestBody TenantPurgeRequest request) throws InvalidApplicationException {
        logger.debug("purging tenant data");
        this.authorizationService.authorizeForce(Permission.PurgeTenantData);

        TenantPurgeReport report = this.tenantPurgeService.purge(request);

        this.auditService.track(AuditableAction.TenantTransfer_Purge, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("request", request)));
        return report;
    }
}
