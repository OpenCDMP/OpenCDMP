package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DescriptionFieldFilePersist {

    private UUID descriptionTemplateId;

    public static final String _descriptionTemplateId = "descriptionTemplateId";

    private String fieldId;

    public static final String _fieldId = "fieldId";

    public UUID getDescriptionTemplateId() {
        return descriptionTemplateId;
    }

    public void setDescriptionTemplateId(UUID descriptionTemplateId) {
        this.descriptionTemplateId = descriptionTemplateId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    @Component(PersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PersistValidator extends BaseValidator<DescriptionFieldFilePersist> {

        public static final String ValidatorName = "DescriptionFieldFilePersistValidator";

        private final MessageSource messageSource;

        protected PersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionFieldFilePersist> modelClass() {
            return DescriptionFieldFilePersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionFieldFilePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getDescriptionTemplateId()))
                            .failOn(DescriptionFieldFilePersist._descriptionTemplateId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionFieldFilePersist._descriptionTemplateId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getFieldId()))
                            .failOn(DescriptionFieldFilePersist._fieldId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionFieldFilePersist._fieldId}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
