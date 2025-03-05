package org.opencdmp.model.persist.planblueprintdefinition;

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

public class BlueprintDescriptionTemplatePersist {

    private UUID descriptionTemplateGroupId;

    public static final String _descriptionTemplateGroupId = "descriptionTemplateGroupId";

    private Integer minMultiplicity;

    public static final String _minMultiplicity = "minMultiplicity";

    private Integer maxMultiplicity;

    public static final String _maxMultiplicity = "maxMultiplicity";

    public UUID getDescriptionTemplateGroupId() {
        return this.descriptionTemplateGroupId;
    }

    public void setDescriptionTemplateGroupId(UUID descriptionTemplateGroupId) {
        this.descriptionTemplateGroupId = descriptionTemplateGroupId;
    }

    public Integer getMinMultiplicity() {
        return this.minMultiplicity;
    }

    public void setMinMultiplicity(Integer minMultiplicity) {
        this.minMultiplicity = minMultiplicity;
    }

    public Integer getMaxMultiplicity() {
        return this.maxMultiplicity;
    }

    public void setMaxMultiplicity(Integer maxMultiplicity) {
        this.maxMultiplicity = maxMultiplicity;
    }

    @Component(BlueprintDescriptionTemplatePersist.DescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionTemplatePersistValidator extends BaseValidator<BlueprintDescriptionTemplatePersist> {

        public static final String ValidatorName = "PlanBlueprint.DescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        protected DescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<BlueprintDescriptionTemplatePersist> modelClass() {
            return BlueprintDescriptionTemplatePersist.class;
        }

        @Override
        protected List<Specification> specifications(BlueprintDescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> this.isValidGuid(item.getDescriptionTemplateGroupId()))
                            .failOn(BlueprintDescriptionTemplatePersist._descriptionTemplateGroupId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{BlueprintDescriptionTemplatePersist._descriptionTemplateGroupId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getMinMultiplicity()))
                            .must(() -> item.getMinMultiplicity() >= 0)
                            .failOn(BlueprintDescriptionTemplatePersist._minMultiplicity).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{BlueprintDescriptionTemplatePersist._minMultiplicity}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(() -> !this.isNull(item.getMaxMultiplicity()))
                            .must(() -> item.getMaxMultiplicity() > 0)
                            .failOn(BlueprintDescriptionTemplatePersist._maxMultiplicity).failWith(this.messageSource.getMessage("Validation_UnexpectedValue", new Object[]{BlueprintDescriptionTemplatePersist._maxMultiplicity}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
