package org.opencdmp.controllers;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import org.apache.commons.io.FilenameUtils;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.StorageFileBuilder;
import org.opencdmp.model.persist.StorageFilePersist;
import org.opencdmp.query.StorageFileQuery;
import org.opencdmp.service.storage.StorageFileProperties;
import org.opencdmp.service.storage.StorageFileService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping(path = "api/storage-file")
public class StorageFileController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StorageFileController.class));
    private final AuditService auditService;
    private final QueryFactory queryFactory;
    private final BuilderFactory builderFactory;
    private final MessageSource messageSource;
    private final StorageFileService storageFileService;
    private final StorageFileProperties config;
    private final UserScope userScope;
    private final AuthorizationService authorizationService;
    private final ConventionService conventionService;
    private final ValidatorFactory validatorFactory;
    public StorageFileController(
            AuditService auditService,
            QueryFactory queryFactory,
            BuilderFactory builderFactory, MessageSource messageSource,
            StorageFileService storageFileService,
            StorageFileProperties config,
            UserScope userScope,
            AuthorizationService authorizationService, ConventionService conventionService, ValidatorFactory validatorFactory) {
        this.auditService = auditService;
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.messageSource = messageSource;
        this.storageFileService = storageFileService;
        this.config = config;
        this.userScope = userScope;
        this.authorizationService = authorizationService;
        this.conventionService = conventionService;
        this.validatorFactory = validatorFactory;
    }

    @GetMapping("{id}")
    public StorageFile get(@PathVariable("id") UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving " + StorageFile.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.authorizationService.authorizeForce(Permission.BrowseStorageFile, Permission.DeferredAffiliation);

        StorageFileQuery query = this.queryFactory.query(StorageFileQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        StorageFile model = this.builderFactory.builder(StorageFileBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.StorageFile_Query, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }


    @PostMapping("upload-temp-files")
    @Transactional
    public List<StorageFile> uploadTempFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        logger.debug("upload temp files");

        this.authorizationService.authorizeForce(Permission.EditStorageFile, Permission.DeferredAffiliation);

        List<StorageFile> addedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            StorageFilePersist storageFilePersist = new StorageFilePersist();
            storageFilePersist.setName(FilenameUtils.removeExtension(file.getOriginalFilename()));
            storageFilePersist.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            storageFilePersist.setMimeType(URLConnection.guessContentTypeFromName(file.getOriginalFilename()));
            storageFilePersist.setOwnerId(this.userScope.getUserIdSafe());
            storageFilePersist.setStorageType(StorageType.Temp);
            storageFilePersist.setLifetime(Duration.ofSeconds(this.config.getTempStoreLifetimeSeconds()));
            this.validatorFactory.validator(StorageFilePersist.StorageFilePersistValidator.class).validateForce(storageFilePersist);
            StorageFile persisted = this.storageFileService.persistBytes(storageFilePersist, file.getBytes(), new BaseFieldSet(StorageFile._id, StorageFile._name, StorageFile._extension));

            addedFiles.add(persisted);
        }
        this.auditService.track(AuditableAction.StorageFile_Upload, "models", addedFiles);

        return addedFiles;
    }

    @GetMapping("download/{id}")
    public ResponseEntity<byte[]> get(@PathVariable("id") UUID id) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("download" ).And("id", id));

        this.authorizationService.authorizeForce(Permission.BrowseStorageFile, Permission.DeferredAffiliation);

        StorageFileEntity storageFile = this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(id).firstAs(new BaseFieldSet().ensure(StorageFile._createdAt, StorageFile._fullName, StorageFile._mimeType, StorageFile._extension));
        if (storageFile == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        byte[] file = this.storageFileService.readAsBytesSafe(id);
        if (file == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, StorageFile.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.StorageFile_Download, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));

        String contentType = storageFile.getMimeType();
        if (this.conventionService.isNullOrEmpty(contentType)) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(file.length);
        responseHeaders.setContentType(MediaType.valueOf(contentType));
        responseHeaders.set("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(storageFile.getName() + (storageFile.getExtension().startsWith(".") ? "" : ".") + storageFile.getExtension(), StandardCharsets.UTF_8).replace("+", "_")  + "\"");
        responseHeaders.set("Access-Control-Expose-Headers", "Content-Disposition");
        responseHeaders.get("Access-Control-Expose-Headers").add("Content-Type");

        return new ResponseEntity<>(file, responseHeaders, HttpStatus.OK);
    }

}
