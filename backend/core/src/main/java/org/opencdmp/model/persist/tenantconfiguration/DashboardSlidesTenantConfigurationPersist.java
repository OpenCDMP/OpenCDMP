package org.opencdmp.model.persist.tenantconfiguration;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DashboardSlideKind;
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

public class DashboardSlidesTenantConfigurationPersist {

    private List<DashboardSlidePersist> slides;
    public static final String _slides = "slides";

    public List<DashboardSlidePersist> getSlides() {
        return slides;
    }

    public void setSlides(List<DashboardSlidePersist> slides) {
        this.slides = slides;
    }

    @Component(DashboardSlidesTenantConfigurationPersist.DashboardSlidesTenantConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DashboardSlidesTenantConfigurationPersistValidator extends BaseValidator<DashboardSlidesTenantConfigurationPersist> {

        public static final String ValidatorName = "DashboardSlidesTenantConfigurationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected DashboardSlidesTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DashboardSlidesTenantConfigurationPersist> modelClass() {
            return DashboardSlidesTenantConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(DashboardSlidesTenantConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSlides()))
                            .must(() -> item.getSlides().stream().filter(x -> x != null && DashboardSlideKind.Recommended.equals(x.getKind())).count() <= 1)
                            .failOn(DashboardSlidesTenantConfigurationPersist._slides).failWith(messageSource.getMessage("Validation_Unique", new Object[]{DashboardSlidesTenantConfigurationPersist._slides}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSlides()))
                            .on(DashboardSlidesTenantConfigurationPersist._slides)
                            .over(item.getSlides())
                            .using((itm) -> this.validatorFactory.validator(DashboardSlidePersist.DashboardSlidePersistValidator.class))
            );
        }
    }
}
