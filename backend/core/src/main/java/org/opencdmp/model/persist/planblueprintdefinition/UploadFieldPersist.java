package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.ValidatorFactory;
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

public class UploadFieldPersist extends FieldPersist {

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

    @Component(UploadFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UploadFieldPersistValidator extends BaseFieldPersistValidator<UploadFieldPersist> {

        public static final String ValidatorName = "PlanBlueprint.UploadFieldPersist";

        private final ValidatorFactory validatorFactory;

        protected UploadFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<UploadFieldPersist> modelClass() {
            return UploadFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(UploadFieldPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(UploadFieldPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UploadFieldPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getTypes()))
                            .failOn(UploadFieldPersist._types).failWith(messageSource.getMessage("Validation_Required", new Object[]{UploadFieldPersist._types}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getMaxFileSizeInMB()))
                            .failOn(UploadFieldPersist._maxFileSizeInMB).failWith(messageSource.getMessage("Validation_Required", new Object[]{UploadFieldPersist._maxFileSizeInMB}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getMaxFileSizeInMB()))
                            .must(() -> item.getMaxFileSizeInMB() > 0 && item.getMaxFileSizeInMB() <= 10)
                            .failOn(UploadFieldPersist._maxFileSizeInMB).failWith(messageSource.getMessage("Validation_UnexpectedValue", new Object[]{UploadFieldPersist._maxFileSizeInMB}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isNull(item.getTypes()))
                            .on(UploadFieldPersist._types)
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
    
            public static final String ValidatorName = "PlanBlueprint.UploadOptionPersistValidator";
    
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

