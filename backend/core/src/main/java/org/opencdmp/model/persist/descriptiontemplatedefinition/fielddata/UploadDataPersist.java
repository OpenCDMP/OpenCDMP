package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class UploadDataPersist extends BaseFieldDataPersist {

    private List<UploadOptionPersist> types = null;

    public static final String _types = "types";

    public List<UploadOptionPersist> getTypes() {
        return types;
    }

    public void setTypes(List<UploadOptionPersist> types) {
        this.types = types;
    }

    private Integer maxFileSizeInMB;

    public static final String _maxFileSizeInMB = "maxFileSizeInMB";

    public Integer getMaxFileSizeInMB() {
        return maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(Integer maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }

    @Component(UploadDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UploadDataPersistValidator extends BaseFieldDataPersistValidator<UploadDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.UploadDataPersistValidator";

        private final ValidatorFactory validatorFactory;

        protected UploadDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<UploadDataPersist> modelClass() {
            return UploadDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(UploadDataPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getTypes()))
                            .failOn(UploadDataPersist._types).failWith(messageSource.getMessage("Validation_Required", new Object[]{UploadDataPersist._types}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getMaxFileSizeInMB()))
                            .failOn(UploadDataPersist._maxFileSizeInMB).failWith(messageSource.getMessage("Validation_Required", new Object[]{UploadDataPersist._maxFileSizeInMB}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getMaxFileSizeInMB()))
                            .must(() -> item.getMaxFileSizeInMB() > 0 && item.getMaxFileSizeInMB() <= 10)
                            .failOn(UploadDataPersist._maxFileSizeInMB).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{UploadDataPersist._maxFileSizeInMB}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isNull(item.getTypes()))
                            .on(UploadDataPersist._types)
                            .over(item.getTypes())
                            .using((itm) -> this.validatorFactory.validator(UploadOptionPersist.UploadOptionPersistValidator.class))
            ));
            return specifications;
        }
    }

    public static class UploadOptionPersist {
    
        private String label = null;
    
        public static final String _label = "label";
    
        private String value = null;
    
        public static final String _value = "value";
    
        public String getLabel() {
            return label;
        }
    
        public void setLabel(String label) {
            this.label = label;
        }
    
        public String getValue() {
            return value;
        }
    
        public void setValue(String value) {
            this.value = value;
        }
    
        @Component(UploadOptionPersistValidator.ValidatorName)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public static class UploadOptionPersistValidator extends BaseValidator<UploadOptionPersist> {
    
            public static final String ValidatorName = "DescriptionTemplate.UploadOptionPersistValidator";
    
            private final MessageSource messageSource;
    
            protected UploadOptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
                super(conventionService, errors);
                this.messageSource = messageSource;
            }
    
            @Override
            protected Class<UploadOptionPersist> modelClass() {
                return UploadOptionPersist.class;
            }
    
            @Override
            protected List<Specification> specifications(UploadOptionPersist item) {
                return Arrays.asList(
                        this.spec()
                                .must(() -> !this.isEmpty(item.getLabel()))
                                .failOn(UploadOptionPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{UploadOptionPersist._label}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isEmpty(item.getValue()))
                                .failOn(UploadOptionPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{UploadOptionPersist._value}, LocaleContextHolder.getLocale()))
                );
            }
        }
    
    }
}

