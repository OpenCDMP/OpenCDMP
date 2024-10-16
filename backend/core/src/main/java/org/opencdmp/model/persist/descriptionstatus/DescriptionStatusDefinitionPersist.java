package org.opencdmp.model.persist.descriptionstatus;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.descriptionstatus.DescriptionStatusDefinitionAuthorization;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DescriptionStatusDefinitionPersist {

    private DescriptionStatusDefinitionAuthorizationPersist authorization;
    public final static String _authorization = "authorization";

    public DescriptionStatusDefinitionAuthorizationPersist getAuthorization() { return authorization; }
    public void setAuthorization(DescriptionStatusDefinitionAuthorizationPersist authorization) { this.authorization = authorization; }


    @Component(DescriptionStatusDefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionStatusDefinitionPersistValidator extends BaseValidator<DescriptionStatusDefinitionPersist> {

        public final static String ValidatorName = "DescriptionStatusPersistValidation.DescriptionStatusDefinitionPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected DescriptionStatusDefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionStatusDefinitionPersist> modelClass() {
            return DescriptionStatusDefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionStatusDefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getAuthorization()))
                            .failOn(DescriptionStatusDefinitionPersist._authorization).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusDefinitionPersist._authorization}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getAuthorization()))
                            .on(DescriptionStatusDefinitionPersist._authorization)
                            .over(item.getAuthorization())
                            .using(() -> this.validatorFactory.validator(DescriptionStatusDefinitionAuthorizationPersist.DescriptionStatusDefinitionAuthorizationPersistValidator.class))
            );
        }
    }
}
