package org.opencdmp.model.persist.deposit;

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

public class DepositAuthenticateRequest {

    private String repositoryId;

    public static final String _repositoryId = "repositoryId";

    private String code;

    public static final String _code = "code";

    public String getRepositoryId() {
        return this.repositoryId;
    }
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    @Component(DepositAuthenticateRequestValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DepositAuthenticateRequestValidator extends BaseValidator<DepositAuthenticateRequest> {

        public static final String ValidatorName = "DepositAuthenticateRequestValidator";

        private final MessageSource messageSource;

        protected DepositAuthenticateRequestValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DepositAuthenticateRequest> modelClass() {
            return DepositAuthenticateRequest.class;
        }

        @Override
        protected List<Specification> specifications(DepositAuthenticateRequest item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getRepositoryId()))
                            .failOn(DepositAuthenticateRequest._repositoryId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositAuthenticateRequest._repositoryId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getCode()))
                            .failOn(DepositAuthenticateRequest._code).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositAuthenticateRequest._code}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
