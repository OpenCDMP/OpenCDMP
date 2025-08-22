package org.opencdmp.model.persist.tenantconfiguration;


import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.viewpreference.ViewPreferencePersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class ViewPreferencesConfigurationPersist {

    private List<ViewPreferencePersist> planPreferences;
    public static final String _planPreferences = "planPreferences";

    private List<ViewPreferencePersist> descriptionPreferences;
    public static final String _descriptionPreferences = "descriptionPreferences";

    public List<ViewPreferencePersist> getPlanPreferences() {
        return planPreferences;
    }

    public void setPlanPreferences(List<ViewPreferencePersist> planPreferences) {
        this.planPreferences = planPreferences;
    }

    public List<ViewPreferencePersist> getDescriptionPreferences() {
        return descriptionPreferences;
    }

    public void setDescriptionPreferences(List<ViewPreferencePersist> descriptionPreferences) {
        this.descriptionPreferences = descriptionPreferences;
    }

    @Component(ViewPreferencesConfigurationPersist.ViewPreferencesConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class ViewPreferencesConfigurationPersistValidator extends BaseValidator<ViewPreferencesConfigurationPersist> {

        public static final  String ValidatorName = "ViewPreferencesConfigurationPersistValidator";

        private final MessageSource messageSource;

        public final ValidatorFactory validatorFactory;

        protected ViewPreferencesConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource, ValidatorFactory validatorFactory){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<ViewPreferencesConfigurationPersist> modelClass() { return ViewPreferencesConfigurationPersist.class; }

        @Override
        protected List<Specification> specifications(ViewPreferencesConfigurationPersist item) {
            return Arrays.asList(
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getPlanPreferences()))
                            .on(ViewPreferencesConfigurationPersist._planPreferences)
                            .over(item.getPlanPreferences())
                            .using((itm) -> this.validatorFactory.validator(ViewPreferencePersist.ViewPreferencePersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getDescriptionPreferences()))
                            .on(ViewPreferencesConfigurationPersist._descriptionPreferences)
                            .over(item.getDescriptionPreferences())
                            .using((itm) -> this.validatorFactory.validator(ViewPreferencePersist.ViewPreferencePersistValidator.class))
            );
        }
    }
}
