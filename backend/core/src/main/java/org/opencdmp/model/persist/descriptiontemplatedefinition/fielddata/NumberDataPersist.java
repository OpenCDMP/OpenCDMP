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

public class NumberDataPersist extends BaseFieldDataPersist {

    @Component(NumberDataPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class NumberDataPersistValidator extends BaseFieldDataPersistValidator<NumberDataPersist> {

        public static final String ValidatorName = "NumberDataPersist";


        protected NumberDataPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
        }

        @Override
        protected Class<NumberDataPersist> modelClass() {
            return NumberDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(NumberDataPersist item) {
            return getBaseSpecifications(item);
        }
    }
}
