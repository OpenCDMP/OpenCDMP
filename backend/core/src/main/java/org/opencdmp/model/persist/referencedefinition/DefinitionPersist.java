package org.opencdmp.model.persist.referencedefinition;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DefinitionPersist {

    @NotNull(message = "{validation.empty}")
    @Valid
    private List<FieldPersist> fields = null;

    public static final String _fields = "fields";

    public List<FieldPersist> getFields() {
        return fields;
    }

    public void setFields(List<FieldPersist> fields) {
        this.fields = fields;
    }

    @Component(DefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DefinitionPersistValidator extends BaseValidator<DefinitionPersist> {

        public static final String ValidatorName = "Reference.DefinitionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected DefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DefinitionPersist> modelClass() {
            return DefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DefinitionPersist item) {
            return Arrays.asList(
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFields()))
                            .on(DefinitionPersist._fields)
                            .over(item.getFields())
                            .using((itm) -> this.validatorFactory.validator(FieldPersist.FieldPersistValidator.class))
            );
        }
    }

}
