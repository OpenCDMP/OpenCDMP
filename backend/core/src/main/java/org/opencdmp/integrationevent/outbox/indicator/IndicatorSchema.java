package org.opencdmp.integrationevent.outbox.indicator;


import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IndicatorSchema {

    private UUID id;

    private List<IndicatorField> fields;
    public static final String _fields = "fields";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<IndicatorField> getFields() {
        return fields;
    }

    public void setFields(List<IndicatorField> fields) {
        this.fields = fields;
    }

    @Component(IndicatorSchemaValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class IndicatorSchemaValidator extends BaseValidator<IndicatorSchema> {

        public static final String ValidatorName = "IndicatorSchemaValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected IndicatorSchemaValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<IndicatorSchema> modelClass() {
            return IndicatorSchema.class;
        }

        @Override
        protected List<Specification> specifications(IndicatorSchema item) {
            return Arrays.asList(
                    this.navSpec()
                            .iff(() -> !this.isNull(item.getFields()))
                            .on(IndicatorSchema._fields)
                            .over(item.getFields())
                            .using((itm) -> this.validatorFactory.validator(IndicatorField.IndicatorFieldValidator.class))
            );
        }
    }
}
