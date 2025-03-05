package org.opencdmp.model.persist;


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

public class DescriptionMultiplePersist {

    private List<DescriptionPersist> descriptions;
    public static final String _descriptions = "descriptions";

    public List<DescriptionPersist> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<DescriptionPersist> descriptions) {
        this.descriptions = descriptions;
    }

    @Component(DescriptionMultiplePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionMultiplePersistValidator extends BaseValidator<DescriptionMultiplePersist> {

        public static final String ValidatorName = "DescriptionMultiplePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;


        protected DescriptionMultiplePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionMultiplePersist> modelClass() {
            return DescriptionMultiplePersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionMultiplePersist item) {

            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getDescriptions()))
                            .failOn(DescriptionMultiplePersist._descriptions).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionMultiplePersist._descriptions}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptions()))
                            .on(DescriptionMultiplePersist._descriptions)
                            .over(item.getDescriptions())
                            .using((itm) -> this.validatorFactory.validator(DescriptionPersist.DescriptionPersistValidator.class))
                    );
        }

    }

}
