package org.opencdmp.model.persist.planblueprintdefinition;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PlanBlueprintExtraFieldDataType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ExtraFieldPersist extends FieldPersist {

    private PlanBlueprintExtraFieldDataType dataType;

    public static final String _dataType = "dataType";

    public PlanBlueprintExtraFieldDataType getDataType() {
        return this.dataType;
    }

    public void setDataType(PlanBlueprintExtraFieldDataType dataType) {
        this.dataType = dataType;
    }

    @Component(ExtraFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ExtraFieldPersistValidator extends BaseFieldPersistValidator<ExtraFieldPersist> {

        public static final String ValidatorName = "PlanBlueprint.ExtraFieldPersistValidator";

        protected ExtraFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<ExtraFieldPersist> modelClass() {
            return ExtraFieldPersist.class;
        }

        @Override
        protected List<Specification> specifications(ExtraFieldPersist item) {
            List<Specification> specifications = this.getBaseSpecifications(item);
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(FieldPersist._label).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{FieldPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getDataType()))
                            .failOn(ExtraFieldPersist._dataType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ExtraFieldPersist._dataType}, LocaleContextHolder.getLocale()))
            ));
            return specifications;
        }
    }

}
