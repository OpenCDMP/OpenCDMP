package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintSystemFieldType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

public class SystemFieldPersist extends FieldPersist {

    private PlanBlueprintSystemFieldType systemFieldType;

    public static final String _systemFieldType = "systemFieldType";

    public PlanBlueprintSystemFieldType getSystemFieldType() {
        return this.systemFieldType;
    }

    public void setSystemFieldType(PlanBlueprintSystemFieldType systemFieldType) {
        this.systemFieldType = systemFieldType;
    }

    @Component(SystemFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SystemFieldPersistValidator extends BaseFieldPersistValidator<SystemFieldPersist> {

        public static final String ValidatorName = "PlanBlueprint.SystemFieldPersistValidator";

        protected SystemFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<SystemFieldPersist> modelClass() {
            return SystemFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(SystemFieldPersist item) {
            List<Specification> specifications = this.getBaseSpecifications(item);
            specifications.add(
                    this.spec()
                            .must(() -> !this.isNull(item.getSystemFieldType()))
                            .failOn(SystemFieldPersist._systemFieldType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SystemFieldPersist._systemFieldType}, LocaleContextHolder.getLocale()))
            );
            return specifications;
        }
    }

}

