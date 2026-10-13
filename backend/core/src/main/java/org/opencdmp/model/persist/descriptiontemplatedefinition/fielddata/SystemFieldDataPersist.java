package org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

public class SystemFieldDataPersist extends BaseFieldDataPersist {

    private SystemFieldType systemFieldType;

    public static final String _systemFieldType = "systemFieldType";

    public SystemFieldType getSystemFieldType() {
        return this.systemFieldType;
    }

    public void setSystemFieldType(SystemFieldType systemFieldType) {
        this.systemFieldType = systemFieldType;
    }


    @Component(SystemFieldPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SystemFieldPersistValidator extends BaseFieldDataPersistValidator<SystemFieldDataPersist> {

        public static final String ValidatorName = "DescriptionTemplate.SystemFieldPersistValidator";

        private final ValidatorFactory validatorFactory;

        protected SystemFieldPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors, messageSource);
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<SystemFieldDataPersist> modelClass() {
            return SystemFieldDataPersist.class;
        }

        @Override
        protected List<Specification> specifications(SystemFieldDataPersist item) {
            List<Specification> specifications = getBaseSpecifications(item);
            specifications.add(
                    this.spec()
                            .must(() -> !this.isNull(item.getSystemFieldType()))
                            .failOn(SystemFieldDataPersist._systemFieldType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{SystemFieldDataPersist._systemFieldType}, LocaleContextHolder.getLocale()))
            );
            return specifications;
        }
    }
}

