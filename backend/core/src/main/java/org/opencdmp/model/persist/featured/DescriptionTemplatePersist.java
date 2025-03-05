package org.opencdmp.model.persist.featured;


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

public class DescriptionTemplatePersist {

    private UUID groupId;
    public static final String _groupId = "groupId";

    private Integer ordinal;
    public static final String _ordinal = "ordinal";

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @Component(DescriptionTemplatePersist.DescriptionTemplatePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DescriptionTemplatePersistValidator extends BaseValidator<DescriptionTemplatePersist> {

        public static final  String ValidatorName = "Featured.DescriptionTemplatePersistValidator";

        private final MessageSource messageSource;

        public final ValidatorFactory validatorFactory;

        protected DescriptionTemplatePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource, ValidatorFactory validatorFactory){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DescriptionTemplatePersist> modelClass() { return DescriptionTemplatePersist.class; }

        @Override
        protected List<Specification> specifications(DescriptionTemplatePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getGroupId()))
                            .failOn(DescriptionTemplatePersist._groupId)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._groupId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(DescriptionTemplatePersist._ordinal)
                            .failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DescriptionTemplatePersist._ordinal}, LocaleContextHolder.getLocale()))

            );
        }
    }
}
