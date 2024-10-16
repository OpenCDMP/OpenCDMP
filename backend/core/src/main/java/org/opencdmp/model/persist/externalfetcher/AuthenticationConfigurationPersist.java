package org.opencdmp.model.persist.externalfetcher;

import org.opencdmp.commons.enums.ExternalFetcherApiHTTPMethodType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public class AuthenticationConfigurationPersist {

    private Boolean enabled;

    public static final String _enabled = "enabled";

    private String authUrl;

    public static final String _authUrl = "authUrl";

    private ExternalFetcherApiHTTPMethodType authMethod;

    public static final String _authMethod = "authMethod";

    private String authTokenPath;

    public static final String _authTokenPath = "authTokenPath";

    private String authRequestBody;

    public static final String _authRequestBody = "authRequestBody";

    private String type;

    public static final String _type = "type";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public ExternalFetcherApiHTTPMethodType getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(ExternalFetcherApiHTTPMethodType authMethod) {
        this.authMethod = authMethod;
    }

    public String getAuthTokenPath() {
        return authTokenPath;
    }

    public void setAuthTokenPath(String authTokenPath) {
        this.authTokenPath = authTokenPath;
    }

    public String getAuthRequestBody() {
        return authRequestBody;
    }

    public void setAuthRequestBody(String authRequestBody) {
        this.authRequestBody = authRequestBody;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Component(AuthenticationConfigurationPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class AuthenticationConfigurationPersistValidator extends BaseValidator<AuthenticationConfigurationPersist> {

        public static final String ValidatorName = "AuthenticationConfigurationPersistValidator";

        private final MessageSource messageSource;

        protected AuthenticationConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<AuthenticationConfigurationPersist> modelClass() {
            return AuthenticationConfigurationPersist.class;
        }

        @Override
        protected List<Specification> specifications(AuthenticationConfigurationPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isNull(item.getEnabled()))
                            .failOn(AuthenticationConfigurationPersist._enabled).failWith(messageSource.getMessage("Validation_Required", new Object[]{AuthenticationConfigurationPersist._enabled}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getEnabled)
                            .must(() -> !this.isEmpty(item.getAuthUrl()))
                            .failOn(AuthenticationConfigurationPersist._authUrl).failWith(messageSource.getMessage("Validation_Required", new Object[]{AuthenticationConfigurationPersist._authUrl}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getEnabled)
                            .must(() -> !this.isNull(item.getAuthMethod()))
                            .failOn(AuthenticationConfigurationPersist._authMethod).failWith(messageSource.getMessage("Validation_Required", new Object[]{AuthenticationConfigurationPersist._authMethod}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getEnabled)
                            .must(() -> !this.isEmpty(item.getAuthTokenPath()))
                            .failOn(AuthenticationConfigurationPersist._authTokenPath).failWith(messageSource.getMessage("Validation_Required", new Object[]{AuthenticationConfigurationPersist._authTokenPath}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getEnabled)
                            .must(() -> !this.isEmpty(item.getAuthRequestBody()))
                            .failOn(AuthenticationConfigurationPersist._authRequestBody).failWith(messageSource.getMessage("Validation_Required", new Object[]{AuthenticationConfigurationPersist._authRequestBody}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(item::getEnabled)
                            .must(() -> !this.isEmpty(item.getType()))
                            .failOn(AuthenticationConfigurationPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{AuthenticationConfigurationPersist._type}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
