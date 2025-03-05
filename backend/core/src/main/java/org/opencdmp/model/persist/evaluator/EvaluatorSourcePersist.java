package org.opencdmp.model.persist.evaluator;


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

public class EvaluatorSourcePersist {

    private String evaluatorId;
    public static final String _evaluatorId = "evaluatorId";
    private String url;
    public static final String _url = "url";
    private String issuerUrl;
    public static final String _issuerUrl = "issuerUrl";
    private String clientId;
    public static final String _clientId = "clientId";
    private String clientSecret;
    public static final String _clientSecret = "clientSecret";
    private String scope;
    public static final String _scope = "scope";

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(String evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Component(EvaluatorSourcePersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class EvaluatorSourcePersistValidator extends BaseValidator<EvaluatorSourcePersist>{

        public static final String ValidatorName = "EvaluatorSourcePersistValidator";

        private final MessageSource messageSource;

        protected EvaluatorSourcePersistValidator(ConventionService conventionService, ErrorThesaurusProperties errorThesaurusProperties, MessageSource messageSource){
            super(conventionService, errorThesaurusProperties);
            this.messageSource = messageSource;
        }


        @Override
        protected Class<EvaluatorSourcePersist> modelClass() { return EvaluatorSourcePersist.class; }

        @Override
        protected List<Specification> specifications(EvaluatorSourcePersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getEvaluatorId()))
                            .failOn(EvaluatorSourcePersist._evaluatorId).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorSourcePersist._evaluatorId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getUrl()))
                            .failOn(EvaluatorSourcePersist._url).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorSourcePersist._url}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getIssuerUrl()))
                            .failOn(EvaluatorSourcePersist._issuerUrl).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorSourcePersist._issuerUrl}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getClientId()))
                            .failOn(EvaluatorSourcePersist._clientId).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorSourcePersist._clientId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getClientSecret()))
                            .failOn(EvaluatorSourcePersist._clientSecret).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorSourcePersist._clientSecret}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getScope()))
                            .failOn(EvaluatorSourcePersist._scope).failWith(messageSource.getMessage("Validation_Required", new Object[]{EvaluatorSourcePersist._scope}, LocaleContextHolder.getLocale()))
            );
        }
    }
}