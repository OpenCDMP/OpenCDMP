package org.opencdmp.model.persist.descriptionreference;

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

public class DescriptionReferenceDataPersist {

    private String fieldId;

    public static final String _fieldId = "fieldId";

    private Integer ordinal;

    public static final String _ordinal = "ordinal";


    public String getFieldId() {
        return this.fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public Integer getOrdinal() {
        return this.ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @Component(DescriptionReferenceDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionReferenceDataPersistValidator extends BaseValidator<DescriptionReferenceDataPersist> {

        public static final String ValidatorName = "DescriptionReferenceDataPersistValidator";
        private final MessageSource messageSource;

        protected DescriptionReferenceDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
	        this.messageSource = messageSource;
        }

        @Override
        protected Class<DescriptionReferenceDataPersist> modelClass() {
            return DescriptionReferenceDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionReferenceDataPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getFieldId()))
                            .failOn(DescriptionReferenceDataPersist._fieldId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionReferenceDataPersist._fieldId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(DescriptionReferenceDataPersist._ordinal).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionReferenceDataPersist._ordinal}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
