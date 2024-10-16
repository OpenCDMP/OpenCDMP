package org.opencdmp.model.persist;

import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.StorageFileEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StorageFilePersist {

    private String name;

    public static final String _name = "name";

    private String extension;

    public static final String _extension = "extension";

    private String mimeType;

    public static final String _mimeType = "mimeType";

    private StorageType storageType;

    public static final String _storageType = "storageType";

    private Duration lifetime;

    private UUID ownerId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Duration getLifetime() {
        return lifetime;
    }

    public void setLifetime(Duration lifetime) {
        this.lifetime = lifetime;
    }

    @Component(StorageFilePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class StorageFilePersistValidator extends BaseValidator<StorageFilePersist> {

        public static final String ValidatorName = "StorageFilePersistValidator";

        private final MessageSource messageSource;

        protected StorageFilePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<StorageFilePersist> modelClass() {
            return StorageFilePersist.class;
        }

        @Override
        protected List<Specification> specifications(StorageFilePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getName()))
                            .failOn(StorageFilePersist._name).failWith(messageSource.getMessage("Validation_Required", new Object[]{StorageFilePersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getName()))
                            .must(() -> this.lessEqualLength(item.getName(), StorageFileEntity._nameLen))
                            .failOn(StorageFilePersist._name).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{StorageFilePersist._name}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getExtension()))
                            .failOn(StorageFilePersist._extension).failWith(messageSource.getMessage("Validation_Required", new Object[]{StorageFilePersist._extension}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getExtension()))
                            .must(() -> this.lessEqualLength(item.getExtension(), StorageFileEntity._extensionLen))
                            .failOn(StorageFilePersist._extension).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{StorageFilePersist._extension}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getMimeType()))
                            .failOn(StorageFilePersist._mimeType).failWith(messageSource.getMessage("Validation_Required", new Object[]{StorageFilePersist._mimeType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getMimeType()))
                            .must(() -> this.lessEqualLength(item.getMimeType(), StorageFileEntity._mimeTypeLen))
                            .failOn(StorageFilePersist._mimeType).failWith(messageSource.getMessage("Validation_MaxLength", new Object[]{StorageFilePersist._mimeType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getStorageType()))
                            .failOn(StorageFilePersist._storageType).failWith(messageSource.getMessage("Validation_Required", new Object[]{StorageFilePersist._storageType}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
