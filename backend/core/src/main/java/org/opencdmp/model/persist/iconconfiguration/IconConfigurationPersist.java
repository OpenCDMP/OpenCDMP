package org.opencdmp.model.persist.iconconfiguration;


import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IconConfigurationPersist {

    public final static String _matIconName = "matIconName";
    private String matIconName;

    public final static String _storageFileId = "storageFileId";
    private UUID storageFileId;

    public String getMatIconName() {
        return matIconName;
    }

    public void setMatIconName(String matIconName) {
        this.matIconName = matIconName;
    }

    public UUID getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
    }

    @Component(IconConfigurationPersist.IconConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class IconConfigurationPersistValidator extends BaseValidator<IconConfigurationPersist> {
        public static final String ValidatorName = "IconConfigurationPersistValidator";
        private final MessageSource messageSource;

        public IconConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<IconConfigurationPersist> modelClass() {
            return IconConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(IconConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getMatIconName()))
                            .must(() -> this.isNull(item.getStorageFileId()))
                            .failOn(IconConfigurationPersist._storageFileId).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{IconConfigurationPersist._storageFileId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getStorageFileId()))
                            .must(() -> this.isEmpty(item.getMatIconName()))
                            .failOn(IconConfigurationPersist._matIconName).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{IconConfigurationPersist._matIconName}, LocaleContextHolder.getLocale()))

            );
        }

        @Override
        public Errors validateObject(Object target) {
            return super.validateObject(target);
        }
    }
}
