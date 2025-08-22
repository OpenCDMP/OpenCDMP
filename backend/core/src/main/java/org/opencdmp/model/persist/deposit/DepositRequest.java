package org.opencdmp.model.persist.deposit;

import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.commons.enums.DepositAuthMethod;
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
import java.util.UUID;

public class DepositRequest {

    private String repositoryId;

    public static final String _repositoryId = "repositoryId";

    private UUID planId;

    public static final String _planId = "planId";

    private String authorizationCode;

    private DepositAuthMethod depositAuthInfoType;

    public static final String _depositAuthInfoType = "depositAuthInfoType";

    private BaseFieldSet project;

    public String getRepositoryId() {
        return this.repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public UUID getPlanId() {
        return this.planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public String getAuthorizationCode() {
        return this.authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public BaseFieldSet getProject() {
        return this.project;
    }

    public void setProject(BaseFieldSet project) {
        this.project = project;
    }

    public DepositAuthMethod getDepositAuthInfoType() {
        return depositAuthInfoType;
    }

    public void setDepositAuthInfoType(DepositAuthMethod depositAuthInfoType) {
        this.depositAuthInfoType = depositAuthInfoType;
    }

    @Component(DepositRequestValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DepositRequestValidator extends BaseValidator<DepositRequest> {

        public static final String ValidatorName = "DepositRequestValidator";

        private final MessageSource messageSource;

        protected DepositRequestValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource) {
            super(conventionService, errors);
            this.messageSource = messageSource;
        }

        @Override
        protected Class<DepositRequest> modelClass() {
            return DepositRequest.class;
        }

        @Override
        protected List<Specification> specifications(DepositRequest item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getRepositoryId()))
                            .failOn(DepositRequest._repositoryId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositRequest._repositoryId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> this.isValidGuid(item.getPlanId()))
                            .failOn(DepositRequest._planId).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositRequest._planId}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getDepositAuthInfoType()))
                            .failOn(DepositRequest._depositAuthInfoType).failWith(this.messageSource.getMessage("Validation_Required", new Object[]{DepositRequest._depositAuthInfoType}, LocaleContextHolder.getLocale()))
            );
        }
    }

}
