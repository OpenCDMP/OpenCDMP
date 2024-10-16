package org.opencdmp.model.persist;

import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.planreference.PlanReferenceDataPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class PlanReferencePersist {

    private ReferencePersist reference;

    public static final String _reference = "reference";


    private PlanReferenceDataPersist data;

    public static final String _data = "data";


    public ReferencePersist getReference() {
        return reference;
    }

    public void setReference(ReferencePersist reference) {
        this.reference = reference;
    }

    public PlanReferenceDataPersist getData() {
        return data;
    }

    public void setData(PlanReferenceDataPersist data) {
        this.data = data;
    }

    @Component(PlanReferencePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PlanReferencePersistValidator extends BaseValidator<PlanReferencePersist> {

        public static final String ValidatorName = "PlanReferencePersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PlanReferencePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PlanReferencePersist> modelClass() {
            return PlanReferencePersist.class;
        }

        @Override
        protected List<Specification> specifications(PlanReferencePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getReference()))
                            .failOn(PlanReferencePersist._reference).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanReferencePersist._reference}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getReference()))
                            .on(PlanReferencePersist._reference)
                            .over(item.getReference())
                            .using(() -> this.validatorFactory.validator(ReferencePersist.ReferenceWithoutTypePersistValidator.class)),
                    this.spec()
                            .must(() -> !this.isNull(item.getData()))
                            .failOn(PlanReferencePersist._data).failWith(messageSource.getMessage("Validation_Required", new Object[]{PlanReferencePersist._data}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getData()))
                            .on(PlanReferencePersist._data)
                            .over(item.getData())
                            .using(() -> this.validatorFactory.validator(PlanReferenceDataPersist.PlanReferenceDataPersistValidator.class))
            );
        }

    }

}
