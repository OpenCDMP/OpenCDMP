package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

public class SemanticValuePersist {

    private String key = null;

    public static final String _key = "key";

    private String value = null;

    public static final String _value = "value";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Component(SemanticValuePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SemanticValuePersistValidator extends BaseValidator<SemanticValuePersist> {

        public static final String ValidatorName = "DescriptionTemplate.SemanticValuePersistValidator";

        private final MessageSource messageSource;

        protected SemanticValuePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<SemanticValuePersist> modelClass() {
            return SemanticValuePersist.class;
        }

        @Override
        protected List<Specification> specifications(SemanticValuePersist item) {
            return List.of(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getKey()))
                            .failOn(SemanticValuePersist._key).failWith(messageSource.getMessage("Validation_Required", new Object[]{SemanticValuePersist._key}, LocaleContextHolder.getLocale()))
            );
        }
    }
}
