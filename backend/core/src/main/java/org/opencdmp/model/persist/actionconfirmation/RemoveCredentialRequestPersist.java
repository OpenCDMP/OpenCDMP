package org.opencdmp.model.persist.actionconfirmation;

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
import java.util.UUID;

public class RemoveCredentialRequestPersist {

    private UUID credentialId;
    public static final String _credentialId = "credentialId";

    public UUID getCredentialId() {
        return this.credentialId;
    }

    public void setCredentialId(UUID credentialId) {
        this.credentialId = credentialId;
    }

    @Component(RemoveCredentialRequestPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RemoveCredentialRequestPersistValidator extends BaseValidator<RemoveCredentialRequestPersist> {

        public static final String ValidatorName = "RemoveCredentialRequestPersistValidator";

        private final MessageSource messageSource;


        protected RemoveCredentialRequestPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<RemoveCredentialRequestPersist> modelClass() {
            return RemoveCredentialRequestPersist.class;
        }

        @Override
        protected List<Specification> specifications(RemoveCredentialRequestPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getCredentialId()))
                            .failOn(RemoveCredentialRequestPersist._credentialId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{RemoveCredentialRequestPersist._credentialId}, LocaleContextHolder.getLocale()))
            );
        }
    }

}

