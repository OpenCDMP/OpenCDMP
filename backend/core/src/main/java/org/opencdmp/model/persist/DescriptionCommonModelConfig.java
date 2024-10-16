package org.opencdmp.model.persist;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DescriptionCommonModelConfig {

    private String id;
    public static final String _id = "id";
    private UUID sectionId;
    public static final String _sectionId = "_sectionId";
    private UUID templateId;
    public static final String _templateId = "templateId";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getSectionId() {
        return sectionId;
    }

    public void setSectionId(UUID sectionId) {
        this.sectionId = sectionId;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }


    @Component(DescriptionCommonModelConfigValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionCommonModelConfigValidator extends BaseValidator<DescriptionCommonModelConfig> {

        public static final String ValidatorName = "DescriptionCommonModelConfigValidator";

        private final MessageSource messageSource;

        protected DescriptionCommonModelConfigValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionCommonModelConfig> modelClass() {
            return DescriptionCommonModelConfig.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionCommonModelConfig item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getId()))
                            .failOn(DescriptionCommonModelConfig._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionCommonModelConfig._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isEmpty(item.getId()))
                            .must(() -> this.isValidGuid(UUID.fromString(item.getId())))
                            .failOn(DescriptionCommonModelConfig._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionCommonModelConfig._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getSectionId()))
                            .failOn(DescriptionCommonModelConfig._sectionId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionCommonModelConfig._sectionId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getTemplateId()))
                            .failOn(DescriptionCommonModelConfig._templateId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionCommonModelConfig._templateId}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
