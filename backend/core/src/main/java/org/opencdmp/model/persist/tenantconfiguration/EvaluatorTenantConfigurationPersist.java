package org.opencdmp.model.persist.tenantconfiguration;

import gr.cite.tools.validation.specification.Specification;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.evaluator.EvaluatorSourcePersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class EvaluatorTenantConfigurationPersist {

    private List<EvaluatorSourcePersist> sources;
    public static final String _sources = "sources";
    private Boolean disableSystemSources;
    public static final String _disableSystemSources = "disableSystemSources";

    public List<EvaluatorSourcePersist> getSources() {
        return sources;
    }

    public void setSources(List<EvaluatorSourcePersist> sources) {
        this.sources = sources;
    }

    public Boolean getDisableSystemSources() {
        return disableSystemSources;
    }

    public void setDisableSystemSources(Boolean disableSystemSources) {
        this.disableSystemSources = disableSystemSources;
    }

    @Component(EvaluatorTenantConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluatorTenantConfigurationPersistValidator extends BaseValidator<EvaluatorTenantConfigurationPersist>{

        public static final  String ValidatorName = "EvaluatorTenantConfigurationPersistValidator";

        private final MessageSource messageSource;

        public final ValidatorFactory validatorFactory;

        protected EvaluatorTenantConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource, ValidatorFactory validatorFactory){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<EvaluatorTenantConfigurationPersist> modelClass() { return EvaluatorTenantConfigurationPersist.class; }

        @Override
        protected List<Specification> specifications(EvaluatorTenantConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getDisableSystemSources()))
                            .failOn(EvaluatorTenantConfigurationPersist._disableSystemSources).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorTenantConfigurationPersist._disableSystemSources}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getSources()))
                            .failOn(EvaluatorTenantConfigurationPersist._sources).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorTenantConfigurationPersist._sources}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSources()))
                            .on(EvaluatorTenantConfigurationPersist._sources)
                            .over(item.getSources())
                            .using((itm) -> this.validatorFactory.validator(EvaluatorSourcePersist.EvaluatorSourcePersistValidator.class))


            );
        }
    }
}
