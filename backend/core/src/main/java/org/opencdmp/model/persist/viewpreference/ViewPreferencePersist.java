package org.opencdmp.model.persist.viewpreference;


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

public class ViewPreferencePersist {

    private UUID referenceTypeId;
    public static final String _referenceTypeId = "referenceTypeId";

    private Integer ordinal;
    public static final String _ordinal = "ordinal";

    public UUID getReferenceTypeId() {
        return referenceTypeId;
    }

    public void setReferenceTypeId(UUID referenceTypeId) {
        this.referenceTypeId = referenceTypeId;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @Component(ViewPreferencePersist.ViewPreferencePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ViewPreferencePersistValidator extends BaseValidator<ViewPreferencePersist> {

        public static final  String ValidatorName = "ViewPreferencePersistValidator";

        private final MessageSource messageSource;

        public final ValidatorFactory validatorFactory;

        protected ViewPreferencePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource, ValidatorFactory validatorFactory){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ViewPreferencePersist> modelClass() { return ViewPreferencePersist.class; }

        @Override
        protected List<Specification> specifications(ViewPreferencePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getReferenceTypeId()))
                            .failOn(ViewPreferencePersist._referenceTypeId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ViewPreferencePersist._referenceTypeId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(ViewPreferencePersist._ordinal)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{ViewPreferencePersist._ordinal}, LocaleContextHolder.getLocale()))

            );
        }
    }
}
