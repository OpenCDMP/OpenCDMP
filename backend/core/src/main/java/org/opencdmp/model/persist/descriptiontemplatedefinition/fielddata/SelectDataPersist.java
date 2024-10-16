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

public class SelectDataPersist extends BaseFieldDataPersist {

    private List<OptionPersist> options = null;

    public static final String _options = "options";


    private Boolean multipleSelect = null;

    public static final String _multipleSelect = "multipleSelect";

    public List<OptionPersist> getOptions() {
        return options;
    }

    public void setOptions(List<OptionPersist> options) {
        this.options = options;
    }

    public Boolean getMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    @Component(SelectDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SelectDataPersistValidator extends BaseFieldDataPersistValidator<SelectDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.SelectDataPersistValidator";

        private final ValidatorFactory validatorFactory;

        protected SelectDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<SelectDataPersist> modelClass() {
            return SelectDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(SelectDataPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getMultipleSelect()))
                            .failOn(SelectDataPersist._multipleSelect).failWith(messageSource.getMessage("Validation_Required", new Object[]{SelectDataPersist._multipleSelect}, LocaleContextHolder.getLocale())),

                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getOptions()))
                            .failOn(SelectDataPersist._options).failWith(messageSource.getMessage("Validation_Required", new Object[]{SelectDataPersist._options}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getOptions()))
                            .on(SelectDataPersist._options)
                            .over(item.getOptions())
                            .using((itm) -> this.validatorFactory.validator(OptionPersist.ComboBoxOptionPersistValidator.class))
            ));
            return specifications;
        }
    }

    public static class OptionPersist {
    
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
    
        @Component(ComboBoxOptionPersistValidator.ValidatorName)
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public static class ComboBoxOptionPersistValidator extends BaseValidator<OptionPersist> {
    
            public static final String ValidatorName = "DescriptionTemplate.ComboBoxOptionPersistValidator";
    
            private final MessageSource messageSource;
    
            protected ComboBoxOptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
                super(conventionService, errors);
                this.messageSource = messageSource;
            }
    
            @Override
            protected Class<OptionPersist> modelClass() {
                return OptionPersist.class;
            }
    
            @Override
            protected List<Specification> specifications(OptionPersist item) {
                return Arrays.asList(
                        this.spec()
                                .must(() -> !this.isEmpty(item.getLabel()))
                                .failOn(OptionPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{OptionPersist._label}, LocaleContextHolder.getLocale())),
                        this.spec()
                                .must(() -> !this.isEmpty(item.getValue()))
                                .failOn(OptionPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{OptionPersist._value}, LocaleContextHolder.getLocale()))
                );
            }
        }
    
    }
}
