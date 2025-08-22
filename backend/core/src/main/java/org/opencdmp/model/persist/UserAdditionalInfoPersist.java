package org.opencdmp.model.persist;

import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationUserPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class UserAdditionalInfoPersist {

    private String avatarUrl;

    private String timezone;

    public static final String _timezone = "timezone";

    private String culture;

    public static final String _culture = "culture";

    private String language;

    public static final String _language = "language";

    private String roleOrganization;

    private ReferencePersist organization;

    public static final String _organization = "organization";

    private List<PluginConfigurationUserPersist> pluginConfigurations;

    public static final String _pluginConfigurations = "pluginConfigurations";

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCulture() {
        return culture;
    }

    public void setCulture(String culture) {
        this.culture = culture;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRoleOrganization() {
        return roleOrganization;
    }

    public void setRoleOrganization(String roleOrganization) {
        this.roleOrganization = roleOrganization;
    }

    public ReferencePersist getOrganization() {
        return organization;
    }

    public void setOrganization(ReferencePersist organization) {
        this.organization = organization;
    }

    public List<PluginConfigurationUserPersist> getPluginConfigurations() {
        return pluginConfigurations;
    }

    public void setPluginConfigurations(List<PluginConfigurationUserPersist> pluginConfigurations) {
        this.pluginConfigurations = pluginConfigurations;
    }

    @Component(UserAdditionalInfoPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UserAdditionalInfoPersistValidator extends BaseValidator<UserAdditionalInfoPersist> {

        public static final String ValidatorName = "UserAdditionalInfoPersistValidator";

        private final MessageSource messageSource;
        private final ValidatorFactory validatorFactory;

        protected UserAdditionalInfoPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<UserAdditionalInfoPersist> modelClass() {
            return UserAdditionalInfoPersist.class;
        }

        @Override
        protected List<Specification> specifications(UserAdditionalInfoPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getTimezone()))
                            .failOn(UserAdditionalInfoPersist._timezone).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserAdditionalInfoPersist._timezone}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCulture()))
                            .failOn(UserAdditionalInfoPersist._culture).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserAdditionalInfoPersist._culture}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLanguage()))
                            .failOn(UserAdditionalInfoPersist._language).failWith(messageSource.getMessage("Validation_Required", new Object[]{UserAdditionalInfoPersist._language}, LocaleContextHolder.getLocale())),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getOrganization()))
                            .on(UserAdditionalInfoPersist._organization)
                            .over(item.getOrganization())
                            .using(() -> this.validatorFactory.validator(ReferencePersist.ReferenceWithoutTypePersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getPluginConfigurations()))
                            .on(UserAdditionalInfoPersist._pluginConfigurations)
                            .over(item.getPluginConfigurations())
                            .using((itm) -> this.validatorFactory.validator(PluginConfigurationUserPersist.PluginConfigurationUserPersistValidator.class))
            );
        }
    }

}
