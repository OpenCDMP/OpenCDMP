package org.opencdmp.model.persist.descriptiontemplatedefinition;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class DefaultValuePersist {


    private String textValue;
    public static final String _textValue = "textValue";

    private Instant dateValue;
    public static final String _dateValue = "dateValue";

    private Boolean booleanValue;
    public static final String _booleanValue = "booleanValue";

    public String getTextValue() {
        return this.textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Instant getDateValue() {
        return this.dateValue;
    }

    public void setDateValue(Instant dateValue) {
        this.dateValue = dateValue;
    }

    public Boolean getBooleanValue() {
        return this.booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Boolean isNullOrEmpty(){
        if ((this.textValue == null || this.textValue.isEmpty()) && this.dateValue == null && this.booleanValue == null) return true;
        return false;
    }


    @Component(DefaultValuePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DefaultValuePersistValidator extends BaseValidator<DefaultValuePersist> {

        public static final String ValidatorName = "DescriptionTemplate.DefaultValuePersistValidator";

        protected DefaultValuePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors) {
            super(conventionService, errors);
        }


        @Override
        protected Class<DefaultValuePersist> modelClass() {
            return DefaultValuePersist.class;
        }

        @Override
        protected List<Specification> specifications(DefaultValuePersist item) {
            return Arrays.asList(
            );
        }
    }

}
