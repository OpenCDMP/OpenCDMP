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
import java.util.UUID;

public class UpdatePlanDescriptionTemplate {

    private UUID id;
    public static final String _id = "id";

    private List<PlanDescriptionTemplatePersist> descriptionTemplates;

    public static final String _descriptionTemplates = "descriptionTemplates";

    private String hash;
    public static final String _hash = "hash";

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<PlanDescriptionTemplatePersist> getDescriptionTemplates() {
        return this.descriptionTemplates;
    }

    public void setDescriptionTemplates(List<PlanDescriptionTemplatePersist> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Component(UpdatePlanDescriptionTemplateValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UpdatePlanDescriptionTemplateValidator extends BaseValidator<UpdatePlanDescriptionTemplate> {

        public static final String ValidatorName = "UpdatePlanDescriptionTemplateValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected UpdatePlanDescriptionTemplateValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<UpdatePlanDescriptionTemplate> modelClass() {
            return UpdatePlanDescriptionTemplate.class;
        }

        @Override
        protected List<Specification> specifications(UpdatePlanDescriptionTemplate item) {

            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getId()))
                            .failOn(UpdatePlanDescriptionTemplate._id).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UpdatePlanDescriptionTemplate._id}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidHash(item.getHash()))
                            .failOn(UpdatePlanDescriptionTemplate._hash).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{UpdatePlanDescriptionTemplate._hash}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionTemplates()))
                            .on(UpdatePlanDescriptionTemplate._descriptionTemplates)
                            .over(item.getDescriptionTemplates())
                            .using((itm) -> this.validatorFactory.validator(PlanDescriptionTemplatePersist.PlanDescriptionTemplatePersistValidator.class))
            );
        }

    }

}
