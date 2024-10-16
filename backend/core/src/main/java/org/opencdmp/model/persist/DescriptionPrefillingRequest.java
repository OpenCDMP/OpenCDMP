package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DescriptionPrefillingRequest {

    private DescriptionPrefillingRequestData data;
    public static final String _data = "data";

    private UUID prefillingSourceId;

    public static final String _prefillingSourceId = "prefillingSourceId";

    private UUID descriptionTemplateId;

    public static final String _descriptionTemplateId = "descriptionTemplateId";

    private BaseFieldSet project;
    public static final String _project = "project";

    public DescriptionPrefillingRequestData getData() {
        return data;
    }

    public void setData(DescriptionPrefillingRequestData data) {
        this.data = data;
    }

    public UUID getPrefillingSourceId() {
        return prefillingSourceId;
    }

    public void setPrefillingSourceId(UUID prefillingSourceId) {
        this.prefillingSourceId = prefillingSourceId;
    }

    public UUID getDescriptionTemplateId() {
        return descriptionTemplateId;
    }

    public void setDescriptionTemplateId(UUID descriptionTemplateId) {
        this.descriptionTemplateId = descriptionTemplateId;
    }

    public BaseFieldSet getProject() {
        return project;
    }

    public void setProject(BaseFieldSet project) {
        this.project = project;
    }

    @Component(DescriptionProfilingRequestValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionProfilingRequestValidator extends BaseValidator<DescriptionPrefillingRequest> {

        public static final String ValidatorName = "DescriptionProfilingRequestValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;


        protected DescriptionProfilingRequestValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
	        this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionPrefillingRequest> modelClass() {
            return DescriptionPrefillingRequest.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionPrefillingRequest item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getDescriptionTemplateId()))
                            .failOn(DescriptionPrefillingRequest._descriptionTemplateId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionPrefillingRequest._descriptionTemplateId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPrefillingSourceId()))
                            .failOn(DescriptionPrefillingRequest._prefillingSourceId).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionPrefillingRequest._prefillingSourceId}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getData()))
                            .on(DescriptionReferencePersist._reference)
                            .over(item.getData())
                            .using(() -> this.validatorFactory.validator(DescriptionPrefillingRequestData.DescriptionPrefillingRequestDataValidator.class)),
                    this.spec()
                            .must(() -> !this.isNull(item.getData()))
                            .failOn(DescriptionPrefillingRequest._data).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionPrefillingRequest._data}, LocaleContextHolder.getLocale()))
            );
        }
    }

}


