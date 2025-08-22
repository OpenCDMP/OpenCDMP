package org.opencdmp.model.persist.pluginconfiguration;

import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.PluginType;
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


public class PluginConfigurationPersist {

    private String pluginCode;
    public static final String _pluginCode = "pluginCode";

    private PluginType pluginType;
    public static final String _pluginType = "pluginType";

    private List<PluginConfigurationFieldPersist> fields;
    public static final String _fields = "fields";

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public List<PluginConfigurationFieldPersist> getFields() {
        return fields;
    }

    public void setFields(List<PluginConfigurationFieldPersist> fields) {
        this.fields = fields;
    }

    @Component(PluginConfigurationPersist.PluginConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PluginConfigurationPersistValidator extends BaseValidator<PluginConfigurationPersist> {

        public static final String ValidatorName = "PluginConfigurationPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PluginConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PluginConfigurationPersist> modelClass() {
            return PluginConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(PluginConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getPluginCode()))
                            .failOn(PluginConfigurationPersist._pluginCode).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationPersist._pluginCode}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getPluginType()))
                            .failOn(PluginConfigurationPersist._pluginType).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationPersist._pluginType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getFields()))
                            .failOn(PluginConfigurationPersist._fields).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationPersist._fields}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getFields()))
                            .on(PluginConfigurationPersist._fields)
                            .over(item.getFields())
                            .using((itm) -> this.validatorFactory.validator(PluginConfigurationFieldPersist.PluginConfigurationFieldPersistValidator.class))
            );
        }
    }
}
