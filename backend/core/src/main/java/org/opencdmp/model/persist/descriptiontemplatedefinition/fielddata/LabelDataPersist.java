package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

public class LabelDataPersist extends BaseFieldDataPersist {

    @Component(LabelPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class LabelPersistValidator extends BaseFieldDataPersistValidator<LabelDataPersist> {

        public static final String ValidatorName = "LabelPersistValidator";


        protected LabelPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<LabelDataPersist> modelClass() {
            return LabelDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(LabelDataPersist item) {
            return getBaseSpecifications(item);
        }
    }
}
