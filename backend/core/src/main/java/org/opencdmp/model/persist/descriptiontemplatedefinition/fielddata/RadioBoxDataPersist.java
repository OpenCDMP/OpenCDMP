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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RadioBoxDataPersist extends BaseFieldDataPersist {

    private List<RadioBoxOptionPersist> options = null;

    public static final String _options = "options";

    public List<RadioBoxOptionPersist> getOptions() {
        return options;
    }

    public void setOptions(List<RadioBoxOptionPersist> options) {
        this.options = options;
    }

    @Component(RadioBoxDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RadioBoxDataPersistValidator extends BaseFieldDataPersistValidator<RadioBoxDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.RadioBoxDataPersistValidator";

        private final ValidatorFactory validatorFactory;

        protected RadioBoxDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<RadioBoxDataPersist> modelClass() {
            return RadioBoxDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(RadioBoxDataPersist item) {

            // For Radio Box we don't have label, since it's the actual title of the question.
            return new ArrayList<>(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getFieldType()))
                            .failOn(BaseFieldDataPersist._fieldType).failWith(messageSource.getMessage("Validation_Required", new Object[]{BaseFieldDataPersist._fieldType}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getOptions()))
                            .on(RadioBoxDataPersist._options)
                            .over(item.getOptions())
                            .using((itm) -> this.validatorFactory.validator(RadioBoxOptionPersist.RadioBoxOptionPersistValidator.class))
            ));
        }
    }

	public static class RadioBoxOptionPersist {
	
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
	
	    @Component(RadioBoxOptionPersistValidator.ValidatorName)
	    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	    public static class RadioBoxOptionPersistValidator extends BaseValidator<RadioBoxOptionPersist> {
	
	        public static final String ValidatorName = "DescriptionTemplate.RadioBoxOptionPersistValidator";
	
	        private final MessageSource messageSource;
	
	        protected RadioBoxOptionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
	            super(conventionService, errors);
	            this.messageSource = messageSource;
	        }
	
	        @Override
	        protected Class<RadioBoxOptionPersist> modelClass() {
	            return RadioBoxOptionPersist.class;
	        }
	
	        @Override
	        protected List<Specification> specifications(RadioBoxOptionPersist item) {
	            return Arrays.asList(
	                    this.spec()
	                            .must(() -> !this.isEmpty(item.getLabel()))
	                            .failOn(RadioBoxOptionPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{RadioBoxOptionPersist._label}, LocaleContextHolder.getLocale())),
	                    this.spec()
	                            .must(() -> !this.isEmpty(item.getValue()))
	                            .failOn(RadioBoxOptionPersist._value).failWith(messageSource.getMessage("Validation_Required", new Object[]{RadioBoxOptionPersist._value}, LocaleContextHolder.getLocale()))
	            );
	        }
	    }
	
	}
}
