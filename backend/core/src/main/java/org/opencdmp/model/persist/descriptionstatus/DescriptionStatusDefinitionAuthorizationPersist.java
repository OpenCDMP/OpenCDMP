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

public class DescriptionStatusDefinitionAuthorizationPersist {

    private DescriptionStatusDefinitionAuthorizationItemPersist edit;
    public final static String _edit = "edit";

    public DescriptionStatusDefinitionAuthorizationItemPersist getEdit() { return edit; }

    public void setEdit(DescriptionStatusDefinitionAuthorizationItemPersist edit) { this.edit = edit; }

    @Component(DescriptionStatusDefinitionAuthorizationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionStatusDefinitionAuthorizationPersistValidator extends BaseValidator<DescriptionStatusDefinitionAuthorizationPersist> {

        public static final String ValidatorName = "DescriptionStatusPersistValidation.DescriptionStatusDefinitionPersistValidator.DescriptionStatusDefinitionAuthorizationPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected DescriptionStatusDefinitionAuthorizationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionStatusDefinitionAuthorizationPersist> modelClass() {
            return DescriptionStatusDefinitionAuthorizationPersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionStatusDefinitionAuthorizationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getEdit()))
                            .failOn(DescriptionStatusDefinitionAuthorizationPersist._edit).failWith(messageSource.getMessage("Validation_Required", new Object[]{DescriptionStatusDefinitionAuthorizationPersist._edit}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getEdit()))
                            .on(DescriptionStatusDefinitionAuthorization._edit)
                            .over(item.getEdit())
                            .using(() -> this.validatorFactory.validator(DescriptionStatusDefinitionAuthorizationItemPersist.DescriptionStatusDefinitionAuthorizationItemPersistValidator.class))
            );
        }
    }
}
