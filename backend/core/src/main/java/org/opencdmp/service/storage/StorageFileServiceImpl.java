package org.opencdmp.service.storage;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.StorageFilePermission;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.enums.SupportiveMaterialFieldType;
import org.opencdmp.commons.scope.user.UserScope;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.StorageFileBuilder;
import org.opencdmp.model.persist.StorageFilePersist;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.management.InvalidApplicationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class StorageFileServiceImpl implements StorageFileService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(StorageFileServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final BuilderFactory builderFactory;
    private final UserScope userScope;
    private final StorageFileProperties config;

    @Autowired
    public StorageFileServiceImpl(
            TenantEntityManager entityManager,
            AuthorizationService authorizationService,
            BuilderFactory builderFactory,
            UserScope userScope,
            StorageFileProperties config
    ) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.builderFactory = builderFactory;
        this.userScope = userScope;
        this.config = config;
        
    }

    //region storage management
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.bootstrap();
    }

    private void bootstrap() {
        if (this.config.getStorages() != null) {
            for (StorageFileProperties.StorageConfig storage : this.config.getStorages()) {
                Path path = Paths.get(storage.getBasePath());
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public StorageFile persistBytes(StorageFilePersist model, byte[] payload, FieldSet fields) throws IOException {
        StorageFileEntity storageFile = this.buildDataEntry(model);

        File file = new File(this.filePath(storageFile.getFileRef(), storageFile.getStorageType()));
        if (file.exists()) throw new FileAlreadyExistsException(storageFile.getName());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(payload);
        }

        this.entityManager.persist(storageFile);
        this.entityManager.flush();
        return this.builderFactory.builder(StorageFileBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, StorageFile._id), storageFile);
    }

    @Override
    public StorageFile persistString(StorageFilePersist model, String payload, FieldSet fields, Charset charset) throws IOException {
        return this.persistBytes(model, payload.getBytes(charset), fields);
    }

    private StorageFileEntity buildDataEntry(StorageFilePersist model) {

        StorageFileEntity data = new StorageFileEntity();
        data.setId(UUID.randomUUID());
        data.setFileRef(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase(Locale.ROOT));
        data.setName(model.getName());
        data.setOwnerId(model.getOwnerId());
        data.setExtension(model.getExtension());
        data.setStorageType(model.getStorageType());
        data.setMimeType(model.getMimeType());
        data.setCreatedAt(Instant.now());
        data.setPurgeAt(model.getLifetime() == null ? null : Instant.now().plus(model.getLifetime()));

        return data;
    }

    @Override
    public StorageFile moveToStorage(UUID fileId, StorageType storageType, boolean replaceDestination, FieldSet fields) {
        try {
            StorageFileEntity storageFile = this.entityManager.find(StorageFileEntity.class, fileId);
            if (storageFile == null) return null;
            
            this.authorizeForce(storageFile, StorageFilePermission.Read);

            File file = new File(this.filePath(storageFile.getFileRef(), storageFile.getStorageType()));
            if (!file.exists()) return null;

            File destinationFile = new File(this.filePath(storageFile.getFileRef(), storageFile.getStorageType()));
            if (file.exists() && !replaceDestination) return null;
            
            boolean fileCopied = FileCopyUtils.copy(file, destinationFile) > 0;
            if (!fileCopied) return null;

            storageFile.setStorageType(storageType);

            this.entityManager.merge(storageFile);
            
            file.delete();

            this.entityManager.merge(storageFile);
            return this.builderFactory.builder(StorageFileBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, StorageFile._id), storageFile);
        }
        catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileId, ex);
            return null;
        }
    }

    @Override
    public StorageFile copyToStorage(UUID fileId, StorageType storageType, boolean replaceDestination, FieldSet fields) {
        try {
            StorageFileEntity storageFile = this.entityManager.find(StorageFileEntity.class, fileId);
            if (storageFile == null) return null;
            
            this.authorizeForce(storageFile, StorageFilePermission.Read);

            File file = new File(this.filePath(storageFile.getFileRef(), storageFile.getStorageType()));
            if (!file.exists()) return null;

            File destinationFile = new File(this.filePath(storageFile.getFileRef(), storageType));
            if (file.exists() && !replaceDestination) return null;
            
            boolean fileCopied = FileCopyUtils.copy(file, destinationFile) > 0;
            if (!fileCopied) return null;

            StorageFileEntity data = new StorageFileEntity();
            data.setId(UUID.randomUUID());
            data.setFileRef(storageFile.getFileRef());
            data.setName(storageFile.getName());
            data.setOwnerId(storageFile.getOwnerId());
            data.setExtension(storageFile.getExtension());
            data.setStorageType(storageType);
            data.setMimeType(storageFile.getMimeType());
            data.setCreatedAt(Instant.now());
            data.setPurgeAt(storageFile.getPurgeAt());

            this.entityManager.persist(data);
            
            this.entityManager.merge(storageFile);
            return this.builderFactory.builder(StorageFileBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, StorageFile._id), data);
            
        }
        catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileId, ex);
            return null;
        }
    }

    @Override
    public boolean exists(UUID fileId) {
        try {
            StorageFileEntity storageFile = this.entityManager.find(StorageFileEntity.class, fileId, true);
            if (storageFile == null) return false;
            this.authorizeForce(storageFile, StorageFilePermission.Read);

            File file = new File(this.filePath(storageFile.getFileRef(), storageFile.getStorageType()));
            return file.exists();

        }
        catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileId, ex);
            return false;
        }
    }

    @Override
    public boolean fileRefExists(String fileRef, StorageType storageType) {
        File file = null;
        try {
            file = new File(this.filePath(fileRef, storageType));
            return file.exists();
        } catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileRef, ex);
            return false;
        }
    }

    @Override
    public void updatePurgeAt(UUID fileId, Instant purgeAt) throws InvalidApplicationException {

        StorageFileEntity storageFile = this.entityManager.find(StorageFileEntity.class, fileId);
        if (storageFile == null) return;

        storageFile.setPurgeAt(purgeAt);
        this.entityManager.merge(storageFile);
        this.entityManager.flush();
    }

    @Override
    public boolean purgeSafe(UUID fileId) {
        try {
            StorageFileEntity storageFile = this.entityManager.find(StorageFileEntity.class, fileId);
            if (storageFile == null) return false;

            storageFile.setPurgedAt(Instant.now());

            this.entityManager.merge(storageFile);
            this.entityManager.flush();
            
            File file = new File(this.filePath(storageFile.getFileRef(), storageFile.getStorageType()));
            if (!file.exists()) return true;

            return file.delete();
        }
        catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileId, ex);
            return false;
        }
    }

    @Override
    public String readAsTextSafe(UUID fileId, Charset charset) {
        byte[] bytes = this.readAsBytesSafe(fileId);
        return bytes == null ? null : new String(bytes, charset);
    }

    @Override
    public byte[] readAsBytesSafe(UUID fileId) {
        byte[] bytes = null;
        try {
            StorageFileEntity storageFile = this.entityManager.find(StorageFileEntity.class, fileId, true);
            if (storageFile == null) return null;
            this.authorizeForce(storageFile, StorageFilePermission.Read);
            
            bytes = this.readByFileRefAsBytesSafe(storageFile.getFileRef(), storageFile.getStorageType());
        }
        catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileId, ex);
        }
        return bytes;
        
    }

    @Override
    public String readByFileRefAsTextSafe(String fileRef, StorageType storageType, Charset charset) {
        byte[] bytes = this.readByFileRefAsBytesSafe(fileRef, storageType);
        return bytes == null ? null : new String(bytes, charset);
    }

    @Override
    public byte[] readByFileRefAsBytesSafe(String fileRef, StorageType storageType) {
        try {
            return this.readFileBytes(this.filePath(fileRef, storageType));
        }
        catch (Exception ex) {
            logger.warn("problem reading byte content of storage file " + fileRef, ex);
            return null;
        }
    }

    private String filePath(String fileRef, StorageType storageType)
    {
        StorageFileProperties.StorageConfig storageFileConfig = this.config.getStorages().stream().filter(x -> storageType.equals(x.getType())).findFirst().orElse(null);
        if (storageFileConfig == null) throw new MyApplicationException("Storage " + storageType + " not found");
        return Paths.get(storageFileConfig.getBasePath(), fileRef).toString();
    }

    private void authorizeForce(StorageFileEntity storageFile, StorageFilePermission storageFilePermission) {
        if (storageFile.getOwnerId() != null && storageFile.getOwnerId().equals(this.userScope.getUserIdSafe())){
            return;
        }
        this.authorizationService.authorizeForce(Permission.BrowseStorageFile);
    }

    public byte[] readFileBytes(String path) throws IOException {

        byte[] bytes;
        File file = new File(path);
        if (!file.exists()) return null;
        try(InputStream inputStream = new FileInputStream(file)){
            bytes = inputStream.readAllBytes();
        }

	    return bytes;
    }
    
    //endregion
    
    //region file paths

    @Override
    public byte[] getSemanticsFile() {
        try {
            return this.readFileBytes(this.config.getStaticFiles().getSemantics());
        }
        catch (Exception ex) {
            logger.warn("problem reading semantics file", ex);
            return null;
        }
    }



    //endregion
    
    //region materials



    @Override
    public byte[] getSupportiveMaterial(SupportiveMaterialFieldType type, String language) {
        switch (type){
            case Faq -> {
                return this.getFaq(language);
            }
            case About -> {
                return this.getAbout(language);
            }
            case Glossary -> {
                return this.getGlossary(language);
            }
            case TermsOfService -> {
                return this.getTermsOfService(language);
            }
            case UserGuide -> {
                return this.getUserGuide(language);
            }
            case CookiePolicy -> {
                return this.getCookiePolicy(language);
            }
            default -> throw new InternalError("unknown type: " + type);
        }
    }

    @Override
    public byte[] getCookiePolicy(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getCookiePolicy(), this.config.getMaterialFiles().getCookiePolicyNamePattern(), language);
    }

    @Override
    public void setCookiePolicy(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getCookiePolicy(), this.config.getMaterialFiles().getCookiePolicyNamePattern(), language, payload);
    }

    @Override
    public byte[] getUserGuide(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getUserGuide(), this.config.getMaterialFiles().getUserGuideNamePattern(), language);
    }

    @Override
    public void setUserGuide(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getUserGuide(), this.config.getMaterialFiles().getUserGuideNamePattern(), language, payload);
    }

    @Override
    public byte[] getAbout(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getAbout(), this.config.getMaterialFiles().getAboutNamePattern(), language);
    }

    @Override
    public void setAbout(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getAbout(), this.config.getMaterialFiles().getAboutNamePattern(), language, payload);
    }

    @Override
    public byte[] getTermsOfService(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getTermsOfService(), this.config.getMaterialFiles().getTermsOfServiceNamePattern(), language);
    }

    @Override
    public void setTermsOfService(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getTermsOfService(), this.config.getMaterialFiles().getTermsOfServiceNamePattern(), language, payload);
    }

    @Override
    public byte[] getGlossary(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getGlossary(), this.config.getMaterialFiles().getGlossaryNamePattern(), language);
    }

    @Override
    public void setGlossary(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getGlossary(), this.config.getMaterialFiles().getGlossaryNamePattern(), language, payload);
    }

    @Override
    public byte[] getLanguage(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getLanguage(), this.config.getMaterialFiles().getLanguageNamePattern(), language);
    }

    @Override
    public File getLanguageFileName(String language) {
        return this.getLocalizedFile(this.config.getMaterialFiles().getLanguage(), this.config.getMaterialFiles().getLanguageNamePattern(), language);
    }
    @Override
    public void setLanguage(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getLanguage(), this.config.getMaterialFiles().getLanguageNamePattern(), language, payload);
    }

    @Override
    public byte[] getFaq(String language) {
        return this.getLocalized(this.config.getMaterialFiles().getFaq(), this.config.getMaterialFiles().getFaqNamePattern(), language);
    }

    @Override
    public void setFaq(String language, byte[] payload) {
        this.setLocalized(this.config.getMaterialFiles().getFaq(), this.config.getMaterialFiles().getFaqNamePattern(), language, payload);
    }

    private byte[] getLocalized(String baseDir, String pattern, String language) {
        try {
            File file = this.getLocalizedFile(baseDir, pattern, language);
            if (!file.exists()) file = this.getLocalizedFile(baseDir, pattern, this.config.getDefaultLanguage());
            if (!file.exists()) return null;
            return this.readFileBytes(file.getAbsolutePath());
        }
        catch (Exception ex) {
            logger.warn("problem reading " + baseDir + " " + language, ex);
            return null;
        }
    }

    private File getLocalizedFile(String baseDir, String pattern, String language) {
        return new File(baseDir +  this.getLocalizedNamePattern(pattern, language));
    }

    private void setLocalized(String baseDir, String pattern, String language, byte[] payload) {
        try {
            File file = this.getLocalizedFile(baseDir, pattern, language);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(payload);
            }
        }
        catch (Exception ex) {
            logger.warn("problem write " + baseDir + " " + language, ex);
        }
    }
    
    private String getLocalizedNamePattern(String pattern, String language){
        return pattern.replace(this.config.getMaterialFiles().getLocalizedNameLanguageKey(), language);
    }


    //endregion
}
