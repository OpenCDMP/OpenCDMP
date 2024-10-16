package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

public class LabelAndMultiplicityDataPersist extends BaseFieldDataPersist {

    private Boolean multipleSelect;

    public static final String _multipleSelect = "multipleSelect";

    public Boolean getMultipleSelect() {
        return multipleSelect;
    }

    public void setMultipleSelect(Boolean multipleSelect) {
        this.multipleSelect = multipleSelect;
    }

    @Component(LabelAndMultiplicityDataPersist.LabelAndMultiplicityDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class LabelAndMultiplicityDataPersistValidator extends BaseFieldDataPersistValidator<LabelAndMultiplicityDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.LabelAndMultiplicityDataPersistValidator";

        protected LabelAndMultiplicityDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<LabelAndMultiplicityDataPersist> modelClass() {
            return LabelAndMultiplicityDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(LabelAndMultiplicityDataPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.add(
                    this.spec()
                            .must(() -> !this.isNull(item.getMultipleSelect()))
                            .failOn(LabelAndMultiplicityDataPersist._multipleSelect).failWith(messageSource.getMessage("Validation_Required", new Object[]{LabelAndMultiplicityDataPersist._multipleSelect}, LocaleContextHolder.getLocale()))
            );
            return specifications;
        }
    }

}
