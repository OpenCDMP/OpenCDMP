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
import java.util.Objects;

public class EvaluatorTenantConfigurationPersist {

    private List<EvaluatorSourcePersist> sources;
    public static final String _sources = "sources";
    private Boolean disableSystemSources;
    public static final String _disableSystemSources = "disableSystemSources";
    private List<String> disabledSourceIds;
    public static final String _disabledSourceIds = "disabledSourceIds";
    private List<EvaluatorRestrictionPersist> restrictions;
    public static final String _restrictions = "restrictions";

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

    public List<String> getDisabledSourceIds() {
        return disabledSourceIds;
    }

    public void setDisabledSourceIds(List<String> disabledSourceIds) {
        this.disabledSourceIds = disabledSourceIds;
    }

    public List<EvaluatorRestrictionPersist> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<EvaluatorRestrictionPersist> restrictions) {
        this.restrictions = restrictions;
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
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSources()))
                            .on(EvaluatorTenantConfigurationPersist._sources)
                            .over(item.getSources())
                            .using((itm) -> this.validatorFactory.validator(EvaluatorSourcePersist.EvaluatorSourcePersistValidator.class)),
                    this.spec()
                            .iff(() -> !this.isListNullOrEmpty(item.getRestrictions()))
                            .must(() -> item.getRestrictions().stream().filter(Objects::nonNull).map(EvaluatorRestrictionPersist::getEvaluatorId).distinct().count() == item.getRestrictions().stream().filter(Objects::nonNull).count())
                            .failOn(EvaluatorTenantConfigurationPersist._restrictions).failWith(messageSource.getMessage("Validation_Unique", new Object[]{EvaluatorTenantConfigurationPersist._restrictions}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getRestrictions()))
                            .on(EvaluatorTenantConfigurationPersist._restrictions)
                            .over(item.getRestrictions())
                            .using((itm) -> this.validatorFactory.validator(EvaluatorRestrictionPersist.EvaluatorRestrictionPersistValidator.class))


            );
        }
    }
}
