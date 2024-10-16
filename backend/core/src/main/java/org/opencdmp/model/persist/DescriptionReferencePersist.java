package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptionreference.DescriptionReferenceDataPersist;
import org.opencdmp.model.persist.planreference.PlanReferenceDataPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class DescriptionReferencePersist {


    private ReferencePersist reference;

    public static final String _reference = "reference";


    private DescriptionReferenceDataPersist data;

    public static final String _data = "data";

    public ReferencePersist getReference() {
        return this.reference;
    }

    public void setReference(ReferencePersist reference) {
        this.reference = reference;
    }

    public DescriptionReferenceDataPersist getData() {
        return this.data;
    }

    public void setData(DescriptionReferenceDataPersist data) {
        this.data = data;
    }

    @Component(DescriptionReferencePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionReferencePersistValidator extends BaseValidator<DescriptionReferencePersist> {

        public static final String ValidatorName = "DescriptionReferencePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected DescriptionReferencePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionReferencePersist> modelClass() {
            return DescriptionReferencePersist.class;
        }

        @Override
        protected List<Specification> specifications(DescriptionReferencePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getReference()))
                            .failOn(DescriptionReferencePersist._reference).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionReferencePersist._reference}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getReference()))
                            .on(DescriptionReferencePersist._reference)
                            .over(item.getReference())
                            .using(() -> this.validatorFactory.validator(ReferencePersist.ReferencePersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isNull(item.getData()))
                            .failOn(DescriptionReferencePersist._data).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionReferencePersist._data}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getData()))
                            .on(DescriptionReferencePersist._data)
                            .over(item.getData())
                            .using(() -> this.validatorFactory.validator(PlanReferenceDataPersist.PlanReferenceDataPersistValidator.class))
            );
        }
    }

}


