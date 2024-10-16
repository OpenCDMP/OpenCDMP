package org.opencdmp.model.persist.descriptionproperties;

import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.specification.Specification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ExternalIdentifierPersist {

    private String identifier;
    public static final String _identifier = "identifier";

    private String type;
    public static final String _type = "type";

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Component(PersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PersistValidator extends BaseValidator<ExternalIdentifierPersist> {

        public static final String ValidatorName = "Description.ExternalIdentifierPersistPersistValidator";

        private final MessageSource messageSource;

        protected PersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<ExternalIdentifierPersist> modelClass() {
            return ExternalIdentifierPersist.class;
        }

        @Override
        protected List<Specification> specifications(ExternalIdentifierPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getIdentifier()))
                            .failOn(ExternalIdentifierPersist._identifier).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalIdentifierPersist._identifier}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getType()))
                            .failOn(ExternalIdentifierPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalIdentifierPersist._type}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
