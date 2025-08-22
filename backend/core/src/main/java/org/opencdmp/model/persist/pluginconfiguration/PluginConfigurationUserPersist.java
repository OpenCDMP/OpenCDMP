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


public class PluginConfigurationUserPersist {

    private String pluginCode;
    public static final String _pluginCode = "pluginCode";

    private PluginType pluginType;
    public static final String _pluginType = "pluginType";

    private List<PluginConfigurationUserFieldPersist> userFields;
    public static final String _userFields = "userFields";

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


    public List<PluginConfigurationUserFieldPersist> getUserFields() {
        return userFields;
    }

    public void setUserFields(List<PluginConfigurationUserFieldPersist> userFields) {
        this.userFields = userFields;
    }

    @Component(PluginConfigurationUserPersist.PluginConfigurationUserPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class PluginConfigurationUserPersistValidator extends BaseValidator<PluginConfigurationUserPersist> {

        public static final String ValidatorName = "PluginConfigurationUserPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        protected PluginConfigurationUserPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<PluginConfigurationUserPersist> modelClass() {
            return PluginConfigurationUserPersist.class;
        }

        @Override
        protected List<Specification> specifications(PluginConfigurationUserPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getPluginCode()))
                            .failOn(PluginConfigurationUserPersist._pluginCode).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationUserPersist._pluginCode}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getPluginType()))
                            .failOn(PluginConfigurationUserPersist._pluginType).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationUserPersist._pluginType}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getUserFields()))
                            .failOn(PluginConfigurationUserPersist._userFields).failWith(messageSource.getMessage("Validation_Required", new Object[]{PluginConfigurationUserPersist._userFields}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getUserFields()))
                            .on(PluginConfigurationUserPersist._userFields)
                            .over(item.getUserFields())
                            .using((itm) -> this.validatorFactory.validator(PluginConfigurationUserFieldPersist.PluginConfigurationUserFieldPersistValidator.class))
                    );
        }
    }
}
